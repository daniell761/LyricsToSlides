package com.lyrics;

import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.PictureData;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class PowerPointGenerator {
    
    private String backgroundImagePath;
    private Color fontColor = Color.WHITE;
    private double fontSize = 48.0;
    private int textHorizontalPosition = 0;  // 不再使用，保持为0
    private int textVerticalPosition = 100;  // 默认中上位置
    private String chineseFont = "Microsoft YaHei";  // 中文字体
    private String pinyinFont = "Arial Black";              // 拼音字体
    
    /**
     * 设置背景图片路径
     * @param imagePath 图片文件路径
     */
    public void setBackgroundImage(String imagePath) {
        this.backgroundImagePath = imagePath;
    }
    
    /**
     * 设置字体颜色
     * @param color 字体颜色
     */
    public void setFontColor(Color color) {
        this.fontColor = color;
    }
    
    /**
     * 设置字体大小
     * @param size 字体大小
     */
    public void setFontSize(double size) {
        this.fontSize = size;
    }
    
    /**
     * 设置文本位置
     * @param horizontal 水平位置
     * @param vertical 垂直位置
     */
    public void setTextPosition(int horizontal, int vertical) {
        this.textHorizontalPosition = horizontal;
        this.textVerticalPosition = vertical;
    }
    
    /**
     * 设置中文字体
     * @param font 字体名称
     */
    public void setChineseFont(String font) {
        this.chineseFont = font;
    }
    
    /**
     * 设置拼音字体
     * @param font 字体名称
     */
    public void setPinyinFont(String font) {
        this.pinyinFont = font;
    }
    
    /**
     * 创建PowerPoint演示文稿
     * 
     * @param lyrics 歌词文本
     * @param title PPT标题
     * @param linesPerSlide 每张幻灯片的行数
     * @param outputPath 输出文件路径
     * @throws IOException 文件操作异常
     */
    public void createPresentation(String lyrics, String title, int linesPerSlide, String outputPath) 
            throws IOException {
        
        // 创建PPT对象
        XMLSlideShow ppt = new XMLSlideShow();
        
        // 设置页面大小（16:9）
        ppt.setPageSize(new Dimension(1280, 720));
        
        // 创建标题页
        createTitleSlide(ppt, title);
        
        // 分割歌词
        List<String> lines = splitLyrics(lyrics);
        
        // 将歌词分组
        List<List<String>> groups = groupLines(lines, linesPerSlide);
        
        // 为每组创建幻灯片
        for (List<String> group : groups) {
            createLyricsSlide(ppt, group);
        }
        
        // 创建结束页
        //createEndSlide(ppt);
        
        // 保存文件
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            ppt.write(out);
        }
        
        ppt.close();
    }
    
    /**
     * 创建标题页
     */
    private void createTitleSlide(XMLSlideShow ppt, String title) {
        XSLFSlide slide = ppt.createSlide();
        
        // 设置背景
        setSlideBackgroundWithImage(ppt, slide, new Color(45, 45, 48));
        
        // 添加标题
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle(100, 250, 1080, 200));
        
        XSLFTextParagraph titlePara = titleBox.addNewTextParagraph();
        titlePara.setTextAlign(TextParagraph.TextAlign.CENTER);
        
        XSLFTextRun titleRun = titlePara.addNewTextRun();
        titleRun.setText(title);
        titleRun.setFontSize(fontSize);
        titleRun.setFontColor(fontColor);
        titleRun.setFontFamily(chineseFont);
        titleRun.setBold(true);
    }
    
    /**
     * 创建歌词幻灯片
     */
    private void createLyricsSlide(XMLSlideShow ppt, List<String> lines) {
        XSLFSlide slide = ppt.createSlide();
        
        // 设置背景
        setSlideBackgroundWithImage(ppt, slide, new Color(30, 30, 35));
        
        // 1. 提取段落标记（C, B, L）
        String sectionLabel = extractAndRemoveSectionLabel(lines);
        
        // 添加歌词文本框
        XSLFTextBox textBox = slide.createTextBox();
        
        // 文本框宽度设为整个幻灯片宽度，这样CENTER对齐才有意义
        int textBoxWidth = 1280;
        int textBoxHeight = 600;
        
        // 设置文本框位置 - 使用用户指定的垂直位置
        textBox.setAnchor(new Rectangle(0, textVerticalPosition, textBoxWidth, textBoxHeight));
        
        // 设置文本框的垂直对齐方式为顶部对齐
        textBox.setVerticalAlignment(org.apache.poi.sl.usermodel.VerticalAlignment.TOP);
        
        // 禁用文本自动调整 - 这很关键！
        textBox.setTextAutofit(org.apache.poi.sl.usermodel.TextShape.TextAutofit.NONE);
        
        // 设置文本框的内边距为0（左、上、右、下）
        textBox.setLeftInset(0);
        textBox.setTopInset(0);
        textBox.setRightInset(0);
        textBox.setBottomInset(0);
        
        // 处理每一行，分别设置中文和拼音的字体大小
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            
            // 检查这行是否包含中文
            boolean hasChinese = false;
            for (char c : line.toCharArray()) {
                if (c >= 0x4E00 && c <= 0x9FA5) {
                    hasChinese = true;
                    break;
                }
            }
            
            // 创建段落
            XSLFTextParagraph para = (i == 0) ? textBox.addNewTextParagraph() : textBox.addNewTextParagraph();
            para.setTextAlign(TextParagraph.TextAlign.CENTER);
            para.setSpaceBefore(0.0);
            
            // 如果不是最后一行且不包含中文（拼音行），设置较大的段后间距
            if (i < lines.size() - 1 && !hasChinese) {
                para.setSpaceAfter(fontSize * 0.7);
            } else {
                para.setSpaceAfter(0.0);
            }
            para.setLineSpacing(100.0);
            
            XSLFTextRun run = para.addNewTextRun();
            run.setText(line);
            run.setFontColor(fontColor);
            
            // 根据是否包含中文设置字体和大小
            if (hasChinese) {
                // 中文行
                run.setFontSize(fontSize);
                run.setFontFamily(chineseFont);
                run.setBold(true);
            } else {
                // 拼音行 - 50%大小
                run.setFontSize(fontSize * 0.5);
                run.setFontFamily(pinyinFont);
                run.setBold(true);
            }
        }
        
        // 2. 如果有段落标记，在底部添加
        if (sectionLabel != null && !sectionLabel.isEmpty()) {
            XSLFTextBox labelBox = slide.createTextBox();
            labelBox.setAnchor(new Rectangle(0, 540, 1280, 60));
            
            XSLFTextParagraph labelPara = labelBox.addNewTextParagraph();
            labelPara.setTextAlign(TextParagraph.TextAlign.CENTER);
            
            XSLFTextRun labelRun = labelPara.addNewTextRun();
            labelRun.setText(sectionLabel);
            labelRun.setFontSize(60.0);
            labelRun.setFontColor(Color.WHITE);
            labelRun.setFontFamily("Arial");
            labelRun.setBold(true);
        }
    }
    
    /**
     * 提取并移除段落标记（C, B, L, V）
     * 检查所有行，任何行有标记就提取并移除
     * @param lines 歌词行列表（会被直接修改）
     * @return 段落标记，如果没有返回null
     */
    private String extractAndRemoveSectionLabel(List<String> lines) {
        if (lines.isEmpty()) {
            return null;
        }
        
        // 遍历所有行，找到第一个带标记的行
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex).trim();
            
            if (line.length() > 0) {
                char firstChar = line.charAt(0);
                
                // 检查是否是标记字符 (C, B, L, V)
                if (firstChar == 'C' || firstChar == 'B' || firstChar == 'L' || firstChar == 'V') {
                    // 提取标记（字母 + 可选数字）
                    StringBuilder label = new StringBuilder();
                    label.append(firstChar);
                    
                    int i = 1;
                    // 提取后面的数字（如果有）
                    while (i < line.length() && Character.isDigit(line.charAt(i))) {
                        label.append(line.charAt(i));
                        i++;
                    }
                    
                    // 检查标记后是否有空格（必须有空格才算标记）
                    if (i < line.length() && line.charAt(i) == ' ') {
                        String labelStr = label.toString();
                        
                        // 移除标记，保留后面的内容
                        String remaining = line.substring(i + 1).trim();
                        if (!remaining.isEmpty()) {
                            lines.set(lineIndex, remaining);
                        } else {
                            // 如果移除标记后该行为空，删除整行
                            lines.remove(lineIndex);
                        }
                        
                        return labelStr;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 创建结束页
     */
    private void createEndSlide(XMLSlideShow ppt) {
        XSLFSlide slide = ppt.createSlide();
        
        // 设置背景
        setSlideBackgroundWithImage(ppt, slide, new Color(45, 45, 48));
        
        // 添加结束文本
        XSLFTextBox textBox = slide.createTextBox();
        textBox.setAnchor(new Rectangle(100, 280, 1080, 160));
        
        XSLFTextParagraph para = textBox.addNewTextParagraph();
        para.setTextAlign(TextParagraph.TextAlign.CENTER);
        
        XSLFTextRun run = para.addNewTextRun();
        run.setText("谢谢观看");
        run.setFontSize(60.0);
        run.setFontColor(fontColor);
        run.setFontFamily(chineseFont);
        run.setBold(true);
    }
    
    /**
     * 设置幻灯片背景（支持图片或颜色）
     * 修复：保持图片原始比例，不变形
     */
    private void setSlideBackgroundWithImage(XMLSlideShow ppt, XSLFSlide slide, Color defaultColor) {
        if (backgroundImagePath != null && !backgroundImagePath.isEmpty()) {
            try {
                // 添加背景图片
                byte[] imageData = readImageFile(backgroundImagePath);
                PictureData pictureData = ppt.addPicture(imageData, getPictureType(backgroundImagePath));
                
                // 创建图片形状并设置为背景
                XSLFPictureShape picture = slide.createPicture(pictureData);
                
                // 直接填充整个幻灯片区域 - 图片会自动缩放填充
                picture.setAnchor(new Rectangle(0, 0, 1280, 720));
                
                // 将图片移到最底层作为背景
                slide.getShapes().remove(picture);
                slide.getShapes().add(0, picture);
                
            } catch (IOException e) {
                // 如果图片加载失败，使用默认颜色
                System.err.println("无法加载背景图片: " + e.getMessage());
                setSlideBackground(slide, defaultColor);
            }
        } else {
            // 使用默认颜色背景
            setSlideBackground(slide, defaultColor);
        }
    }
    
    /**
     * 设置幻灯片背景颜色
     */
    private void setSlideBackground(XSLFSlide slide, Color color) {
        XSLFBackground background = slide.getBackground();
        background.setFillColor(color);
    }
    
    /**
     * 读取图片文件
     */
    private byte[] readImageFile(String imagePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(imagePath)) {
            byte[] data = new byte[fis.available()];
            fis.read(data);
            return data;
        }
    }
    
    /**
     * 根据文件扩展名获取图片类型
     */
    private PictureData.PictureType getPictureType(String imagePath) {
        String extension = imagePath.toLowerCase();
        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
            return PictureData.PictureType.JPEG;
        } else if (extension.endsWith(".png")) {
            return PictureData.PictureType.PNG;
        } else if (extension.endsWith(".gif")) {
            return PictureData.PictureType.GIF;
        } else if (extension.endsWith(".bmp")) {
            return PictureData.PictureType.BMP;
        } else {
            return PictureData.PictureType.JPEG; // 默认
        }
    }
    
    /**
     * 分割歌词为行
     */
    private List<String> splitLyrics(String lyrics) {
        List<String> lines = new ArrayList<>();
        String[] rawLines = lyrics.split("\n");
        
        for (String line : rawLines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
        
        return lines;
    }
    
    /**
     * 将歌词行分组（支持/分隔符和拼音）
     * 规则：
     * 1. 如果一行末尾有/，则强制在此处分页（包括其拼音）
     * 2. 如果启用了拼音，汉字+拼音必须在同一slide
     * 3. 否则按linesPerSlide正常分组
     */
    private List<List<String>> groupLines(List<String> lines, int linesPerSlide) {
        List<List<String>> groups = new ArrayList<>();
        
        // 检测是否启用了拼音
        boolean hasPinyin = detectPinyinPattern(lines);
        
        List<String> currentGroup = new ArrayList<>();
        int logicalLineCount = 0; // 逻辑行计数（汉字行数）
        
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            
            // 添加当前行（汉字行）
            currentGroup.add(line);
            
            // 检查是否有对应的拼音行
            boolean hasPinyinLine = false;
            String pinyinLine = null;
            
            if (hasPinyin && i + 1 < lines.size()) {
                pinyinLine = lines.get(i + 1);
                // 检查下一行是否是拼音行（不含中文）
                boolean nextLineIsPinyin = true;
                for (char c : pinyinLine.toCharArray()) {
                    if (c >= 0x4E00 && c <= 0x9FA5) {
                        nextLineIsPinyin = false;
                        break;
                    }
                }
                
                if (nextLineIsPinyin) {
                    hasPinyinLine = true;
                    currentGroup.add(pinyinLine);
                }
            }
            
            // 逻辑行计数+1
            logicalLineCount++;
            
            // 检查是否需要分页
            boolean shouldBreak = false;
            
            // 1. 检查汉字行是否有/分隔符
            String cleanLine = line.trim();
            if (cleanLine.endsWith("/")) {
                shouldBreak = true;
            }
            
            // 2. 检查是否达到每页行数
            if (logicalLineCount >= linesPerSlide) {
                shouldBreak = true;
            }
            
            // 3. 移动索引
            if (hasPinyinLine) {
                i += 2; // 跳过汉字+拼音
            } else {
                i += 1; // 只跳过汉字
            }
            
            // 4. 如果到达最后，也要分页
            if (i >= lines.size()) {
                shouldBreak = true;
            }
            
            // 执行分页
            if (shouldBreak && !currentGroup.isEmpty()) {
                // 移除行末的/符号
                List<String> cleanedGroup = new ArrayList<>();
                for (String groupLine : currentGroup) {
                    String cleaned = groupLine.trim();
                    if (cleaned.endsWith("/")) {
                        cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
                    }
                    cleanedGroup.add(cleaned);
                }
                
                groups.add(cleanedGroup);
                currentGroup = new ArrayList<>();
                logicalLineCount = 0;
            }
        }
        
        // 处理最后可能剩余的行
        if (!currentGroup.isEmpty()) {
            // 移除行末的/符号
            List<String> cleanedGroup = new ArrayList<>();
            for (String groupLine : currentGroup) {
                String cleaned = groupLine.trim();
                if (cleaned.endsWith("/")) {
                    cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
                }
                cleanedGroup.add(cleaned);
            }
            groups.add(cleanedGroup);
        }
        
        return groups;
    }
    
    /**
     * 检测歌词是否包含拼音（汉字+拼音成对模式）
     */
    private boolean detectPinyinPattern(List<String> lines) {
        if (lines.size() < 2) {
            return false;
        }
        
        // 检查前几行是否符合"汉字行+拼音行"模式
        for (int i = 0; i < Math.min(4, lines.size() - 1); i += 2) {
            String line1 = lines.get(i);
            String line2 = lines.get(i + 1);
            
            // 第一行应该包含中文
            boolean line1HasChinese = false;
            for (char c : line1.toCharArray()) {
                if (c >= 0x4E00 && c <= 0x9FA5) {
                    line1HasChinese = true;
                    break;
                }
            }
            
            // 第二行应该不包含中文（拼音行）
            boolean line2HasChinese = false;
            for (char c : line2.toCharArray()) {
                if (c >= 0x4E00 && c <= 0x9FA5) {
                    line2HasChinese = true;
                    break;
                }
            }
            
            // 如果符合模式（有中文 + 无中文），说明有拼音
            if (line1HasChinese && !line2HasChinese && line2.length() > 0) {
                return true;
            }
        }
        
        return false;
    }
}