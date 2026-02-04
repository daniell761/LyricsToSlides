# 快速开始指南

## 第一次使用（需要构建）

### Windows用户：

1. 打开命令提示符（CMD）或PowerShell
2. 进入项目文件夹：
   ```
   cd 你的项目路径\LyricsToSlides
   ```

3. 构建项目：
   ```
   mvn clean package
   ```
   （首次构建会下载依赖，需要几分钟）

4. 运行应用：
   - 双击 `run.bat` 文件
   或在命令行输入：
   ```
   run.bat
   ```

### macOS/Linux用户：

1. 打开终端
2. 进入项目文件夹：
   ```bash
   cd 你的项目路径/LyricsToSlides
   ```

3. 构建项目：
   ```bash
   mvn clean package
   ```

4. 运行应用：
   ```bash
   ./run.sh
   ```

## 后续使用

构建完成后，每次只需：
- **Windows**: 双击 `run.bat`
- **macOS/Linux**: 运行 `./run.sh`

## 使用流程

1. **输入标题** → 在顶部输入你的PPT标题
2. **设置行数** → 选择每张幻灯片显示几行歌词（建议4行）
3. **粘贴歌词** → 在大文本框中粘贴你的歌词
4. **生成PPT** → 点击"生成PPT"按钮
5. **保存文件** → 选择保存位置和文件名
6. **完成！** → 可选择立即打开查看

## 示例歌词

试试复制以下歌词到应用中：

```
我曾经跨过山和大海
也穿过人山人海
我曾经拥有着的一切
转眼都飘散如烟

我曾经失落失望失掉所有方向
直到看见平凡才是唯一的答案

当你仍然还在幻想
你的明天她会好吗
还是更烂对我而言
是另一天
```

## 故障排除

### 问题：无法运行 `mvn` 命令
**解决方案**：需要先安装Maven
- Windows: 下载 https://maven.apache.org/download.cgi
- macOS: `brew install maven`
- Linux: `sudo apt-get install maven` 或 `sudo yum install maven`

### 问题：无法运行 `java` 命令
**解决方案**：需要先安装Java JDK 11或更高版本
- 下载地址: https://adoptium.net/

### 问题：生成的PPT打不开
**解决方案**：
- 确保文件保存路径没有特殊字符
- 确保有写入权限
- 检查磁盘空间是否充足

### 问题：字体显示不正常
**解决方案**：
- 确保系统安装了"微软雅黑"字体
- 或修改代码使用其他字体

## 需要帮助？

查看完整文档：`README.md`
