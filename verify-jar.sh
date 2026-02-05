#!/bin/bash

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "========================================"
echo "     JAR文件验证工具"
echo "========================================"
echo ""

JARFILE="target/歌词转PPT.jar"

if [ ! -f "$JARFILE" ]; then
    echo -e "${RED}[错误] 找不到JAR文件！${NC}"
    echo ""
    echo "请先运行: mvn clean package"
    echo "或使用: ./build-and-package.sh"
    echo ""
    exit 1
fi

echo -e "${GREEN}[1/6] 检查文件是否存在...${NC}"
echo "✓ 找到文件: $JARFILE"
echo ""

echo -e "${GREEN}[2/6] 检查文件大小...${NC}"
size=$(stat -f%z "$JARFILE" 2>/dev/null || stat -c%s "$JARFILE" 2>/dev/null)
echo "文件大小: $size 字节 ($(( size / 1024 / 1024 )) MB)"
if [ "$size" -lt 10000000 ]; then
    echo -e "${YELLOW}⚠ 警告：文件太小！正确的JAR应该是10-15MB${NC}"
    echo "可能缺少依赖库。"
fi
echo ""

echo -e "${GREEN}[3/6] 检查Manifest文件...${NC}"
jar xf "$JARFILE" META-INF/MANIFEST.MF 2>/dev/null
if [ -f "META-INF/MANIFEST.MF" ]; then
    cat META-INF/MANIFEST.MF
    rm -rf META-INF
    echo ""
    echo "✓ Manifest文件存在"
else
    echo -e "${RED}✗ 找不到Manifest文件！${NC}"
fi
echo ""

echo -e "${GREEN}[4/6] 检查主类文件...${NC}"
if jar tf "$JARFILE" | grep -q "com/lyrics/LyricsToSlidesApp.class"; then
    echo "✓ 主类文件存在: com/lyrics/LyricsToSlidesApp.class"
else
    echo -e "${RED}✗ 找不到主类文件！${NC}"
fi
echo ""

echo -e "${GREEN}[5/6] 检查POI依赖库...${NC}"
if jar tf "$JARFILE" | grep -q "org/apache/poi/xslf"; then
    echo "✓ Apache POI库已包含"
    echo ""
    echo "显示部分POI类文件:"
    jar tf "$JARFILE" | grep "org/apache/poi" | head -20
else
    echo -e "${RED}✗ Apache POI库缺失！${NC}"
    echo ""
    echo "这个JAR文件无法在其他电脑运行。"
    echo "请使用 mvn clean package 重新构建。"
fi
echo ""

echo -e "${GREEN}[6/6] 尝试运行JAR文件...${NC}"
echo "按Enter键启动程序..."
read

echo ""
echo "正在启动..."
java -jar "$JARFILE"

if [ $? -ne 0 ]; then
    echo ""
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}[失败] JAR文件无法运行！${NC}"
    echo -e "${RED}========================================${NC}"
    echo ""
    echo "请检查上面的错误信息。"
    echo ""
else
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}[成功] JAR文件验证通过！${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
fi
