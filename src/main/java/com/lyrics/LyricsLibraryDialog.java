package com.lyrics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

/**
 * 歌词库管理对话框
 */
public class LyricsLibraryDialog extends JDialog {
    
    private JList<String> songList;
    private DefaultListModel<String> listModel;
    private JTextField searchField;
    private ButtonGroup letterGroup;
    private String selectedSong = null;
    private String selectedLyrics = null;
    
    // 在线存储支持
    private boolean useOnline = false;
    private JToggleButton localButton;
    private JToggleButton onlineButton;
    private JButton settingsButton;
    
    public LyricsLibraryDialog(Frame parent) {
        super(parent, "歌词库", true);
        setSize(900, 650);
        setLocationRelativeTo(parent);
        
        initUI();
        loadAllSongs();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 顶部：存储类型切换
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // 左侧：字母筛选
        JPanel leftPanel = createLetterPanel();
        mainPanel.add(leftPanel, BorderLayout.WEST);
        
        // 中间：歌曲列表和搜索
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // 底部：按钮
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * 创建左侧字母筛选面板
     */
    private JPanel createLetterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "按首字母筛选",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Microsoft YaHei", Font.BOLD, 13)
        ));
        
        JPanel buttonsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        letterGroup = new ButtonGroup();
        
        // "全部" 按钮
        JRadioButton allButton = new JRadioButton("全部", true);
        allButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        allButton.setActionCommand("全部");
        allButton.addActionListener(e -> filterByLetter());
        letterGroup.add(allButton);
        buttonsPanel.add(allButton);
        
        // A-Z 按钮
        for (char c = 'A'; c <= 'Z'; c++) {
            JRadioButton button = new JRadioButton(String.valueOf(c));
            button.setFont(new Font("Arial", Font.BOLD, 13));
            button.setActionCommand(String.valueOf(c));
            button.addActionListener(e -> filterByLetter());
            letterGroup.add(button);
            buttonsPanel.add(button);
        }
        
        // "#" 按钮（其他字符）
        JRadioButton otherButton = new JRadioButton("#");
        otherButton.setFont(new Font("Arial", Font.BOLD, 13));
        otherButton.setActionCommand("#");
        otherButton.addActionListener(e -> filterByLetter());
        letterGroup.add(otherButton);
        buttonsPanel.add(otherButton);
        
        // 不使用滚动条，直接添加按钮面板
        panel.add(buttonsPanel, BorderLayout.CENTER);
        
        // 设置固定宽度，让面板足够大显示所有按钮
        panel.setPreferredSize(new Dimension(200, 0));
        
        return panel;
    }
    
    /**
     * 创建中间面板（搜索和列表）
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // 顶部：搜索
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("搜索：");
        searchLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        searchPanel.add(searchLabel);
        
        searchField = new JTextField(25);
        searchField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchSongs();
            }
        });
        searchPanel.add(searchField);
        
        JButton clearSearchButton = new JButton("清除");
        clearSearchButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            searchSongs();
        });
        searchPanel.add(clearSearchButton);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // 中间：歌曲列表
        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        
        JLabel label = new JLabel("已保存的歌词：");
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        listPanel.add(label, BorderLayout.NORTH);
        
        listModel = new DefaultListModel<>();
        songList = new JList<>(listModel);
        songList.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 双击加载
        songList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    loadSelectedSong();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(songList);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(listPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 左侧：管理按钮
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton renameButton = new JButton("重命名");
        renameButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        renameButton.addActionListener(e -> renameSong());
        leftPanel.add(renameButton);
        
        JButton deleteButton = new JButton("删除");
        deleteButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        deleteButton.setForeground(new Color(200, 0, 0));
        deleteButton.addActionListener(e -> deleteSong());
        leftPanel.add(deleteButton);
        
        panel.add(leftPanel, BorderLayout.WEST);
        
        // 右侧：确认按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton loadButton = new JButton("加载");
        loadButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        loadButton.setPreferredSize(new Dimension(110, 35));
        loadButton.setBackground(new Color(70, 130, 180));
        loadButton.setFocusPainted(false);
        loadButton.addActionListener(e -> loadSelectedSong());
        rightPanel.add(loadButton);
        
        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> {
            selectedSong = null;
            selectedLyrics = null;
            dispose();
        });
        rightPanel.add(cancelButton);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 加载所有歌曲
     */
    private void loadAllSongs() {
        listModel.clear();
        try {
            List<String> songs;
            if (useOnline) {
                songs = GitHubLyricsLibrary.getAllSongs();
            } else {
                songs = LyricsLibrary.getAllSongs();
            }
            for (String song : songs) {
                listModel.addElement(song);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "加载歌曲列表失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 搜索歌曲
     */
    private void searchSongs() {
        String keyword = searchField.getText().trim();
        listModel.clear();
        
        try {
            List<String> songs;
            if (useOnline) {
                songs = GitHubLyricsLibrary.searchSongs(keyword);
            } else {
                songs = LyricsLibrary.searchSongs(keyword);
            }
            for (String song : songs) {
                listModel.addElement(song);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "搜索失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 按首字母筛选
     */
    private void filterByLetter() {
        String letter = letterGroup.getSelection().getActionCommand();
        
        if ("全部".equals(letter)) {
            loadAllSongs();
            return;
        }
        
        listModel.clear();
        try {
            Map<String, List<String>> grouped;
            if (useOnline) {
                grouped = GitHubLyricsLibrary.getSongsByInitial();
            } else {
                grouped = LyricsLibrary.getSongsByInitial();
            }
            List<String> songs = grouped.get(letter);
            
            if (songs != null) {
                for (String song : songs) {
                    listModel.addElement(song);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "筛选失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 加载选中的歌曲
     */
    private void loadSelectedSong() {
        String selected = songList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "请先选择一首歌！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String lyrics;
            if (useOnline) {
                lyrics = GitHubLyricsLibrary.loadLyrics(selected);
            } else {
                lyrics = LyricsLibrary.loadLyrics(selected);
            }
            
            if (lyrics != null) {
                selectedSong = selected;
                selectedLyrics = lyrics;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "加载歌词失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "加载歌词失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 重命名歌曲
     */
    private void renameSong() {
        String selected = songList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "请先选择一首歌！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String newName = JOptionPane.showInputDialog(this, "请输入新的歌名：", selected);
        if (newName != null && !newName.trim().isEmpty()) {
            newName = newName.trim();
            
            try {
                boolean success;
                if (useOnline) {
                    success = GitHubLyricsLibrary.renameLyrics(selected, newName);
                } else {
                    success = LyricsLibrary.renameLyrics(selected, newName);
                }
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "重命名成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadAllSongs();
                } else {
                    JOptionPane.showMessageDialog(this, "重命名失败！可能新歌名已存在。", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "重命名失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 删除歌曲
     */
    private void deleteSong() {
        String selected = songList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "请先选择一首歌！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "确定要删除《" + selected + "》吗？",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success;
                if (useOnline) {
                    success = GitHubLyricsLibrary.deleteLyrics(selected);
                } else {
                    success = LyricsLibrary.deleteLyrics(selected);
                }
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadAllSongs();
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "删除失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 创建顶部面板（存储类型切换）
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // 左侧：切换按钮
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        localButton = new JToggleButton("本地库", true);
        localButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        localButton.setPreferredSize(new Dimension(110, 35));
        localButton.addActionListener(e -> switchToLocal());
        switchPanel.add(localButton);
        
        onlineButton = new JToggleButton("在线库");
        onlineButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        onlineButton.setPreferredSize(new Dimension(110, 35));
        onlineButton.addActionListener(e -> switchToOnline());
        switchPanel.add(onlineButton);
        
        ButtonGroup group = new ButtonGroup();
        group.add(localButton);
        group.add(onlineButton);
        
        panel.add(switchPanel, BorderLayout.WEST);
        
        // 右侧：设置按钮
        settingsButton = new JButton("在线设置");
        settingsButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        settingsButton.addActionListener(e -> openOnlineSettings());
        panel.add(settingsButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 切换到本地库
     */
    private void switchToLocal() {
        useOnline = false;
        loadAllSongs();
    }
    
    /**
     * 切换到在线库
     */
    private void switchToOnline() {
        if (!GitHubLyricsLibrary.isConfigured()) {
            JOptionPane.showMessageDialog(this,
                "请先配置在线歌词库！\n点击右侧\"在线设置\"按钮进行配置。",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
            localButton.setSelected(true);
            return;
        }
        
        useOnline = true;
        try {
            GitHubLyricsLibrary.refreshCache();
            loadAllSongs();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "连接在线库失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
            localButton.setSelected(true);
            useOnline = false;
        }
    }
    
    /**
     * 打开在线设置对话框
     */
    private void openOnlineSettings() {
        OnlineStorageDialog dialog = new OnlineStorageDialog((Frame) getOwner());
        dialog.setVisible(true);
        
        // 如果当前在在线模式，刷新列表
        if (useOnline && GitHubLyricsLibrary.isConfigured()) {
            try {
                GitHubLyricsLibrary.refreshCache();
                loadAllSongs();
            } catch (Exception e) {
                // 忽略错误
            }
        }
    }
    
    /**
     * 获取选中的歌名
     */
    public String getSelectedSong() {
        return selectedSong;
    }
    
    /**
     * 获取选中的歌词
     */
    public String getSelectedLyrics() {
        return selectedLyrics;
    }
}