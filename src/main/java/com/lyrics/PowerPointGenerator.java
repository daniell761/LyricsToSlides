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
        createEndSlide(ppt);
        
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
        titleRun.setFontSize(72.0);
        titleRun.setFontColor(fontColor);
        titleRun.setFontFamily("Microsoft YaHei");
        titleRun.setBold(true);
    }
    
    /**
     * 创建歌词幻灯片
     */
    private void createLyricsSlide(XMLSlideShow ppt, List<String> lines) {
        XSLFSlide slide = ppt.createSlide();
        
        // 设置背景
        setSlideBackgroundWithImage(ppt, slide, new Color(30, 30, 35));
        
        // 添加歌词文本框
        XSLFTextBox textBox = slide.createTextBox();
        
        // 文本框宽度设为整个幻灯片宽度，这样CENTER对齐才有意义
        int textBoxWidth = 1280;
        int textBoxHeight = 600;
        
        // 设置文本框位置 - 使用用户指定的垂直位置
        textBox.setAnchor(new Rectangle(0, textVerticalPosition, textBoxWidth, textBoxHeight));
        
        // 设置文本框的垂直对齐方式为顶部对齐
        textBox.setVerticalAlignment(org.apache.poi.sl.usermodel.VerticalAlignment.TOP);
        
        // 设置文本框的内边距为0（左、上、右、下）
        textBox.setLeftInset(0);
        textBox.setTopInset(0);
        textBox.setRightInset(0);
        textBox.setBottomInset(0);
        
        // 合并所有行
        StringBuilder lyricsText = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            lyricsText.append(lines.get(i));
            if (i < lines.size() - 1) {
                lyricsText.append("\n");
            }
        }
        
        // 添加段落 - 使用CENTER对齐
        XSLFTextParagraph para = textBox.addNewTextParagraph();
        para.setTextAlign(TextParagraph.TextAlign.CENTER);
        para.setLineSpacing(150.0); // 行间距150%
        
        XSLFTextRun run = para.addNewTextRun();
        run.setText(lyricsText.toString());
        run.setFontSize(fontSize);
        run.setFontColor(fontColor);
        run.setFontFamily("Microsoft YaHei");
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
        run.setFontFamily("Microsoft YaHei");
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
     * 将歌词行分组
     */
    private List<List<String>> groupLines(List<String> lines, int linesPerSlide) {
        List<List<String>> groups = new ArrayList<>();
        
        for (int i = 0; i < lines.size(); i += linesPerSlide) {
            int endIndex = Math.min(i + linesPerSlide, lines.size());
            List<String> group = new ArrayList<>(lines.subList(i, endIndex));
            groups.add(group);
        }
        
        return groups;
    }
}