# 歌词转PPT工具

这是一个Java桌面应用程序，可以自动将歌词转换为PowerPoint演示文稿。

## 功能特点

- ✅ 简洁的图形界面
- ✅ 自动将歌词分割成多张幻灯片
- ✅ 可自定义每张幻灯片的行数
- ✅ 美观的深色主题设计
- ✅ 自动生成标题页和结束页
- ✅ 支持中文字体显示

## 系统要求

- Java 11 或更高版本
- Maven 3.6 或更高版本（用于构建）
- Windows/macOS/Linux操作系统

## 安装和运行

### 方法1：使用Maven构建和运行

1. **确保已安装Java和Maven**
   ```bash
   java -version
   mvn -version
   ```

2. **克隆或下载项目到本地**

3. **进入项目目录**
   ```bash
   cd LyricsToSlides
   ```

4. **编译项目**
   ```bash
   mvn clean compile
   ```

5. **运行应用**
   ```bash
   mvn exec:java -Dexec.mainClass="com.lyrics.LyricsToSlidesApp"
   ```

### 方法2：生成可执行JAR文件

1. **构建可执行JAR**
   ```bash
   mvn clean package
   ```

2. **运行生成的JAR文件**
   ```bash
   java -jar target/歌词转PPT.jar
   ```

3. **（可选）创建桌面快捷方式**
   - Windows: 右键点击JAR文件 → 发送到 → 桌面快捷方式
   - macOS: 创建一个shell脚本运行JAR文件
   - Linux: 创建.desktop文件

## 使用说明

1. **启动应用程序**
   - 运行程序后会打开图形界面

2. **输入PPT标题**
   - 在顶部输入框输入你的PPT标题（默认：歌词展示）

3. **设置每页行数**
   - 调整"每张幻灯片行数"（建议2-6行）

4. **输入歌词**
   - 在文本框中粘贴或输入歌词
   - 每行一句歌词

5. **生成PPT**
   - 点击"生成PPT"按钮
   - 选择保存位置和文件名
   - 等待生成完成

6. **打开PPT**
   - 生成完成后可选择立即打开文件

## 项目结构

```
LyricsToSlides/
├── pom.xml                          # Maven配置文件
├── README.md                        # 项目说明
└── src/
    └── main/
        └── java/
            └── com/
                └── lyrics/
                    ├── LyricsToSlidesApp.java      # 主程序（GUI）
                    └── PowerPointGenerator.java    # PPT生成器
```

## 技术栈

- **Java Swing** - 图形用户界面
- **Apache POI** - PowerPoint文件操作
- **Maven** - 项目管理和构建

## 示例歌词格式

```
我曾经跨过山和大海
也穿过人山人海
我曾经拥有着的一切
转眼都飘散如烟
我曾经失落失望失掉所有方向
直到看见平凡才是唯一的答案
```

## 自定义选项

### 修改字体大小
编辑 `PowerPointGenerator.java` 文件中的字体大小参数：
- 标题页：`setFontSize(72.0)` → 修改为你想要的大小
- 歌词页：`setFontSize(48.0)` → 修改为你想要的大小

### 修改背景颜色
编辑 `PowerPointGenerator.java` 文件中的颜色参数：
- 标题页背景：`new Color(45, 45, 48)` → RGB值
- 歌词页背景：`new Color(30, 30, 35)` → RGB值

### 修改字体
编辑 `setFontFamily("Microsoft YaHei")` → 改为其他字体名称

## 常见问题

**Q: 生成的PPT无法打开？**
A: 确保保存路径有写入权限，文件名不包含特殊字符。

**Q: 中文显示乱码？**
A: 确保系统已安装"Microsoft YaHei"字体，或修改代码使用其他中文字体。

**Q: 如何修改幻灯片尺寸？**
A: 在 `PowerPointGenerator.java` 中修改 `setPageSize(new Dimension(1280, 720))` 参数。

## 贡献

欢迎提交问题和改进建议！

## 许可证

MIT License
