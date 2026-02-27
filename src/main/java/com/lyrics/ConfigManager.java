package com.lyrics;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    // Save config in the current working directory
    private static final String CONFIG_FILE = "lyrics_config.properties";

    public static void saveConfig(String token, String gistId) throws IOException {
        Properties props = new Properties();
        props.setProperty("github_token", token);
        props.setProperty("gist_id", gistId);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Lyrics Library Config");
        }
    }

    public static void loadConfig() throws IOException {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        }

        String token = props.getProperty("github_token");
        String gistId = props.getProperty("gist_id");

        if (token != null && !token.isEmpty()) {
            GitHubLyricsLibrary.setGitHubToken(token);
        }
        if (gistId != null && !gistId.isEmpty()) {
            GitHubLyricsLibrary.setGistId(gistId);
        }
    }

    public static boolean configExists() {
        File file = new File(CONFIG_FILE);
        return file.exists();
    }
}
