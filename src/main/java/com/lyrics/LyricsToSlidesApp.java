package com.lyrics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

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
    private JComboBox<String> chineseFontComboBox;  // 中文字体选择
    private JComboBox<String> pinyinFontComboBox;   // 拼音字体选择
    private String chineseFont = "Microsoft YaHei";
    private String pinyinFont = "Arial Black";
    
    // 位置调整 - 只需要垂直位置（水平总是居中）
    private JSpinner verticalSpinner;
    
    // 拼音设置
    private JCheckBox enablePinyinCheckBox;
    
    // 背景亮度调节
    private JSlider brightnessSlider;
    private float backgroundBrightness = 1.0f;  // 默认100%亮度
    
    public LyricsToSlidesApp() {
        setTitle("歌词转PPT工具");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 800);
        setLocationRelativeTo(null);
        
        // 添加菜单栏
        createMenuBar();
        
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
        JLabel titleLabel = new JLabel("实时预览", SwingConstants.CENTER);
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
        JPanel panel = new JPanel(new GridLayout(8, 1, 8, 8));
        
        // 标题输入
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("歌名：");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        titlePanel.add(titleLabel);
        titleTextField = new JTextField(25);
        titleTextField.setText("");
        titleTextField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        titlePanel.add(titleTextField);
        panel.add(titlePanel);
        
        // 每页行数设置
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel linesLabel = new JLabel("每张幻灯片行数：");
        linesLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        settingsPanel.add(linesLabel);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(2, 1, 10, 1);
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
        
        // 背景亮度调节
        JPanel brightnessPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel brightnessLabel = new JLabel("背景亮度：");
        brightnessLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        brightnessPanel.add(brightnessLabel);
        
        brightnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
        brightnessSlider.setPreferredSize(new Dimension(200, 45));
        brightnessSlider.setMajorTickSpacing(25);
        brightnessSlider.setMinorTickSpacing(5);
        brightnessSlider.setPaintTicks(true);
        brightnessSlider.setPaintLabels(true);
        brightnessSlider.addChangeListener(e -> {
            backgroundBrightness = brightnessSlider.getValue() / 100.0f;
            updatePreview();
        });
        brightnessPanel.add(brightnessSlider);
        
        JLabel brightnessHint = new JLabel("(调暗背景可让歌词更清晰)");
        brightnessHint.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        brightnessHint.setForeground(Color.GRAY);
        brightnessPanel.add(brightnessHint);
        
        panel.add(brightnessPanel);

                // 字体选择面板
        JPanel fontSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        // 中文字体
        JLabel chineseFontLabel = new JLabel("中文字体：");
        chineseFontLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        fontSelectionPanel.add(chineseFontLabel);
        
        String[] chineseFonts = {
            "Microsoft YaHei", "SimSun", "SimHei", "KaiTi", "FangSong",
            "Microsoft JhengHei", "STSong", "STHeiti", "STKaiti"
        };
        
        chineseFontComboBox = new JComboBox<>(chineseFonts);
        chineseFontComboBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        chineseFontComboBox.setPreferredSize(new Dimension(145, 30));
        chineseFontComboBox.addActionListener(e -> {
            chineseFont = (String) chineseFontComboBox.getSelectedItem();
            updatePreview();
        });
        fontSelectionPanel.add(chineseFontComboBox);
        
        fontSelectionPanel.add(Box.createHorizontalStrut(20));
        
        // 拼音字体
        JLabel pinyinFontLabel = new JLabel("拼音字体：");
        pinyinFontLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        fontSelectionPanel.add(pinyinFontLabel);
        
        String[] pinyinFonts = {
            "Arial Black","Arial", "Calibri", "Times New Roman", "Georgia", 
            "Verdana", "Tahoma", "Courier New", "Comic Sans MS"
        };
        
        pinyinFontComboBox = new JComboBox<>(pinyinFonts);
        pinyinFontComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        pinyinFontComboBox.setPreferredSize(new Dimension(145, 30));
        pinyinFontComboBox.addActionListener(e -> {
            pinyinFont = (String) pinyinFontComboBox.getSelectedItem();
            updatePreview();
        });
        fontSelectionPanel.add(pinyinFontComboBox);
        
        panel.add(fontSelectionPanel);
        
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
        
        JLabel pinyinHint = new JLabel("  (拼音会显示在汉字下方)");
        pinyinHint.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        pinyinHint.setForeground(Color.GRAY);
        pinyinPanel.add(pinyinHint);
        
        panel.add(pinyinPanel);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // 标题栏（包含标签和帮助按钮）
        JPanel titlePanel = new JPanel(new BorderLayout());
        
        JLabel label = new JLabel("请输入歌词（每行一句）：");
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        titlePanel.add(label, BorderLayout.WEST);
        
        // 帮助按钮
        JButton helpButton = new JButton("如何输入歌词?");
        helpButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        helpButton.setFocusPainted(false);
        helpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpButton.addActionListener(e -> showLyricsFormatHelp());
        titlePanel.add(helpButton, BorderLayout.EAST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        lyricsTextArea = new JTextArea();
        lyricsTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        lyricsTextArea.setLineWrap(true);
        lyricsTextArea.setWrapStyleWord(true);
        lyricsTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openFullEditor();
                }
            }
        });
        
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

    private void openFullEditor() {
        // 创建模态对话框
        JDialog dialog = new JDialog(this, "编辑歌词", true);
        dialog.setSize(800, 800);
        dialog.setLocationRelativeTo(this); // 居中显示
        dialog.setLayout(new BorderLayout());

        // ===== 大文本编辑区 =====
        JTextArea largeTextArea = new JTextArea(lyricsTextArea.getText());
        largeTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
        largeTextArea.setLineWrap(true);
        largeTextArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(largeTextArea);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // ===== 底部按钮 =====
        JButton saveButton = new JButton("确定");
        saveButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        saveButton.addActionListener(e -> {
            lyricsTextArea.setText(largeTextArea.getText());
            dialog.dispose();
        });

        // 居中放置按钮
        JPanel buttonPanel = new JPanel(); // 默认 FlowLayout 居中
        buttonPanel.add(saveButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // ===== ESC 关闭 =====
        largeTextArea.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            "exit"
        );
        largeTextArea.getActionMap().put("exit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);

        // 自动滚动到顶部
        SwingUtilities.invokeLater(() -> {
            lyricsTextArea.setCaretPosition(0);
        });
    }


        
    /**
     * 显示歌词格式帮助对话框
     */
    private void showLyricsFormatHelp() {
        JDialog helpDialog = new JDialog(this, "歌词输入格式说明", true);
        helpDialog.setSize(750, 650);
        helpDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建带格式的帮助文本
        JTextPane helpText = new JTextPane();
        helpText.setContentType("text/html");
        helpText.setEditable(false);
        helpText.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        
        String helpContent = 
            "<html><body style='font-family: Microsoft YaHei; font-size: 14px; padding: 15px;'>" +
            "<h2 style='color: #2c3e50;'>📝 歌词输入格式说明</h2>" +
                        
            "<hr style='margin: 15px 0;'>" +
            
            "<h3 style='color: #3498db;'>1. 标签</h3>" +
            "<p>标签用于标记歌曲结构。</p>" +
            
            "<table width='100%' style='margin: 10px 0; table-layout: fixed;'>" +
            "<tr>" +
            "<td width='50%' style='vertical-align: top; padding: 0 10px;'>" +
            "<div style='background-color: #fff3cd; padding: 15px; border-radius: 5px; height: 100%; box-sizing: border-box;'>" +
            "<b>你输入的内容：</b><br><br>" +
            "<code style='font-size: 13px;'>" +
            "V1 我曾经跨过山和大海<br>" +
            "也穿过人山人海<br>" +
            "<br>" +
            "<br>" +
            "</code>" +
            "</div>" +
            "</td>" +
            "<td width='50%' style='vertical-align: top; text-align: center; padding: 0 10px;'>" +
            "<div style='background-color: #d4edda; padding: 15px; border-radius: 5px; height: 100%; box-sizing: border-box;'>" +
            "<b>PPT中显示：</b><br><br>" +
            "<code style='font-size: 13px;'>" +
            "我曾经跨过山和大海<br>" +
            "也穿过人山人海<br>" +
            "<br>" +
            "V1<br>" +
            "</code>" +
            "</div>" +
            "</td>" +
            "</tr>" +
            "</table>" +
            
            "<p style='font-size: 12px; color: #666;'>常用标签：V / V1 / V2 <code>(Verse)</code>  " +
            "C / C1 / C2 <code>(Chorus)</code>  " +
            "B <code>(Bridge)</code> L <code>(Last)</code></p>" +
            
            "<hr style='margin: 20px 0;'>" +
            
            "<h3 style='color: #3498db;'>2. 分页符 /（控制强制分页）</h3>" +
            "<p>在<b>行末</b>添加 <code>/</code> 会在该行后强制换页。</p>" +
            
            "<table width='100%' style='margin: 10px 0;'>" +
            "<tr>" +
            "<td width='50%' style='vertical-align: top; padding-right: 20px;'>" +
            "<div style='background-color: #fff3cd; padding: 15px; border-radius: 5px;'>" +
            "<b>你输入的内容：</b><br><br>" +
            "<code style='font-size: 13px;'>" +
            "我曾经跨过山和大海<br>" +
            "也穿过人山人海<br>" +
            "我曾经拥有着的一切<span style='background: yellow; font-weight: bold;'>/</span><br>" +
            "转眼都飘散如烟<br>" +
            "我曾经失落失望<br>" +
            "<br>" +
            "<br>" +
            "<br>" +
            "<br>" +
            "<br>" +
            "<br>" +
            "<br>" +
            "<br>" +
            "<br>" +
            "</code>" +
            "</div>" +
            "</td>" +
            "<td width='50%' style='vertical-align: top;'>" +
            "<div style='background-color: #d4edda; padding: 15px; border-radius: 5px;'>" +
            "<b>PPT生成结果：</b><br><br>" +
            
            "<div style='border: 2px solid #3498db; padding: 10px; margin-bottom: 10px; background: white;'>" +
            "<b style='color: #3498db;'>📄 第1页</b><br>" +
            "<code style='font-size: 13px;'>" +
            "我曾经跨过山和大海<br>" +
            "也穿过人山人海" +
            "</code>" +
            "</div>" +
            
            "<div style='border: 2px solid #3498db; padding: 10px; margin-bottom: 10px; background: white;'>" +
            "<b style='color: #3498db;'>📄 第2页</b><br>" +
            "<code style='font-size: 13px;'>" +
            "我曾经拥有着的一切<br>" +
            "</code>" +
            "<small style='color: #e74c3c;'>行末有/，强制分页</small>" +
            "</div>" +
            
            "<div style='border: 2px solid #3498db; padding: 10px; background: white;'>" +
            "<b style='color: #3498db;'>📄 第3页</b><br>" +
            "<code style='font-size: 13px;'>" +
            "转眼都飘散如烟<br>" +
            "我曾经失落失望" +
            "</code>" +
            "</div>" +
            
            "</div>" +
            "</td>" +
            "</tr>" +
            "</table>" +            
            
            "<hr style='margin: 20px 0;'>" +
            
            "<div style='background-color: #e8f5e9; padding: 15px; border-left: 4px solid #4caf50; margin: 15px 0;'>" +
            "<h4 style='margin-top: 0; color: #2e7d32;'>💡 关键要点</h4>" +
            "<ul style='margin-bottom: 0;'>" +
            "<li><b>每输入一行 = PPT中一行</b></li>" +
            "<li><b>标签在行首</b>，格式：<code>V1 歌词内容</code></li>" +
            "<li><b>分页符在行末</b>，格式：<code>歌词内容/</code></li>" +
            "</ul>" +
            "</div>" +
            
            "</body></html>";
        
        helpText.setText(helpContent);
        helpText.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(helpText);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("我明白了");
        closeButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        closeButton.setPreferredSize(new Dimension(120, 40));
        closeButton.addActionListener(e -> helpDialog.dispose());
        buttonPanel.add(closeButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        helpDialog.add(mainPanel);
        helpDialog.setVisible(true);
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        // 加载歌词按钮
        JButton loadButton = new JButton("加载歌词");
        loadButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        loadButton.setPreferredSize(new Dimension(120, 40));
        loadButton.addActionListener(e -> loadLyricsFromLibrary());
        buttonPanel.add(loadButton);
        
        // 保存歌词按钮
        JButton saveButton = new JButton("保存歌词");
        saveButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        saveButton.setPreferredSize(new Dimension(120, 40));
        saveButton.addActionListener(e -> saveLyricsToLibrary());
        buttonPanel.add(saveButton);
        
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
    
        private void updatePreview() {
        String lyrics = lyricsTextArea.getText().trim();
        int linesPerSlide = (Integer) linesPerSlideSpinner.getValue();
        boolean enablePinyin = enablePinyinCheckBox.isSelected();
        
        String[] lines = lyrics.split("\n");
        
        // 1. 先获取第一组歌词（考虑/分隔符）
        StringBuilder previewLyrics = new StringBuilder();
        ArrayList<String> firstSlideLines = new ArrayList<>();
        int count = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && count < linesPerSlide) {
                firstSlideLines.add(trimmed);
                count++;
                if (trimmed.endsWith("/")) break;
            }
        }
        
        // 2. 在第一组中检查标签（只检查会显示的这几行）
        String sectionLabel = null;
        for (int i = 0; i < firstSlideLines.size(); i++) {
            String line = firstSlideLines.get(i);
            if (line.length() > 0) {
                char firstChar = line.charAt(0);
                if (firstChar == 'C' || firstChar == 'B' || firstChar == 'L' || firstChar == 'V') {
                    int j = 1;
                    while (j < line.length() && Character.isDigit(line.charAt(j))) {
                        j++;
                    }
                    if (j < line.length() && line.charAt(j) == ' ') {
                        sectionLabel = line.substring(0, j);
                        // 移除标记
                        String remaining = line.substring(j + 1).trim();
                        firstSlideLines.set(i, remaining);
                        break; // 找到第一个标记就停止
                    }
                }
            }
        }
        
        // 3. 组合第一组的歌词（移除/符号）
        for (int i = 0; i < firstSlideLines.size(); i++) {
            String line = firstSlideLines.get(i);
            String displayLine = line.endsWith("/") ? line.substring(0, line.length() - 1).trim() : line;
            if (i > 0) previewLyrics.append("\n");
            previewLyrics.append(displayLine);
        }
        
        // 4. 如果启用拼音，处理歌词
        String displayLyrics = previewLyrics.toString();
        if (enablePinyin && !displayLyrics.isEmpty()) {
            displayLyrics = PinyinUtil.addPinyinToLyrics(displayLyrics);
        }
        
        // 5. 更新预览面板（包括标记）
        previewPanel.setBackgroundImage(selectedBackgroundPath);
        previewPanel.setLyrics(displayLyrics);
        previewPanel.setSectionLabel(sectionLabel);
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
        private String sectionLabel = null;  // 段落标记
        
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
        
        public void setSectionLabel(String label) {
            this.sectionLabel = label;
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
                // 应用亮度调整
                if (backgroundBrightness < 1.0f) {
                    // 先绘制原图
                    g2d.drawImage(backgroundImage, 0, 0, previewWidth, previewHeight, this);
                    // 再覆盖一层半透明黑色来降低亮度
                    float darkness = 1.0f - backgroundBrightness;
                    g2d.setColor(new Color(0, 0, 0, (int)(darkness * 255)));
                    g2d.fillRect(0, 0, previewWidth, previewHeight);
                } else {
                    // 原始亮度，直接绘制
                    g2d.drawImage(backgroundImage, 0, 0, previewWidth, previewHeight, this);
                }
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
                        // 中文行 - 使用正常字体大小和用户选择的中文字体
                        currentFont = new Font(chineseFont, Font.BOLD, (int) (fontSize * scale));
                    } else {
                        // 拼音行 - 使用50%字体大小和用户选择的拼音字体
                        currentFont = new Font(pinyinFont, Font.BOLD, (int) (fontSize * 0.5 * scale));
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
            
            // 绘制段落标记（如果有）
            if (sectionLabel != null && !sectionLabel.isEmpty()) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, (int) (60 * scale)));
                FontMetrics fm = g2d.getFontMetrics();
                
                int labelWidth = fm.stringWidth(sectionLabel);
                int labelX = (previewWidth - labelWidth) / 2;
                int labelY = (int) (580 * scale) + fm.getAscent();
                
                g2d.drawString(sectionLabel, labelX, labelY);
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
            } else {
                File backgroundDir = new File(System.getProperty("user.dir"), "Background");

                if (!backgroundDir.exists()) {
                    backgroundDir.mkdirs();
                }

                fileChooser.setCurrentDirectory(backgroundDir);
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
            
                        // 使用歌名作为默认文件名
            String defaultFileName = titleTextField.getText().trim();
            if (defaultFileName.isEmpty()) {
                defaultFileName = "歌词展示";
            }
            fileChooser.setSelectedFile(new File(defaultFileName + ".pptx"));
                        
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
                        
                        PowerPointGenerator generator = new PowerPointGenerator();
                        
                        // 设置所有自定义参数
                        if (selectedBackgroundPath != null) {
                            generator.setBackgroundImage(selectedBackgroundPath);
                        }
                        generator.setFontColor(fontColor);
                        generator.setFontSize(fontSize);
                        generator.setTextPosition(0, vPos);  // 水平位置始终为0（居中）
                        generator.setChineseFont(chineseFont);  // 设置中文字体
                        generator.setPinyinFont(pinyinFont);    // 设置拼音字体
                        generator.setBackgroundBrightness(backgroundBrightness);  // 设置背景亮度
                        
                        // 传递原始歌词和enablePinyin参数
                        generator.createPresentation(lyrics, title, linesPerSlide, finalFilePath, enablePinyin);
                        
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
    
    /**
     * 保存歌词到库
     */
    private void saveLyricsToLibrary() {
        String lyrics = lyricsTextArea.getText().trim();
        
        if (lyrics.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "请先输入歌词！",
                "提示",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // 直接使用歌名输入框的内容作为文件名
        String songName = titleTextField.getText().trim();
        
        if (songName.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "请先输入歌名！",
                "提示",
                JOptionPane.WARNING_MESSAGE
            );
            titleTextField.requestFocus();
            return;
        }
        
        // 检查是否已存在
        if (LyricsLibrary.exists(songName)) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "歌曲《" + songName + "》已存在！\n是否覆盖？",
                "确认覆盖",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return; // 用户选择不覆盖
            }
        }
        
        try {
            LyricsLibrary.saveLyrics(songName, lyrics);
            statusLabel.setText("歌词已保存：" + songName);
            statusLabel.setForeground(new Color(0, 128, 0));
            
            JOptionPane.showMessageDialog(
                this,
                "歌词保存成功！",
                "成功",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "保存失败：" + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * 从库加载歌词
     */
    private void loadLyricsFromLibrary() {
        LyricsLibraryDialog dialog = new LyricsLibraryDialog(this);
        dialog.setVisible(true);
        
        String selectedSong = dialog.getSelectedSong();
        String selectedLyrics = dialog.getSelectedLyrics();
        
        if (selectedSong != null && selectedLyrics != null) {
            lyricsTextArea.setText(selectedLyrics);
            titleTextField.setText(selectedSong);
            statusLabel.setText("已加载：" + selectedSong);
            statusLabel.setForeground(new Color(0, 128, 0));
            
            // 自动滚动到顶部
            SwingUtilities.invokeLater(() -> {
                lyricsTextArea.setCaretPosition(0);
            });
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            ConfigManager.loadConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LyricsToSlidesApp app = new LyricsToSlidesApp();
            app.setVisible(true);
        });
    }

    /**
     * 创建菜单栏
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 歌词库菜单
        JMenu libraryMenu = new JMenu("歌词库");
        libraryMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        
        JMenuItem onlineSettingsItem = new JMenuItem("在线存储设置");
        onlineSettingsItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        onlineSettingsItem.addActionListener(e -> {
            OnlineStorageDialog dialog = new OnlineStorageDialog(this);
            dialog.setVisible(true);
        });
        libraryMenu.add(onlineSettingsItem);
        
        libraryMenu.addSeparator();
        
        JMenuItem syncItem = new JMenuItem("↓ 从在线库同步到本地");
        syncItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        syncItem.addActionListener(e -> syncFromOnline());
        libraryMenu.add(syncItem);
        
        JMenuItem uploadItem = new JMenuItem("↑ 从本地上传到在线");
        uploadItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        uploadItem.addActionListener(e -> uploadToOnline());
        libraryMenu.add(uploadItem);
        
        menuBar.add(libraryMenu);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * 从在线同步到本地
     */
    private void syncFromOnline() {
        if (!GitHubLyricsLibrary.isConfigured()) {
            JOptionPane.showMessageDialog(this,
                "请先配置在线存储！\n点击菜单：歌词库 → 在线存储设置",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要从在线库同步到本地吗？\n本地已存在的同名歌曲将被覆盖。",
            "确认同步",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            int count = GitHubLyricsLibrary.exportToLocal();
            JOptionPane.showMessageDialog(this,
                "成功同步 " + count + " 首歌曲到本地！",
                "同步完成",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "同步失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 从本地上传到在线
     */
    private void uploadToOnline() {
        if (!GitHubLyricsLibrary.isConfigured()) {
            JOptionPane.showMessageDialog(this,
                "请先配置在线存储！\n点击菜单：歌词库 → 在线存储设置",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要将所有本地歌词上传到在线库吗？\n这可能需要一些时间。",
            "确认上传",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            int count = GitHubLyricsLibrary.importFromLocal();
            JOptionPane.showMessageDialog(this,
                "成功上传 " + count + " 首歌曲到在线库！",
                "上传完成",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "上传失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 显示关于信息
     */
    private void showAbout() {
        String aboutText = 
            "歌词转PPT工具 v1.0\n\n" +
            "功能特性:\n" +
            "• 本地歌词库管理\n" +
            "• GitHub Gist在线存储\n" +
            "• 拼音注音支持\n" +
            "• 实时预览\n" +
            "• 自定义字体和颜色\n" +
            "• 背景图片支持\n\n" +
            "Author: Daniel\n" +
            "© 2026 歌词转PPT工具";
        
        JOptionPane.showMessageDialog(this,
            aboutText,
            "关于",
            JOptionPane.INFORMATION_MESSAGE);
    }
}