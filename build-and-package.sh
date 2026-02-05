#!/bin/bash

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "==============================================="
echo "   歌词转PPT - 自动构建和打包脚本"
echo "==============================================="
echo ""

# 检查是否在正确的目录
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}[错误] 请在项目根目录运行此脚本！${NC}"
    echo "当前目录应该包含 pom.xml 文件。"
    exit 1
fi

# 步骤1：清理
echo -e "${BLUE}[1/4] 清理旧的构建...${NC}"
mvn clean
if [ $? -ne 0 ]; then
    echo -e "${RED}[错误] 清理失败！${NC}"
    exit 1
fi

# 步骤2：构建
echo ""
echo -e "${BLUE}[2/4] 编译和打包项目...${NC}"
mvn package
if [ $? -ne 0 ]; then
    echo -e "${RED}[错误] 构建失败！请检查编译错误。${NC}"
    exit 1
fi

# 步骤3：复制JAR文件
echo ""
echo -e "${BLUE}[3/4] 复制JAR文件到分发目录...${NC}"
mkdir -p Distribution
cp -f "target/歌词转PPT.jar" "Distribution/歌词转PPT.jar"
if [ $? -ne 0 ]; then
    echo -e "${RED}[错误] 复制JAR文件失败！${NC}"
    exit 1
fi

# 步骤4：创建分发包
echo ""
echo -e "${BLUE}[4/4] 创建分发包...${NC}"
rm -f "歌词转PPT工具.zip"

# 检查是否有zip命令
if command -v zip &> /dev/null; then
    cd Distribution
    zip -r "../歌词转PPT工具.zip" ./*
    cd ..
    
    echo ""
    echo "==============================================="
    echo -e "${GREEN}✅ 构建成功！${NC}"
    echo "==============================================="
    echo ""
    echo "分发包已创建：歌词转PPT工具.zip"
    echo ""
    echo "包含以下文件："
    ls -1 Distribution/
    echo ""
    echo "现在可以将 歌词转PPT工具.zip 分享给别人了！"
    echo "==============================================="
else
    echo ""
    echo "==============================================="
    echo -e "${GREEN}✅ JAR文件已生成！${NC}"
    echo "==============================================="
    echo ""
    echo "文件位置：Distribution/歌词转PPT.jar"
    echo ""
    echo -e "${YELLOW}注意：未检测到zip命令${NC}"
    echo "请手动将 Distribution 文件夹压缩成ZIP文件。"
    echo ""
    echo "Mac用户可以："
    echo "  在Finder中右键Distribution文件夹 → 压缩"
    echo ""
    echo "Linux用户可以安装zip："
    echo "  sudo apt install zip  # Ubuntu/Debian"
    echo "  sudo yum install zip  # CentOS/RHEL"
    echo "==============================================="
fi

echo ""
