package com.lyrics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class LyricsToSlidesApp extends JFrame {
    
    private JTextArea lyricsTextArea;
    private JSpinner linesPerSlideSpinner;
    private JButton generateButton;
    private JButton clearButton;
    private JButton selectBackgroundButton;
    private JButton clearBackgroundButton;
    private JLabel statusLabel;
    private JTextField titleTextField;
    private JLabel backgroundPathLabel;
    private String selectedBackgroundPath = null;
    
    public LyricsToSlidesApp() {
        setTitle("歌词转PPT工具");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 680);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 顶部面板 - 标题和设置
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // 中间面板 - 歌词输入区
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // 底部面板 - 按钮和状态
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        // 标题输入
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.add(new JLabel("PPT标题："));
        titleTextField = new JTextField(30);
        titleTextField.setText("歌词展示");
        titlePanel.add(titleTextField);
        panel.add(titlePanel);
        
        // 每页行数设置
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settingsPanel.add(new JLabel("每张幻灯片行数："));
        
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(4, 1, 20, 1);
        linesPerSlideSpinner = new JSpinner(spinnerModel);
        settingsPanel.add(linesPerSlideSpinner);
        
        settingsPanel.add(new JLabel("   (建议: 2-6行)"));
        panel.add(settingsPanel);
        
        // 背景图片选择
        JPanel backgroundPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backgroundPanel.add(new JLabel("背景图片："));
        
        selectBackgroundButton = new JButton("选择图片");
        selectBackgroundButton.addActionListener(new SelectBackgroundListener());
        backgroundPanel.add(selectBackgroundButton);
        
        clearBackgroundButton = new JButton("使用默认");
        clearBackgroundButton.addActionListener(e -> {
            selectedBackgroundPath = null;
            updateBackgroundLabel();
        });
        backgroundPanel.add(clearBackgroundButton);
        
        backgroundPathLabel = new JLabel("(未选择，将使用默认纯色背景)");
        backgroundPathLabel.setForeground(Color.GRAY);
        backgroundPathLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        backgroundPanel.add(backgroundPathLabel);
        
        panel.add(backgroundPanel);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JLabel label = new JLabel("请输入歌词（每行一句）：");
        panel.add(label, BorderLayout.NORTH);
        
        lyricsTextArea = new JTextArea();
        lyricsTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        lyricsTextArea.setLineWrap(true);
        lyricsTextArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(lyricsTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        generateButton = new JButton("生成PPT");
        generateButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        generateButton.setPreferredSize(new Dimension(120, 35));
        generateButton.addActionListener(new GenerateButtonListener());
        buttonPanel.add(generateButton);
        
        clearButton = new JButton("清空");
        clearButton.setPreferredSize(new Dimension(100, 35));
        clearButton.addActionListener(e -> {
            lyricsTextArea.setText("");
            statusLabel.setText("已清空");
        });
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        // 状态标签
        statusLabel = new JLabel("准备就绪");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setForeground(Color.BLUE);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 更新背景图片标签显示
     */
    private void updateBackgroundLabel() {
        if (selectedBackgroundPath != null) {
            File file = new File(selectedBackgroundPath);
            String fileName = file.getName();
            if (fileName.length() > 40) {
                fileName = fileName.substring(0, 37) + "...";
            }
            backgroundPathLabel.setText("已选择: " + fileName);
            backgroundPathLabel.setForeground(new Color(0, 128, 0));
        } else {
            backgroundPathLabel.setText("(未选择，将使用默认纯色背景)");
            backgroundPathLabel.setForeground(Color.GRAY);
        }
    }
    
    /**
     * 选择背景图片监听器
     */
    private class SelectBackgroundListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择背景图片");
            
            // 设置文件过滤器
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "图片文件 (*.jpg, *.jpeg, *.png, *.gif, *.bmp)", 
                "jpg", "jpeg", "png", "gif", "bmp"
            );
            fileChooser.setFileFilter(filter);
            
            // 如果之前选择过图片，打开该图片所在的文件夹
            if (selectedBackgroundPath != null) {
                fileChooser.setCurrentDirectory(new File(selectedBackgroundPath).getParentFile());
            }
            
            int result = fileChooser.showOpenDialog(LyricsToSlidesApp.this);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                selectedBackgroundPath = selectedFile.getAbsolutePath();
                updateBackgroundLabel();
                statusLabel.setText("背景图片已选择: " + selectedFile.getName());
            }
        }
    }
    
    /**
     * 生成PPT按钮监听器
     */
    private class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String lyrics = lyricsTextArea.getText().trim();
            
            if (lyrics.isEmpty()) {
                JOptionPane.showMessageDialog(
                    LyricsToSlidesApp.this,
                    "请先输入歌词！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            
            // 选择保存位置
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("保存PowerPoint文件");
            fileChooser.setSelectedFile(new File("歌词展示.pptx"));
            
            int userSelection = fileChooser.showSaveDialog(LyricsToSlidesApp.this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                
                // 确保文件扩展名为.pptx
                if (!filePath.toLowerCase().endsWith(".pptx")) {
                    filePath += ".pptx";
                    fileToSave = new File(filePath);
                }
                
                // 创建final变量供lambda使用
                final String finalFilePath = filePath;
                final File finalFileToSave = fileToSave;
                
                // 生成PPT
                generateButton.setEnabled(false);
                statusLabel.setText("正在生成PPT...");
                
                new Thread(() -> {
                    try {
                        String title = titleTextField.getText().trim();
                        int linesPerSlide = (Integer) linesPerSlideSpinner.getValue();
                        
                        PowerPointGenerator generator = new PowerPointGenerator();
                        
                        // 设置背景图片（如果有选择）
                        if (selectedBackgroundPath != null) {
                            generator.setBackgroundImage(selectedBackgroundPath);
                        }
                        
                        generator.createPresentation(lyrics, title, linesPerSlide, finalFilePath);
                        
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("成功！文件已保存至：" + finalFilePath);
                            statusLabel.setForeground(new Color(0, 128, 0));
                            generateButton.setEnabled(true);
                            
                            int result = JOptionPane.showConfirmDialog(
                                LyricsToSlidesApp.this,
                                "PPT生成成功！\n是否打开文件？",
                                "成功",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            
                            if (result == JOptionPane.YES_OPTION) {
                                try {
                                    Desktop.getDesktop().open(finalFileToSave);
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(
                                        LyricsToSlidesApp.this,
                                        "无法打开文件：" + ex.getMessage(),
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE
                                    );
                                }
                            }
                        });
                        
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("生成失败：" + ex.getMessage());
                            statusLabel.setForeground(Color.RED);
                            generateButton.setEnabled(true);
                            
                            JOptionPane.showMessageDialog(
                                LyricsToSlidesApp.this,
                                "生成PPT时出错：\n" + ex.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE
                            );
                        });
                    }
                }).start();
            }
        }
    }
    
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LyricsToSlidesApp app = new LyricsToSlidesApp();
            app.setVisible(true);
        });
    }
}