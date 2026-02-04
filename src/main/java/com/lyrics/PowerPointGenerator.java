package com.lyrics;

import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.sl.usermodel.TextParagraph;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PowerPointGenerator {
    
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
        
        // 设置背景颜色
        setSlideBackground(slide, new Color(45, 45, 48));
        
        // 添加标题
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle(100, 250, 1080, 200));
        
        XSLFTextParagraph titlePara = titleBox.addNewTextParagraph();
        titlePara.setTextAlign(TextParagraph.TextAlign.CENTER);
        
        XSLFTextRun titleRun = titlePara.addNewTextRun();
        titleRun.setText(title);
        titleRun.setFontSize(72.0);
        titleRun.setFontColor(Color.WHITE);
        titleRun.setFontFamily("Microsoft YaHei");
        titleRun.setBold(true);
    }
    
    /**
     * 创建歌词幻灯片
     */
    private void createLyricsSlide(XMLSlideShow ppt, List<String> lines) {
        XSLFSlide slide = ppt.createSlide();
        
        // 设置背景颜色
        setSlideBackground(slide, new Color(30, 30, 35));
        
        // 添加歌词文本框
        XSLFTextBox textBox = slide.createTextBox();
        textBox.setAnchor(new Rectangle(150, 150, 980, 420));
        
        // 合并所有行
        StringBuilder lyricsText = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            lyricsText.append(lines.get(i));
            if (i < lines.size() - 1) {
                lyricsText.append("\n");
            }
        }
        
        // 添加段落
        XSLFTextParagraph para = textBox.addNewTextParagraph();
        para.setTextAlign(TextParagraph.TextAlign.CENTER);
        para.setLineSpacing(150.0); // 行间距150%
        
        XSLFTextRun run = para.addNewTextRun();
        run.setText(lyricsText.toString());
        run.setFontSize(48.0);
        run.setFontColor(Color.WHITE);
        run.setFontFamily("Microsoft YaHei");
    }
    
    /**
     * 创建结束页
     */
    private void createEndSlide(XMLSlideShow ppt) {
        XSLFSlide slide = ppt.createSlide();
        
        // 设置背景颜色
        setSlideBackground(slide, new Color(45, 45, 48));
        
        // 添加结束文本
        XSLFTextBox textBox = slide.createTextBox();
        textBox.setAnchor(new Rectangle(100, 280, 1080, 160));
        
        XSLFTextParagraph para = textBox.addNewTextParagraph();
        para.setTextAlign(TextParagraph.TextAlign.CENTER);
        
        XSLFTextRun run = para.addNewTextRun();
        run.setText("谢谢观看");
        run.setFontSize(60.0);
        run.setFontColor(Color.WHITE);
        run.setFontFamily("Microsoft YaHei");
        run.setBold(true);
    }
    
    /**
     * 设置幻灯片背景颜色
     */
    private void setSlideBackground(XSLFSlide slide, Color color) {
        XSLFBackground background = slide.getBackground();
        background.setFillColor(color);
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
