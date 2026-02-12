package com.lyrics;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.JSONObject;

/**
 * GitHub Gist 在线歌词库
 * 使用GitHub Gist作为云存储，所有用户共享歌词库
 */
public class GitHubLyricsLibrary {

    // GitHub API配置
    private static String GITHUB_TOKEN = "";
    private static String GIST_ID = "";

    private static final String GIST_API = "https://api.github.com/gists";
    private static final String USER_AGENT = "LyricsToSlides/1.0";

    // Java 11 HttpClient (thread-safe)
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    // 本地缓存
    private static Map<String, String> lyricsCache = new HashMap<>();
    private static long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 2 * 60 * 1000;

    // ==============================
    // 基础配置
    // ==============================

    public static void setGitHubToken(String token) {
        GITHUB_TOKEN = token;
    }

    public static void setGistId(String id) {
        GIST_ID = id;
    }

    public static String getGistId() {
        return GIST_ID;
    }

    public static boolean isConfigured() {
        return GITHUB_TOKEN != null && !GITHUB_TOKEN.isEmpty()
                && GIST_ID != null && !GIST_ID.isEmpty();
    }

    // ==============================
    // 初始化
    // ==============================

    public static void initialize() throws IOException {

        if (GITHUB_TOKEN == null || GITHUB_TOKEN.isEmpty()) {
            throw new IllegalStateException("请先设置GitHub Token");
        }

        if (GIST_ID == null || GIST_ID.isEmpty()) {
            createMainGist();
        } else {
            refreshCache();
        }
    }

    private static void createMainGist() throws IOException {

        JSONObject json = new JSONObject();
        json.put("description", "歌词库 - Lyrics Library");
        json.put("public", true);

        JSONObject files = new JSONObject();
        JSONObject readme = new JSONObject();
        readme.put("content", "# 歌词库\n这是一个公共歌词库，包含各种歌曲的歌词。");
        files.put("README.md", readme);

        json.put("files", files);

        HttpResponse<String> response =
                sendRequest("POST", GIST_API, json.toString());

        if (response.statusCode() == 201) {

            JSONObject responseJson = new JSONObject(response.body());
            GIST_ID = responseJson.getString("id");

            System.out.println("===========================================");
            System.out.println("成功创建在线歌词库！");
            System.out.println("Gist ID: " + GIST_ID);
            System.out.println("公开URL: " + responseJson.getString("html_url"));
            System.out.println("请保存此ID，以便下次使用！");
            System.out.println("===========================================");

        } else {
            throw new IOException("创建Gist失败 (" +
                    response.statusCode() + "): " + response.body());
        }
    }

    // ==============================
    // 保存歌词
    // ==============================

    public static void saveLyrics(String songName, String lyrics) throws IOException {

        if (!isConfigured()) {
            throw new IllegalStateException("请先初始化在线歌词库");
        }

        JSONObject json = new JSONObject();
        JSONObject files = new JSONObject();
        JSONObject fileContent = new JSONObject();

        String fileName = sanitizeFileName(songName) + ".txt";
        fileContent.put("content", lyrics);
        files.put(fileName, fileContent);
        json.put("files", files);

        HttpResponse<String> response =
                sendRequest("PATCH", GIST_API + "/" + GIST_ID, json.toString());

        if (response.statusCode() == 200) {
            lyricsCache.put(songName, lyrics);
        } else {
            throw new IOException("保存失败 (" +
                    response.statusCode() + "): " + response.body());
        }
    }

    // ==============================
    // 删除歌词
    // ==============================

    public static boolean deleteLyrics(String songName) throws IOException {

        if (!isConfigured()) {
            return false;
        }

        JSONObject json = new JSONObject();
        JSONObject files = new JSONObject();

        String fileName = sanitizeFileName(songName) + ".txt";
        files.put(fileName, JSONObject.NULL);
        json.put("files", files);

        HttpResponse<String> response =
                sendRequest("PATCH", GIST_API + "/" + GIST_ID, json.toString());

        if (response.statusCode() == 200) {
            lyricsCache.remove(songName);
            return true;
        }

        return false;
    }

    // ==============================
    // 重命名
    // ==============================

    public static boolean renameLyrics(String oldName, String newName) throws IOException {

        String lyrics = loadLyrics(oldName);
        if (lyrics == null) {
            return false;
        }

        saveLyrics(newName, lyrics);
        deleteLyrics(oldName);

        return true;
    }

    // ==============================
    // 查询
    // ==============================

    public static String loadLyrics(String songName) throws IOException {
        refreshCacheIfNeeded();
        return lyricsCache.get(songName);
    }

    public static boolean exists(String songName) throws IOException {
        refreshCacheIfNeeded();
        return lyricsCache.containsKey(songName);
    }

    public static List<String> getAllSongs() throws IOException {
        refreshCacheIfNeeded();
        List<String> songs = new ArrayList<>(lyricsCache.keySet());
        Collections.sort(songs);
        return songs;
    }

    public static Map<String, List<String>> getSongsByInitial() throws IOException {

        List<String> allSongs = getAllSongs();
        Map<String, List<String>> grouped = new TreeMap<>();

        for (String song : allSongs) {
            String initial = getInitial(song);
            grouped.computeIfAbsent(initial, k -> new ArrayList<>()).add(song);
        }

        return grouped;
    }

    public static List<String> searchSongs(String keyword) throws IOException {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSongs();
        }

        String lowerKeyword = keyword.toLowerCase();
        List<String> results = new ArrayList<>();

        for (String song : getAllSongs()) {
            if (song.toLowerCase().contains(lowerKeyword)) {
                results.add(song);
            }
        }

        return results;
    }

    // ==============================
    // 缓存管理
    // ==============================

    private static void refreshCacheIfNeeded() throws IOException {

        long now = System.currentTimeMillis();
        if (lyricsCache.isEmpty() || now - lastCacheUpdate > CACHE_DURATION) {
            refreshCache();
        }
    }

    public static void refreshCache() throws IOException {

        if (!isConfigured()) {
            throw new IllegalStateException("请先初始化在线歌词库");
        }

        HttpResponse<String> response =
                sendRequest("GET", GIST_API + "/" + GIST_ID, null);

        if (response.statusCode() != 200) {
            throw new IOException("刷新缓存失败 (" +
                    response.statusCode() + "): " + response.body());
        }

        JSONObject gist = new JSONObject(response.body());
        JSONObject files = gist.getJSONObject("files");

        lyricsCache.clear();

        for (String fileName : files.keySet()) {

            if (fileName.endsWith(".txt")) {

                JSONObject file = files.getJSONObject(fileName);
                String content = file.getString("content");

                String songName =
                        fileName.substring(0, fileName.length() - 4)
                                .replace("_", " ");

                lyricsCache.put(songName, content);
            }
        }

        lastCacheUpdate = System.currentTimeMillis();
    }

    public static void clearCache() {
        lyricsCache.clear();
        lastCacheUpdate = 0;
    }

    public static int importFromLocal() throws IOException {
        List<String> localSongs = LyricsLibrary.getAllSongs();
        int count = 0;

        for (String song : localSongs) {
            String lyrics = LyricsLibrary.loadLyrics(song);
            if (lyrics != null) {
                saveLyrics(song, lyrics);
                count++;
            }
        }

        return count;
    }

    public static int exportToLocal() throws IOException {
        refreshCache();
        int count = 0;

        for (Map.Entry<String, String> entry : lyricsCache.entrySet()) {
            LyricsLibrary.saveLyrics(entry.getKey(), entry.getValue());
            count++;
        }

        return count;
    }


    // ==============================
    // HTTP Helper
    // ==============================

    private static HttpResponse<String> sendRequest(
            String method,
            String url,
            String body
    ) throws IOException {

        try {

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "token " + GITHUB_TOKEN)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", USER_AGENT)
                    .header("Content-Type", "application/json");

            if (body != null) {
                builder.method(method,
                        HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            } else {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            return CLIENT.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("请求被中断", e);
        }
    }

    // ==============================
    // 工具方法
    // ==============================

    private static String getInitial(String name) {

        if (name == null || name.isEmpty()) {
            return "#";
        }

        char first = name.charAt(0);

        // 如果是英文字母
        if ((first >= 'A' && first <= 'Z') || (first >= 'a' && first <= 'z')) {
            return String.valueOf(first).toUpperCase();
        }

        if (first >= 0x4E00 && first <= 0x9FA5) {
            String pinyin = PinyinUtil.toPinyin(String.valueOf(first));
            if (pinyin != null && !pinyin.isEmpty()) {
                return String.valueOf(Character.toUpperCase(pinyin.charAt(0)));
            }
        }

        return "#";
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_")
                   .replaceAll("\\s+", "_");
    }
}
