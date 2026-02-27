package com.lyrics;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {
    
    private static final HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
    
    static {
        // 设置拼音格式
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);  // 先设为小写，后面会手动首字母大写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);  // 不带声调
        format.setVCharType(HanyuPinyinVCharType.WITH_V);  // ü用v表示
    }
    
    /**
     * 将中文转换为拼音（每个字之间用空格分隔，每个词首字母大写）
     * @param chinese 中文文本
     * @return 拼音文本
     */
    public static String toPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        
        StringBuilder pinyin = new StringBuilder();
        
        for (int i = 0; i < chinese.length(); i++) {
            char c = chinese.charAt(i);
            
            // 如果是中文字符
            if (isChinese(c)) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        String py = pinyinArray[0];  // 直接使用小写拼音
                        
                        pinyin.append(py);
                        
                        // 如果下一个字符也是中文，添加空格
                        if (i + 1 < chinese.length() && isChinese(chinese.charAt(i + 1))) {
                            pinyin.append(" ");
                        }
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    // 转换失败，保留原字符
                    pinyin.append(c);
                }
            } else {
                // 非中文字符直接保留
                pinyin.append(c);
            }
        }
        
        return pinyin.toString();
    }
    
    /**
     * 判断是否为中文字符
     */
    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
    
    /**
     * 为每行歌词添加拼音（拼音在汉字下方）
     * @param lyrics 歌词文本（多行）
     * @return 带拼音的歌词（汉字在上，拼音在下）
     */
    public static String addPinyinToLyrics(String lyrics) {
        if (lyrics == null || lyrics.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        String[] lines = lyrics.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            if (!line.isEmpty()) {
                // 检查这行是否包含中文
                boolean hasChinese = false;
                for (char c : line.toCharArray()) {
                    if (isChinese(c)) {
                        hasChinese = true;
                        break;
                    }
                }
                
                // 先添加原文行（汉字在上）
                result.append(line).append("\n");
                
                // 如果有中文，添加拼音行（拼音在下）
                if (hasChinese) {
                    String pinyinLine = toPinyin(line);
                    result.append(pinyinLine);
                }
                
                if (i < lines.length - 1) {
                    result.append("\n");
                }
            }
        }
        
        return result.toString();
    }
}