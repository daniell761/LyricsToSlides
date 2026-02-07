package com.lyrics;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * 歌词库管理类
 * 负责保存、加载、搜索、删除歌词
 */
public class LyricsLibrary {
    
    private static final String LIBRARY_DIR = "lyrics_library";
    private static final String FILE_EXTENSION = ".txt";
    
    /**
     * 初始化歌词库（创建目录）
     */
    public static void initialize() {
        File dir = new File(LIBRARY_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * 保存歌词
     * @param songName 歌名
     * @param lyrics 歌词内容
     * @throws IOException 保存失败
     */
    public static void saveLyrics(String songName, String lyrics) throws IOException {
        initialize();
        
        // 清理歌名，移除非法字符
        String safeName = sanitizeFileName(songName);
        File file = new File(LIBRARY_DIR, safeName + FILE_EXTENSION);
        
        // 写入文件（UTF-8编码）
        Files.write(file.toPath(), lyrics.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 检查歌曲是否已存在
     * @param songName 歌名
     * @return 是否存在
     */
    public static boolean exists(String songName) {
        String safeName = sanitizeFileName(songName);
        File file = new File(LIBRARY_DIR, safeName + FILE_EXTENSION);
        return file.exists();
    }
    
    /**
     * 加载歌词
     * @param songName 歌名
     * @return 歌词内容，如果不存在返回null
     */
    public static String loadLyrics(String songName) {
        try {
            String safeName = sanitizeFileName(songName);
            File file = new File(LIBRARY_DIR, safeName + FILE_EXTENSION);
            
            if (!file.exists()) {
                return null;
            }
            
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * 删除歌词
     * @param songName 歌名
     * @return 是否删除成功
     */
    public static boolean deleteLyrics(String songName) {
        String safeName = sanitizeFileName(songName);
        File file = new File(LIBRARY_DIR, safeName + FILE_EXTENSION);
        return file.delete();
    }
    
    /**
     * 重命名歌词
     * @param oldName 旧歌名
     * @param newName 新歌名
     * @return 是否重命名成功
     */
    public static boolean   renameLyrics(String oldName, String newName) {
        String oldSafeName = sanitizeFileName(oldName);
        String newSafeName = sanitizeFileName(newName);
        
        File oldFile = new File(LIBRARY_DIR, oldSafeName + FILE_EXTENSION);
        File newFile = new File(LIBRARY_DIR, newSafeName + FILE_EXTENSION);
        
        if (!oldFile.exists() || newFile.exists()) {
            return false;
        }
        
        return oldFile.renameTo(newFile);
    }
    
    /**
     * 获取所有歌曲列表
     * @return 歌名列表（按字母排序）
     */
    public static List<String> getAllSongs() {
        initialize();
        
        File dir = new File(LIBRARY_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(FILE_EXTENSION));
        
        List<String> songs = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                // 移除扩展名
                name = name.substring(0, name.length() - FILE_EXTENSION.length());
                songs.add(name);
            }
        }
        
        // 按拼音/字母排序
        Collections.sort(songs, new PinyinComparator());
        
        return songs;
    }
    
    /**
     * 按首字母分组获取歌曲
     * @return Map<首字母, 歌曲列表>
     */
    public static Map<String, List<String>> getSongsByInitial() {
        List<String> allSongs = getAllSongs();
        Map<String, List<String>> grouped = new TreeMap<>();
        
        for (String song : allSongs) {
            String initial = getInitial(song);
            grouped.computeIfAbsent(initial, k -> new ArrayList<>()).add(song);
        }
        
        return grouped;
    }
    
    /**
     * 搜索歌曲（按歌名模糊搜索）
     * @param keyword 关键词
     * @return 匹配的歌名列表
     */
    public static List<String> searchSongs(String keyword) {
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
     * 获取歌名的首字母（拼音或英文）
     */
    private static String getInitial(String name) {
        if (name == null || name.isEmpty()) {
            return "#";
        }
        
        char first = name.charAt(0);
        
        // 如果是英文字母
        if ((first >= 'A' && first <= 'Z') || (first >= 'a' && first <= 'z')) {
            return String.valueOf(first).toUpperCase();
        }
        
        // 如果是中文，获取拼音首字母
        if (first >= 0x4E00 && first <= 0x9FA5) {
            String pinyin = PinyinUtil.toPinyin(String.valueOf(first));
            if (pinyin != null && !pinyin.isEmpty()) {
                char pinyinFirst = pinyin.charAt(0);
                if ((pinyinFirst >= 'A' && pinyinFirst <= 'Z') || (pinyinFirst >= 'a' && pinyinFirst <= 'z')) {
                    return String.valueOf(pinyinFirst).toUpperCase();
                }
            }
        }
        
        // 其他情况
        return "#";
    }
    
    /**
     * 清理文件名（移除非法字符）
     */
    private static String sanitizeFileName(String name) {
        // 移除文件系统不允许的字符
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
    
    /**
     * 拼音比较器（用于排序）
     */
    private static class PinyinComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            // 简单的字符串比较（中文会按Unicode排序，不太准确但够用）
            return s1.compareToIgnoreCase(s2);
        }
    }
}