@echo off
echo ===============================================
echo    歌词转PPT - 自动构建和打包脚本
echo ===============================================
echo.

REM 检查是否在正确的目录
if not exist "pom.xml" (
    echo [错误] 请在项目根目录运行此脚本！
    echo 当前目录应该包含 pom.xml 文件。
    pause
    exit /b 1
)

echo [1/4] 清理旧的构建...
call mvn clean
if %errorlevel% neq 0 (
    echo [错误] 清理失败！
    pause
    exit /b 1
)

echo.
echo [2/4] 编译和打包项目...
call mvn package
if %errorlevel% neq 0 (
    echo [错误] 构建失败！请检查编译错误。
    pause
    exit /b 1
)

echo.
echo [3/4] 复制JAR文件到分发目录...
if not exist "Distribution" mkdir Distribution
copy /Y "target\LyricsToPPT.jar" "Distribution\LyricsToPPT.jar"
if %errorlevel% neq 0 (
    echo [错误] 复制JAR文件失败！
    pause
    exit /b 1
)

echo.
echo [4/4] 创建分发包...
if exist "LyricsToPPT.zip" del "LyricsToPPT.zip"

REM 检查是否有PowerShell（用于压缩）
where powershell >nul 2>&1
if %errorlevel% equ 0 (
    powershell Compress-Archive -Path "Distribution\*" -DestinationPath "LyricsToPPT.zip" -Force
    echo.
    echo ===============================================
    echo ✅ 构建成功！
    echo ===============================================
    echo.
    echo 分发包已创建：歌词转PPT工具.zip
    echo 包含以下文件：
    dir /B Distribution
    echo.
    echo 现在可以将 歌词转PPT工具.zip 分享给别人了！
    echo ===============================================
) else (
    echo.
    echo ===============================================
    echo ✅ JAR文件已生成！
    echo ===============================================
    echo.
    echo 文件位置：Distribution\歌词转PPT.jar
    echo.
    echo 请手动将 Distribution 文件夹压缩成ZIP文件。
    echo 然后就可以分享了！
    echo ===============================================
)

echo.
pause
