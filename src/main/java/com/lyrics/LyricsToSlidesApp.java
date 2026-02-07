package com.lyrics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class LyricsToSlidesApp extends JFrame {
    
    private JTextArea lyricsTextArea;
    private JSpinner linesPerSlideSpinner;
    private JButton generateButton;
    private JButton clearButton;
    private JButton selectBackgroundButton;
    private JButton clearBackgroundButton;
    private JButton fontColorButton;
    private JLabel statusLabel;
    private JTextField titleTextField;
    private JLabel backgroundPathLabel;
    private String selectedBackgroundPath = null;
    
    // 预览面板
    private PreviewPanel previewPanel;
    
    // 字体设置
    private Color fontColor = Color.WHITE;
    private JSpinner fontSizeSpinner;
    
    // 位置调整 - 只需要垂直位置（水平总是居中）
    private JSpinner verticalSpinner;
    
    // 拼音设置
    private JCheckBox enablePinyinCheckBox;
    
    public LyricsToSlidesApp() {
        setTitle("歌词转PPT工具 - 增强版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 800);
        setLocationRelativeTo(null);
        
        // 创建主面板 - 使用分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(750);
        splitPane.setResizeWeight(0.55);
        
        // 左侧面板 - 控制区
        JPanel leftPanel = createLeftPanel();
        splitPane.setLeftComponent(leftPanel);
        
        // 右侧面板 - 预览区
        JPanel rightPanel = createRightPanel();
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane);
        
        // 初始预览
        updatePreview();
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 顶部面板 - 标题和设置
        JPanel topPanel = createTopPanel();
        panel.add(topPanel, BorderLayout.NORTH);
        
        // 中间面板 - 歌词输入区
        JPanel centerPanel = createCenterPanel();
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // 底部面板 - 按钮和状态
        JPanel bottomPanel = createBottomPanel();
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 标题
        JLabel titleLabel = new JLabel("实时预览 (1280x720 @ 60%)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 预览面板容器 - 用于居中显示
        JPanel previewContainer = new JPanel(new GridBagLayout());
        previewContainer.setBackground(Color.DARK_GRAY);
        
        // 预览面板 - 固定尺寸1280x720
        previewPanel = new PreviewPanel();
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        
        previewContainer.add(previewPanel);
        
        JScrollPane scrollPane = new JScrollPane(previewContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 8, 8));
        
        // 标题输入
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("PPT标题：");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        titlePanel.add(titleLabel);
        titleTextField = new JTextField(25);
        titleTextField.setText("歌词展示");
        titleTextField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        titlePanel.add(titleTextField);
        panel.add(titlePanel);
        
        // 每页行数设置
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel linesLabel = new JLabel("每张幻灯片行数：");
        linesLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        settingsPanel.add(linesLabel);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(2, 1, 50, 1);
        linesPerSlideSpinner = new JSpinner(spinnerModel);
        ((JSpinner.DefaultEditor) linesPerSlideSpinner.getEditor()).getTextField().setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        linesPerSlideSpinner.setPreferredSize(new Dimension(80, 30));
        linesPerSlideSpinner.addChangeListener(e -> updatePreview());
        settingsPanel.add(linesPerSlideSpinner);
        panel.add(settingsPanel);
        
        // 背景图片选择
        JPanel backgroundPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel bgLabel = new JLabel("背景图片：");
        bgLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        backgroundPanel.add(bgLabel);
        
        selectBackgroundButton = new JButton("选择图片");
        selectBackgroundButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        selectBackgroundButton.addActionListener(new SelectBackgroundListener());
        backgroundPanel.add(selectBackgroundButton);
        
        clearBackgroundButton = new JButton("使用默认");
        clearBackgroundButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        clearBackgroundButton.addActionListener(e -> {
            selectedBackgroundPath = null;
            updateBackgroundLabel();
            updatePreview();
        });
        backgroundPanel.add(clearBackgroundButton);
        
        backgroundPathLabel = new JLabel("(未选择)");
        backgroundPathLabel.setForeground(Color.GRAY);
        backgroundPathLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        backgroundPanel.add(backgroundPathLabel);
        
        panel.add(backgroundPanel);
        
        // 字体设置面板
        JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel fontSizeLabel = new JLabel("字体大小：");
        fontSizeLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        fontPanel.add(fontSizeLabel);
        
        SpinnerNumberModel fontSizeModel = new SpinnerNumberModel(90.0, 1.0, 500.0, 1.0);
        fontSizeSpinner = new JSpinner(fontSizeModel);
        ((JSpinner.DefaultEditor) fontSizeSpinner.getEditor()).getTextField().setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        fontSizeSpinner.setPreferredSize(new Dimension(80, 30));
        fontSizeSpinner.addChangeListener(e -> updatePreview());
        fontPanel.add(fontSizeSpinner);
        
        JLabel colorLabel = new JLabel("   字体颜色：");
        colorLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        fontPanel.add(colorLabel);
        fontColorButton = new JButton("    ");
        fontColorButton.setBackground(fontColor);
        fontColorButton.setPreferredSize(new Dimension(60, 30));
        fontColorButton.addActionListener(new FontColorListener());
        fontPanel.add(fontColorButton);
        
        panel.add(fontPanel);
        
        // 位置调整面板 - 只需要垂直位置
        JPanel positionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel vPosLabel = new JLabel("垂直位置（上下调整）：");
        vPosLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        positionPanel.add(vPosLabel);
        
        SpinnerNumberModel vModel = new SpinnerNumberModel(50, -200, 500, 10);
        verticalSpinner = new JSpinner(vModel);
        ((JSpinner.DefaultEditor) verticalSpinner.getEditor()).getTextField().setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        verticalSpinner.setPreferredSize(new Dimension(80, 30));
        verticalSpinner.addChangeListener(e -> updatePreview());
        positionPanel.add(verticalSpinner);
        
        JLabel hintLabel = new JLabel("  (水平自动居中)");
        hintLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        hintLabel.setForeground(Color.GRAY);
        positionPanel.add(hintLabel);
        
        panel.add(positionPanel);
        
        // 拼音设置面板
        JPanel pinyinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        enablePinyinCheckBox = new JCheckBox("自动添加拼音");
        enablePinyinCheckBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        enablePinyinCheckBox.setSelected(true);
        enablePinyinCheckBox.addActionListener(e -> updatePreview());
        pinyinPanel.add(enablePinyinCheckBox);
        
        JLabel pinyinHint = new JLabel("  (拼音会显示在汉字上方)");
        pinyinHint.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        pinyinHint.setForeground(Color.GRAY);
        pinyinPanel.add(pinyinHint);
        
        panel.add(pinyinPanel);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JLabel label = new JLabel("请输入歌词（每行一句）：");
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        panel.add(label, BorderLayout.NORTH);
        
        lyricsTextArea = new JTextArea();
        lyricsTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        lyricsTextArea.setLineWrap(true);
        lyricsTextArea.setWrapStyleWord(true);
        
        // 添加文本变化监听器，实时更新预览
        lyricsTextArea.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { updatePreview(); }
            public void removeUpdate(DocumentEvent e) { updatePreview(); }
            public void insertUpdate(DocumentEvent e) { updatePreview(); }
        });
        
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
        generateButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        generateButton.setPreferredSize(new Dimension(140, 40));
        generateButton.addActionListener(new GenerateButtonListener());
        buttonPanel.add(generateButton);
        
        clearButton = new JButton("清空");
        clearButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        clearButton.setPreferredSize(new Dimension(110, 40));
        clearButton.addActionListener(e -> {
            lyricsTextArea.setText("");
            statusLabel.setText("已清空");
        });
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        // 状态标签
        statusLabel = new JLabel("准备就绪 - 修改任何设置将自动更新预览");
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
            if (fileName.length() > 30) {
                fileName = fileName.substring(0, 27) + "...";
            }
            backgroundPathLabel.setText("已选择: " + fileName);
            backgroundPathLabel.setForeground(new Color(0, 128, 0));
        } else {
            backgroundPathLabel.setText("(未选择)");
            backgroundPathLabel.setForeground(Color.GRAY);
        }
    }
    
    /**
     * 更新预览 - 自动实时更新
     */
    private void updatePreview() {
        String lyrics = lyricsTextArea.getText().trim();
        int linesPerSlide = (Integer) linesPerSlideSpinner.getValue();
        boolean enablePinyin = enablePinyinCheckBox.isSelected();
        
        // 获取第一组歌词
        String[] lines = lyrics.split("\n");
        StringBuilder previewLyrics = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && count < linesPerSlide) {
                if (count > 0) previewLyrics.append("\n");
                previewLyrics.append(trimmed);
                count++;
            }
        }
        
        // 如果启用拼音，处理歌词
        String displayLyrics = previewLyrics.toString();
        if (enablePinyin && !displayLyrics.isEmpty()) {
            displayLyrics = PinyinUtil.addPinyinToLyrics(displayLyrics);
        }
        
        // 更新预览面板
        previewPanel.setBackgroundImage(selectedBackgroundPath);
        previewPanel.setLyrics(displayLyrics);
        previewPanel.setFontColor(fontColor);
        previewPanel.setFontSize(((Number) fontSizeSpinner.getValue()).doubleValue());
        previewPanel.setVerticalPosition((Integer) verticalSpinner.getValue());
        previewPanel.repaint();
    }
    
    /**
     * 预览面板类 - 1:1 真实尺寸预览
     */
    private class PreviewPanel extends JPanel {
        private BufferedImage backgroundImage;
        private String lyrics = "";
        private Color textColor = Color.WHITE;
        private double fontSize = 48.0;
        private int verticalPos = 100;  // 默认中上位置
        
        public PreviewPanel() {
            // 设置为缩小的预览尺寸 (原始1280x720的60%)
            setPreferredSize(new Dimension(768, 432));
            setMinimumSize(new Dimension(768, 432));
            setMaximumSize(new Dimension(768, 432));
            setBackground(new Color(30, 30, 35));
        }
        
        public void setBackgroundImage(String imagePath) {
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    backgroundImage = ImageIO.read(new File(imagePath));
                } catch (Exception e) {
                    backgroundImage = null;
                }
            } else {
                backgroundImage = null;
            }
        }
        
        public void setLyrics(String lyrics) {
            this.lyrics = lyrics;
        }
        
        public void setFontColor(Color color) {
            this.textColor = color;
        }
        
        public void setFontSize(double size) {
            this.fontSize = size;
        }
        
        public void setVerticalPosition(int pos) {
            this.verticalPos = pos;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // 原始PPT尺寸
            int originalWidth = 1280;
            int originalHeight = 720;
            
            // 预览面板尺寸
            int previewWidth = getWidth();
            int previewHeight = getHeight();
            
            // 缩放比例（0.6）
            double scale = (double) previewWidth / originalWidth;
            
            // 绘制背景
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, previewWidth, previewHeight, this);
            } else {
                g2d.setColor(new Color(30, 30, 35));
                g2d.fillRect(0, 0, previewWidth, previewHeight);
            }
            
            // 绘制歌词
            if (!lyrics.isEmpty()) {
                g2d.setColor(textColor);
                
                String[] lines = lyrics.split("\n");
                
                // 按比例缩放垂直位置
                int scaledVerticalPos = (int) (verticalPos * scale);
                int y = scaledVerticalPos;
                
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    
                    // 判断是否是中文行
                    boolean hasChinese = false;
                    for (char c : line.toCharArray()) {
                        if (c >= 0x4E00 && c <= 0x9FA5) {
                            hasChinese = true;
                            break;
                        }
                    }
                    
                    // 设置字体
                    Font currentFont;
                    if (hasChinese) {
                        // 中文行 - 使用正常字体大小
                        currentFont = new Font("Microsoft YaHei", Font.PLAIN, (int) (fontSize * scale));
                    } else {
                        // 拼音行 - 使用60%字体大小，Arial字体
                        currentFont = new Font("Arial", Font.PLAIN, (int) (fontSize * 0.6 * scale));
                    }
                    g2d.setFont(currentFont);
                    FontMetrics fm = g2d.getFontMetrics();
                    
                    // 每行都需要加上ascent来得到基线位置
                    y += fm.getAscent();
                    
                    // 居中对齐
                    int textWidth = fm.stringWidth(line);
                    int x = (previewWidth - textWidth) / 2;
                    
                    g2d.drawString(line, x, y);
                    
                    // 移动到下一行的顶部
                    // 先加上descent（从基线到底部的距离）
                    y += fm.getDescent();
                    
                    // 如果是拼音行且不是最后一行，添加额外的段后间距
                    if (!hasChinese && i < lines.length - 1) {
                        y += (int) (fontSize * 0.3 * scale);
                    }
                }
            } else {
                // 显示提示文字
                g2d.setColor(Color.GRAY);
                g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, (int) (24 * scale)));
                String hint = "输入歌词后将自动显示预览";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (previewWidth - fm.stringWidth(hint)) / 2;
                int y = previewHeight / 2;
                g2d.drawString(hint, x, y);
            }
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
            
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "图片文件 (*.jpg, *.jpeg, *.png, *.gif, *.bmp)", 
                "jpg", "jpeg", "png", "gif", "bmp"
            );
            fileChooser.setFileFilter(filter);
            
            if (selectedBackgroundPath != null) {
                fileChooser.setCurrentDirectory(new File(selectedBackgroundPath).getParentFile());
            }
            
            int result = fileChooser.showOpenDialog(LyricsToSlidesApp.this);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                selectedBackgroundPath = selectedFile.getAbsolutePath();
                updateBackgroundLabel();
                updatePreview();
                statusLabel.setText("背景图片已选择: " + selectedFile.getName());
            }
        }
    }
    
    /**
     * 字体颜色选择监听器
     */
    private class FontColorListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Color newColor = JColorChooser.showDialog(
                LyricsToSlidesApp.this,
                "选择字体颜色",
                fontColor
            );
            
            if (newColor != null) {
                fontColor = newColor;
                fontColorButton.setBackground(fontColor);
                updatePreview();
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
                
                if (!filePath.toLowerCase().endsWith(".pptx")) {
                    filePath += ".pptx";
                    fileToSave = new File(filePath);
                }
                
                final String finalFilePath = filePath;
                final File finalFileToSave = fileToSave;
                
                generateButton.setEnabled(false);
                statusLabel.setText("正在生成PPT...");
                
                new Thread(() -> {
                    try {
                        String title = titleTextField.getText().trim();
                        int linesPerSlide = (Integer) linesPerSlideSpinner.getValue();
                        double fontSize = ((Number) fontSizeSpinner.getValue()).doubleValue();
                        int vPos = (Integer) verticalSpinner.getValue();
                        boolean enablePinyin = enablePinyinCheckBox.isSelected();
                        
                        // 如果启用拼音，处理歌词
                        String processedLyrics = lyrics;
                        if (enablePinyin) {
                            processedLyrics = PinyinUtil.addPinyinToLyrics(lyrics);
                        }
                        
                        PowerPointGenerator generator = new PowerPointGenerator();
                        
                        // 设置所有自定义参数
                        if (selectedBackgroundPath != null) {
                            generator.setBackgroundImage(selectedBackgroundPath);
                        }
                        generator.setFontColor(fontColor);
                        generator.setFontSize(fontSize);
                        generator.setTextPosition(0, vPos);  // 水平位置始终为0（居中）
                        
                        generator.createPresentation(processedLyrics, title, linesPerSlide, finalFilePath);
                        
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