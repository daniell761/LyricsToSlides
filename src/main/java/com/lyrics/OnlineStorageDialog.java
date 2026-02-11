package com.lyrics;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * 在线歌词库设置对话框
 */
public class OnlineStorageDialog extends JDialog {
    
    private JTextField tokenField;
    private JTextField gistIdField;
    private JButton testButton;
    private JButton createButton;
    private JButton saveButton;
    private JButton importButton;
    private JButton exportButton;
    private JLabel statusLabel;
    private JTextArea infoArea;
    
    public OnlineStorageDialog(Frame parent) {
        super(parent, "在线歌词库设置", true);
        setSize(650, 550);
        setLocationRelativeTo(parent);
        
        initUI();
        loadCurrentSettings();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 顶部说明
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // 中间设置区
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // 底部按钮
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("GitHub Gist 在线存储");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        panel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        
        JLabel descLabel = new JLabel("使用GitHub Gist将歌词存储在云端，所有用户都可以访问");
        descLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        descLabel.setForeground(Color.GRAY);
        panel.add(descLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Token设置
        JPanel tokenPanel = new JPanel(new BorderLayout(10, 5));
        tokenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("GitHub Personal Access Token"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel tokenLabel = new JLabel("Token:");
        tokenLabel.setPreferredSize(new Dimension(80, 25));
        tokenField = new JTextField();
        tokenField.setToolTipText("粘贴你的GitHub Token");
        
        JButton helpButton = new JButton("如何获取?");
        helpButton.addActionListener(e -> showTokenHelp());
        
        JPanel tokenInputPanel = new JPanel(new BorderLayout(5, 0));
        tokenInputPanel.add(tokenLabel, BorderLayout.WEST);
        tokenInputPanel.add(tokenField, BorderLayout.CENTER);
        tokenInputPanel.add(helpButton, BorderLayout.EAST);
        
        tokenPanel.add(tokenInputPanel, BorderLayout.NORTH);
        
        panel.add(tokenPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Gist ID设置
        JPanel gistPanel = new JPanel(new BorderLayout(10, 10));
        gistPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Gist ID（歌词库ID）"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel gistLabel = new JLabel("Gist ID:");
        gistLabel.setPreferredSize(new Dimension(80, 25));
        gistIdField = new JTextField();
        gistIdField.setToolTipText("如果已有Gist，粘贴ID。留空将创建新的。");
        
        JPanel gistInputPanel = new JPanel(new BorderLayout(5, 0));
        gistInputPanel.add(gistLabel, BorderLayout.WEST);
        gistInputPanel.add(gistIdField, BorderLayout.CENTER);
        
        gistPanel.add(gistInputPanel, BorderLayout.NORTH);
        
        // Gist操作按钮
        JPanel gistButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        createButton = new JButton("创建新Gist");
        createButton.setToolTipText("创建一个新的公共歌词库");
        createButton.addActionListener(e -> createNewGist());
        gistButtonPanel.add(createButton);
        
        testButton = new JButton("测试连接");
        testButton.setToolTipText("测试Token和Gist ID是否有效");
        testButton.addActionListener(e -> testConnection());
        gistButtonPanel.add(testButton);
        
        gistPanel.add(gistButtonPanel, BorderLayout.SOUTH);
        
        panel.add(gistPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // 状态显示
        statusLabel = new JLabel("未配置");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        statusLabel.setForeground(Color.ORANGE);
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(10));
        
        // 信息区域
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        infoArea.setBackground(new Color(245, 245, 245));
        infoArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoArea.setText("配置后可以：\n" +
                        "1. 将歌词保存到云端\n" +
                        "2. 从任何设备访问歌词\n" +
                        "3. 与其他用户共享歌词库");
        
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        panel.add(scrollPane);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 左侧：数据迁移
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        importButton = new JButton("导入本地→在线");
        importButton.setToolTipText("将本地歌词库上传到在线");
        importButton.addActionListener(e -> importFromLocal());
        leftPanel.add(importButton);
        
        exportButton = new JButton("导出在线→本地");
        exportButton.setToolTipText("将在线歌词库下载到本地");
        exportButton.addActionListener(e -> exportToLocal());
        leftPanel.add(exportButton);
        
        panel.add(leftPanel, BorderLayout.WEST);
        
        // 右侧：保存/取消
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        saveButton = new JButton("保存设置");
        saveButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        saveButton.setPreferredSize(new Dimension(110, 35));
        saveButton.addActionListener(e -> saveSettings());
        rightPanel.add(saveButton);
        
        JButton cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(80, 35));
        cancelButton.addActionListener(e -> dispose());
        rightPanel.add(cancelButton);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadCurrentSettings() {
        // 这里可以从配置文件加载保存的设置
        // 暂时留空，用户需要手动输入
    }
    
    private void showTokenHelp() {
        String help = "如何获取GitHub Personal Access Token:\n\n" +
                     "1. 登录GitHub (https://github.com)\n" +
                     "2. 点击右上角头像 -> Settings\n" +
                     "3. 左侧菜单最底部 -> Developer settings\n" +
                     "4. Personal access tokens -> Tokens (classic)\n" +
                     "5. 点击 Generate new token (classic)\n" +
                     "6. Note填写: LyricsApp\n" +
                     "7. 勾选权限: gist\n" +
                     "8. 点击 Generate token\n" +
                     "9. 复制生成的token（以ghp_开头）\n" +
                     "10. 粘贴到上方输入框\n\n" +
                     "注意: Token只显示一次，请妥善保存！";
        
        JTextArea textArea = new JTextArea(help);
        textArea.setEditable(false);
        textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 350));
        
        JOptionPane.showMessageDialog(this, scrollPane, "获取GitHub Token", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void createNewGist() {
        String token = tokenField.getText().trim();
        
        if (token.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "请先输入GitHub Token",
                "错误",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            statusLabel.setText("正在创建...");
            statusLabel.setForeground(Color.ORANGE);
            
            GitHubLyricsLibrary.setGitHubToken(token);
            GitHubLyricsLibrary.setGistId("");
            GitHubLyricsLibrary.initialize();
            
            String gistId = GitHubLyricsLibrary.getGistId();
            gistIdField.setText(gistId);
            
            statusLabel.setText("✓ 创建成功！");
            statusLabel.setForeground(new Color(0, 150, 0));
            
            infoArea.setText("成功创建在线歌词库！\n" +
                           "Gist ID: " + gistId + "\n" +
                           "请保存此ID，以便下次使用。\n\n" +
                           "现在可以:\n" +
                           "1. 点击\"导入本地→在线\"上传现有歌词\n" +
                           "2. 或直接保存设置开始使用");
            
        } catch (Exception ex) {
            statusLabel.setText("✗ 创建失败");
            statusLabel.setForeground(Color.RED);
            
            JOptionPane.showMessageDialog(this,
                "创建失败: " + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void testConnection() {
        String token = tokenField.getText().trim();
        String gistId = gistIdField.getText().trim();
        
        if (token.isEmpty() || gistId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "请先输入Token和Gist ID",
                "错误",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            statusLabel.setText("正在测试...");
            statusLabel.setForeground(Color.ORANGE);
            
            GitHubLyricsLibrary.setGitHubToken(token);
            GitHubLyricsLibrary.setGistId(gistId);
            GitHubLyricsLibrary.refreshCache();
            
            int songCount = GitHubLyricsLibrary.getAllSongs().size();
            
            statusLabel.setText("✓ 连接成功！");
            statusLabel.setForeground(new Color(0, 150, 0));
            
            infoArea.setText("连接成功！\n" +
                           "当前在线歌词库包含 " + songCount + " 首歌曲\n\n" +
                           "可以点击\"保存设置\"应用配置");
            
        } catch (Exception ex) {
            statusLabel.setText("✗ 连接失败");
            statusLabel.setForeground(Color.RED);
            
            JOptionPane.showMessageDialog(this,
                "连接失败: " + ex.getMessage() + "\n\n" +
                "请检查:\n" +
                "1. Token是否正确\n" +
                "2. Gist ID是否正确\n" +
                "3. 网络连接是否正常",
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void importFromLocal() {
        if (!GitHubLyricsLibrary.isConfigured()) {
            JOptionPane.showMessageDialog(this,
                "请先配置并测试连接",
                "提示",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要将所有本地歌词上传到在线库吗？\n" +
            "这可能需要一些时间。",
            "确认导入",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            int count = GitHubLyricsLibrary.importFromLocal();
            
            JOptionPane.showMessageDialog(this,
                "成功导入 " + count + " 首歌曲到在线库！",
                "导入完成",
                JOptionPane.INFORMATION_MESSAGE);
            
            infoArea.setText("导入完成！\n" +
                           "已上传 " + count + " 首歌曲到在线库");
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "导入失败: " + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportToLocal() {
        if (!GitHubLyricsLibrary.isConfigured()) {
            JOptionPane.showMessageDialog(this,
                "请先配置并测试连接",
                "提示",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要将在线歌词库下载到本地吗？\n" +
            "本地已存在的同名歌曲将被覆盖。",
            "确认导出",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            int count = GitHubLyricsLibrary.exportToLocal();
            
            JOptionPane.showMessageDialog(this,
                "成功导出 " + count + " 首歌曲到本地！",
                "导出完成",
                JOptionPane.INFORMATION_MESSAGE);
            
            infoArea.setText("导出完成！\n" +
                           "已下载 " + count + " 首歌曲到本地");
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "导出失败: " + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveSettings() {
        String token = tokenField.getText().trim();
        String gistId = gistIdField.getText().trim();
        
        if (token.isEmpty() || gistId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "请先输入Token和Gist ID",
                "错误",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            GitHubLyricsLibrary.setGitHubToken(token);
            GitHubLyricsLibrary.setGistId(gistId);
            
            // 这里可以保存到配置文件
            // saveToConfig(token, gistId);
            
            JOptionPane.showMessageDialog(this,
                "设置已保存！\n\n" +
                "现在可以在歌词库对话框中选择\"在线库\"来访问云端歌词。",
                "成功",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "保存失败: " + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
