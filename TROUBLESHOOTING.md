# Maven编译错误修复指南

## 问题描述
遇到错误：`Error while storing the mojo status: Input length = 1`

## 解决方案

这个错误通常是由于Maven编译器插件版本或配置问题引起的。我已经修复了配置文件。

### 方法1：使用修复后的pom.xml（推荐）

我已经将Maven编译器版本从3.11.0降级到更稳定的3.8.1，并将Java版本从11改为8（更广泛兼容）。

新的配置已在项目中，你只需要：

1. **删除之前的构建缓存**
   ```bash
   # Windows
   rmdir /s /q target
   
   # Mac/Linux
   rm -rf target
   ```

2. **清理Maven缓存（可选但建议）**
   ```bash
   mvn clean
   ```

3. **重新构建**
   ```bash
   mvn clean package
   ```

### 方法2：手动编译（不使用Maven）

如果Maven问题持续，可以手动编译Java文件：

**步骤1：创建编译脚本**

创建文件 `compile.bat`（Windows）或 `compile.sh`（Mac/Linux）：

```bash
#!/bin/bash
# compile.sh

# 创建输出目录
mkdir -p build/classes
mkdir -p build/lib

# 下载依赖（手动下载一次）
# 从 https://poi.apache.org/download.html 下载Apache POI
# 需要以下JAR文件：
# - poi-5.2.5.jar
# - poi-ooxml-5.2.5.jar
# - poi-ooxml-lite-5.2.5.jar
# - xmlbeans-5.1.1.jar
# - commons-compress-1.24.0.jar
# - commons-collections4-4.4.jar
# 放到 build/lib 目录

# 编译
javac -d build/classes -cp "build/lib/*" \
  src/main/java/com/lyrics/LyricsToSlidesApp.java \
  src/main/java/com/lyrics/PowerPointGenerator.java

# 创建JAR
cd build/classes
jar cfm ../LyricsToSlides.jar ../../manifest.txt com/

echo "编译完成！"
echo "运行: java -cp \"build/LyricsToSlides.jar:build/lib/*\" com.lyrics.LyricsToSlidesApp"
```

**步骤2：创建manifest文件**

创建 `manifest.txt`:
```
Manifest-Version: 1.0
Main-Class: com.lyrics.LyricsToSlidesApp
Class-Path: lib/poi-5.2.5.jar lib/poi-ooxml-5.2.5.jar lib/poi-ooxml-lite-5.2.5.jar lib/xmlbeans-5.1.1.jar lib/commons-compress-1.24.0.jar lib/commons-collections4-4.4.jar
```

### 方法3：使用Gradle替代Maven

创建 `build.gradle`:

```gradle
plugins {
    id 'java'
    id 'application'
}

group = 'com.lyrics'
version = '1.0'
sourceCompatibility = '1.8'
targetCompatibility = '1.8'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.apache.poi:poi:5.2.5'
    implementation 'org.apache.poi:poi-ooxml:5.2.5'
}

application {
    mainClass = 'com.lyrics.LyricsToSlidesApp'
}

jar {
    manifest {
        attributes 'Main-Class': 'com.lyrics.LyricsToSlidesApp'
    }
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

然后使用：
```bash
# 构建
gradle build

# 运行
gradle run

# 创建可执行JAR
gradle jar
```

### 方法4：修复Maven本地仓库

有时Maven本地仓库损坏会导致问题：

```bash
# 删除Maven本地仓库中的问题插件
# Windows
rmdir /s /q %USERPROFILE%\.m2\repository\org\apache\maven\plugins\maven-compiler-plugin

# Mac/Linux
rm -rf ~/.m2/repository/org/apache/maven/plugins/maven-compiler-plugin

# 重新下载
mvn clean package -U
```

### 方法5：使用IDE（最简单）

如果你有IDE（如IntelliJ IDEA、Eclipse、NetBeans）：

1. 打开IDE
2. 导入项目为Maven项目
3. IDE会自动处理依赖
4. 右键项目 → Run 'LyricsToSlidesApp.main()'

**IntelliJ IDEA:**
- File → Open → 选择项目文件夹
- 等待Maven导入完成
- 右键 `LyricsToSlidesApp.java` → Run

**Eclipse:**
- File → Import → Maven → Existing Maven Projects
- 选择项目文件夹
- 右键项目 → Run As → Java Application

### 常见Maven问题和解决方案

**问题1：依赖下载失败**
```bash
# 使用阿里云镜像（中国用户）
# 编辑 ~/.m2/settings.xml 或 C:\Users\你的用户名\.m2\settings.xml
```

添加：
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

**问题2：Java版本不匹配**
```bash
# 检查Java版本
java -version

# 如果是Java 17+，可能需要添加参数
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"
```

**问题3：权限问题**
```bash
# Linux/Mac：给予执行权限
chmod +x mvnw
./mvnw clean package
```

## 验证修复

构建成功后，你应该看到：

```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  XX.XXX s
[INFO] Finished at: YYYY-MM-DD
[INFO] ------------------------------------------------------------------------
```

并且在 `target/` 目录下生成：
- `lyrics-to-slides-1.0-SNAPSHOT-jar-with-dependencies.jar`

## 下载依赖JAR文件（手动方法）

如果完全无法使用Maven，可以手动下载JAR文件：

1. **Apache POI核心库**
   - https://poi.apache.org/download.html
   - 下载poi-bin-5.2.5.zip

2. **解压后需要的JAR文件：**
   - poi-5.2.5.jar
   - poi-ooxml-5.2.5.jar
   - poi-ooxml-lite-5.2.5.jar
   - xmlbeans-5.1.1.jar
   - commons-compress-1.24.0.jar
   - commons-collections4-4.4.jar
   - log4j-api-2.20.0.jar

3. **编译命令：**
   ```bash
   javac -cp "lib/*" -d build src/main/java/com/lyrics/*.java
   ```

4. **运行命令：**
   ```bash
   java -cp "build:lib/*" com.lyrics.LyricsToSlidesApp
   ```

## 需要帮助？

如果以上方法都不行，请告诉我：
1. 你的操作系统
2. Java版本（`java -version`）
3. Maven版本（`mvn -version`）
4. 完整的错误信息
