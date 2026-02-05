@echo off
title 歌词转PPT工具
color 0A
echo.
echo ===============================================
echo        欢迎使用歌词转PPT工具
echo ===============================================
echo.
echo 正在启动程序...
echo.

REM 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    color 0C
    echo ===============================================
    echo [错误] 未检测到Java运行环境！
    echo ===============================================
    echo.
    echo 请先安装Java 8或更高版本
    echo.
    echo 下载地址：https://adoptium.net/
    echo.
    echo 安装完成后，请重新运行此程序。
    echo ===============================================
    echo.
    pause
    exit /b 1
)

REM 显示Java版本
echo 检测到Java版本：
java -version 2>&1 | findstr "version"
echo.

REM 检查JAR文件是否存在
if not exist "歌词转PPT.jar" (
    color 0C
    echo ===============================================
    echo [错误] 找不到"歌词转PPT.jar"文件！
    echo ===============================================
    echo.
    echo 请确保以下文件在同一目录下：
    echo - 启动歌词转PPT.bat
    echo - 歌词转PPT.jar
    echo.
    pause
    exit /b 1
)

REM 运行程序
echo 正在启动歌词转PPT工具...
echo.
java -jar 歌词转PPT.jar

REM 检查运行结果
if %errorlevel% neq 0 (
    echo.
    color 0C
    echo ===============================================
    echo [错误] 程序运行失败！
    echo ===============================================
    echo.
    echo 可能的原因：
    echo 1. JAR文件已损坏
    echo 2. Java版本太旧（需要Java 8+）
    echo 3. 系统内存不足
    echo.
    echo 请检查以上问题后重试。
    echo ===============================================
    echo.
    pause
)
