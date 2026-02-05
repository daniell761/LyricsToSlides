@echo off
echo ========================================
echo     JAR文件验证工具
echo ========================================
echo.

set JARFILE=target\LyricsToPPT.jar

if not exist %JARFILE% (
    color 0C
    echo [错误] 找不到JAR文件！
    echo.
    echo 请先运行: mvn clean package
    echo 或使用: build-and-package.bat
    echo.
    pause
    exit /b 1
)

echo [1/6] 检查文件是否存在...
echo ✓ 找到文件: %JARFILE%
echo.

echo [2/6] 检查文件大小...
for %%A in (%JARFILE%) do (
    set size=%%~zA
    echo 文件大小: %%~zA 字节
)
echo.
if %size% LSS 10000000 (
    color 0E
    echo ⚠ 警告：文件太小！正确的JAR应该是10-15MB
    echo 可能缺少依赖库。
    echo.
)

echo [3/6] 检查Manifest文件...
jar xf %JARFILE% META-INF/MANIFEST.MF 2>nul
if exist META-INF\MANIFEST.MF (
    type META-INF\MANIFEST.MF
    del /q META-INF\MANIFEST.MF
    rmdir META-INF
    echo.
    echo ✓ Manifest文件存在
) else (
    color 0C
    echo ✗ 找不到Manifest文件！
)
echo.

echo [4/6] 检查主类文件...
jar tf %JARFILE% | findstr "com/lyrics/LyricsToSlidesApp.class" >nul
if %errorlevel% equ 0 (
    echo ✓ 主类文件存在: com/lyrics/LyricsToSlidesApp.class
) else (
    color 0C
    echo ✗ 找不到主类文件！
)
echo.

echo [5/6] 检查POI依赖库...
jar tf %JARFILE% | findstr "org/apache/poi/xslf" >nul
if %errorlevel% equ 0 (
    echo ✓ Apache POI库已包含
    echo.
    echo 显示部分POI类文件:
) else (
    color 0C
    echo ✗ Apache POI库缺失！
    echo.
    echo 这个JAR文件无法在其他电脑运行。
    echo 请使用 mvn clean package 重新构建。
)
echo.

echo [6/6] 尝试运行JAR文件...
echo 按任意键启动程序...
pause >nul
echo.
echo 正在启动...
java -jar %JARFILE%

if %errorlevel% neq 0 (
    echo.
    color 0C
    echo ========================================
    echo [失败] JAR文件无法运行！
    echo ========================================
    echo.
    echo 请检查上面的错误信息。
    echo.
) else (
    echo.
    color 0A
    echo ========================================
    echo [成功] JAR文件验证通过！
    echo ========================================
    echo.
)

pause
