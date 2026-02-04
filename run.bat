@echo off
echo =====================================
echo 歌词转PPT工具 - 启动中...
echo =====================================
echo.

REM 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未检测到Java运行环境！
    echo 请先安装Java 11或更高版本。
    echo.
    pause
    exit /b 1
)

REM 运行应用
java -jar target\lyrics-to-slides-1.0-SNAPSHOT-jar-with-dependencies.jar

if %errorlevel% neq 0 (
    echo.
    echo 应用运行出错！
    echo 请确保已使用 'mvn clean package' 构建项目。
    echo.
    pause
)
