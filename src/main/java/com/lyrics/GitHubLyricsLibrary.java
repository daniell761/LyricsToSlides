package com.lyrics;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/**
 * GitHub Gist 在线歌词库
 * 使用GitHub Gist作为云存储，所有用户共享歌词库
 */
public class GitHubLyricsLibrary {
    
    // GitHub API配置
    private static String GITHUB_TOKEN = "";
    private static String GIST_ID = "";  // 用于存储所有歌词的主Gist
    
    private static final String GIST_API = "https://api.github.com/gists";
    private static final String USER_AGENT = "LyricsToSlides/1.0";
    
    // 本地缓存
    private static Map<String, String> lyricsCache = new HashMap<>();
    private static long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 2 * 60 * 1000; // 2分钟缓存
    
    /**
     * 设置GitHub Token
     * 获取方式: GitHub -> Settings -> Developer settings -> Personal access tokens -> Tokens (classic)
     * 需要勾选 'gist' 权限
     */
    public static void setGitHubToken(String token) {
        GITHUB_TOKEN = token;
    }
    
    /**
     * 设置Gist ID
     */
    public static void setGistId(String id) {
        GIST_ID = id;
    }
    
    /**
     * 获取当前Gist ID
     */
    public static String getGistId() {
        return GIST_ID;
    }
    
    /**
     * 检查是否已配置
     */
    public static boolean isConfigured() {
        return GITHUB_TOKEN != null && !GITHUB_TOKEN.isEmpty() && 
               GIST_ID != null && !GIST_ID.isEmpty();
    }
    
    /**
     * 初始化（创建或连接到Gist）
     */
    public static void initialize() throws IOException {
        if (GITHUB_TOKEN == null || GITHUB_TOKEN.isEmpty()) {
            throw new IllegalStateException("请先设置GitHub Token");
        }
        
        if (GIST_ID == null || GIST_ID.isEmpty()) {
            // 创建新的Gist
            createMainGist();
        } else {
            // 验证Gist是否存在
            try {
                refreshCache();
            } catch (IOException e) {
                throw new IOException("无法连接到Gist，请检查Gist ID是否正确: " + e.getMessage());
            }
        }
    }
    
    /**
     * 创建主Gist（用于存储所有歌词）
     */
    private static void createMainGist() throws IOException {
        URL url = new URL(GIST_API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setDoOutput(true);
            
            // 创建初始的空歌词库
            JSONObject json = new JSONObject();
            json.put("description", "歌词库 - Lyrics Library");
            json.put("public", true); // 公开，所有人都可以访问
            
            JSONObject files = new JSONObject();
            JSONObject readme = new JSONObject();
            readme.put("content", "# 歌词库\n这是一个公共歌词库，包含各种歌曲的歌词。");
            files.put("README.md", readme);
            
            json.put("files", files);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                String response = readResponse(conn.getInputStream());
                JSONObject responseJson = new JSONObject(response);
                GIST_ID = responseJson.getString("id");
                
                System.out.println("===========================================");
                System.out.println("成功创建在线歌词库！");
                System.out.println("Gist ID: " + GIST_ID);
                System.out.println("公开URL: " + responseJson.getString("html_url"));
                System.out.println("请保存此ID，以便下次使用！");
                System.out.println("===========================================");
            } else {
                String error = readResponse(conn.getErrorStream());
                throw new IOException("创建Gist失败 (" + responseCode + "): " + error);
            }
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * 保存歌词到在线库
     */
    public static void saveLyrics(String songName, String lyrics) throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException("请先初始化在线歌词库");
        }
        
        URL url = new URL(GIST_API + "/" + GIST_ID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setDoOutput(true);
            
            JSONObject json = new JSONObject();
            JSONObject files = new JSONObject();
            JSONObject fileContent = new JSONObject();
            
            String fileName = sanitizeFileName(songName) + ".txt";
            fileContent.put("content", lyrics);
            files.put(fileName, fileContent);
            
            json.put("files", files);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                // 更新缓存
                lyricsCache.put(songName, lyrics);
            } else {
                String error = readResponse(conn.getErrorStream());
                throw new IOException("保存失败 (" + responseCode + "): " + error);
            }
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * 从在线库加载歌词
     */
    public static String loadLyrics(String songName) throws IOException {
        refreshCacheIfNeeded();
        return lyricsCache.get(songName);
    }
    
    /**
     * 删除歌词
     */
    public static boolean deleteLyrics(String songName) throws IOException {
        if (!isConfigured()) {
            return false;
        }
        
        URL url = new URL(GIST_API + "/" + GIST_ID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setDoOutput(true);
            
            JSONObject json = new JSONObject();
            JSONObject files = new JSONObject();
            
            String fileName = sanitizeFileName(songName) + ".txt";
            files.put(fileName, JSONObject.NULL); // null表示删除
            
            json.put("files", files);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                lyricsCache.remove(songName);
                return true;
            }
            return false;
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * 重命名歌词
     */
    public static boolean renameLyrics(String oldName, String newName) throws IOException {
        String lyrics = loadLyrics(oldName);
        if (lyrics == null) {
            return false;
        }
        
        // 保存新名称
        saveLyrics(newName, lyrics);
        // 删除旧名称
        deleteLyrics(oldName);
        
        return true;
    }
    
    /**
     * 检查歌曲是否存在
     */
    public static boolean exists(String songName) throws IOException {
        refreshCacheIfNeeded();
        return lyricsCache.containsKey(songName);
    }
    
    /**
     * 获取所有歌曲列表
     */
    public static List<String> getAllSongs() throws IOException {
        refreshCacheIfNeeded();
        List<String> songs = new ArrayList<>(lyricsCache.keySet());
        Collections.sort(songs);
        return songs;
    }
    
    /**
     * 按首字母分组获取歌曲
     */
    public static Map<String, List<String>> getSongsByInitial() throws IOException {
        List<String> allSongs = getAllSongs();
        Map<String, List<String>> grouped = new TreeMap<>();
        
        for (String song : allSongs) {
            String initial = getInitial(song);
            grouped.computeIfAbsent(initial, k -> new ArrayList<>()).add(song);
        }
        
        return grouped;
    }
    
    /**
     * 搜索歌曲
     */
    public static List<String> searchSongs(String keyword) throws IOException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSongs();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        List<String> allSongs = getAllSongs();
        List<String> results = new ArrayList<>();
        
        for (String song : allSongs) {
            if (song.toLowerCase().contains(lowerKeyword)) {
                results.add(song);
            }
        }
        
        return results;
    }
    
    /**
     * 刷新缓存（如果需要）
     */
    private static void refreshCacheIfNeeded() throws IOException {
        long now = System.currentTimeMillis();
        if (lyricsCache.isEmpty() || now - lastCacheUpdate > CACHE_DURATION) {
            refreshCache();
        }
    }
    
    /**
     * 强制刷新缓存
     */
    public static void refreshCache() throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException("请先初始化在线歌词库");
        }
        
        URL url = new URL(GIST_API + "/" + GIST_ID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String response = readResponse(conn.getInputStream());
                JSONObject gist = new JSONObject(response);
                JSONObject files = gist.getJSONObject("files");
                
                lyricsCache.clear();
                
                for (String fileName : files.keySet()) {
                    if (fileName.endsWith(".txt") && !fileName.equals("README.md")) {
                        JSONObject file = files.getJSONObject(fileName);
                        String content = file.getString("content");
                        
                        // 从文件名恢复歌名
                        String songName = fileName.substring(0, fileName.length() - 4);
                        songName = songName.replace("_", " "); // 简单处理，你可能需要更复杂的逻辑
                        
                        lyricsCache.put(songName, content);
                    }
                }
                
                lastCacheUpdate = System.currentTimeMillis();
            } else {
                String error = readResponse(conn.getErrorStream());
                throw new IOException("刷新缓存失败 (" + responseCode + "): " + error);
            }
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * 清除本地缓存
     */
    public static void clearCache() {
        lyricsCache.clear();
        lastCacheUpdate = 0;
    }
    
    /**
     * 从本地库导入到在线库
     */
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
    
    /**
     * 从在线库导出到本地库
     */
    public static int exportToLocal() throws IOException {
        refreshCache();
        int count = 0;
        
        for (Map.Entry<String, String> entry : lyricsCache.entrySet()) {
            LyricsLibrary.saveLyrics(entry.getKey(), entry.getValue());
            count++;
        }
        
        return count;
    }
    
    /**
     * 获取首字母
     */
    private static String getInitial(String name) {
        if (name == null || name.isEmpty()) {
            return "#";
        }
        
        char first = name.charAt(0);
        
        if ((first >= 'A' && first <= 'Z') || (first >= 'a' && first <= 'z')) {
            return String.valueOf(first).toUpperCase();
        }
        
        if (first >= 0x4E00 && first <= 0x9FA5) {
            String pinyin = PinyinUtil.toPinyin(String.valueOf(first));
            if (pinyin != null && !pinyin.isEmpty()) {
                char pinyinFirst = pinyin.charAt(0);
                if ((pinyinFirst >= 'A' && pinyinFirst <= 'Z') || 
                    (pinyinFirst >= 'a' && pinyinFirst <= 'z')) {
                    return String.valueOf(pinyinFirst).toUpperCase();
                }
            }
        }
        
        return "#";
    }
    
    /**
     * 清理文件名
     */
    private static String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_")
                   .replaceAll("\\s+", "_");
    }
    
    /**
     * 读取HTTP响应
     */
    private static String readResponse(InputStream is) throws IOException {
        if (is == null) return "";
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
}
