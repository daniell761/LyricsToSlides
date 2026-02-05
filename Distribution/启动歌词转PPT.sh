#!/bin/bash

# 设置颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

clear
echo "==============================================="
echo "        欢迎使用歌词转PPT工具"
echo "==============================================="
echo ""
echo "正在启动程序..."
echo ""

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo -e "${RED}===============================================${NC}"
    echo -e "${RED}[错误] 未检测到Java运行环境！${NC}"
    echo -e "${RED}===============================================${NC}"
    echo ""
    echo "请先安装Java 8或更高版本"
    echo ""
    echo "Mac用户安装方法："
    echo "  brew install openjdk"
    echo ""
    echo "Linux用户安装方法："
    echo "  sudo apt install openjdk-11-jdk  # Ubuntu/Debian"
    echo "  sudo yum install java-11-openjdk # CentOS/RHEL"
    echo ""
    echo "或访问：https://adoptium.net/"
    echo ""
    echo "==============================================="
    echo ""
    read -p "按Enter键退出..."
    exit 1
fi

# 显示Java版本
echo -e "${GREEN}检测到Java版本：${NC}"
java -version 2>&1 | head -n 1
echo ""

# 检查JAR文件是否存在
if [ ! -f "歌词转PPT.jar" ]; then
    echo -e "${RED}===============================================${NC}"
    echo -e "${RED}[错误] 找不到\"歌词转PPT.jar\"文件！${NC}"
    echo -e "${RED}===============================================${NC}"
    echo ""
    echo "请确保以下文件在同一目录下："
    echo "- 启动歌词转PPT.sh"
    echo "- 歌词转PPT.jar"
    echo ""
    read -p "按Enter键退出..."
    exit 1
fi

# 运行程序
echo "正在启动歌词转PPT工具..."
echo ""
java -jar 歌词转PPT.jar

# 检查运行结果
if [ $? -ne 0 ]; then
    echo ""
    echo -e "${RED}===============================================${NC}"
    echo -e "${RED}[错误] 程序运行失败！${NC}"
    echo -e "${RED}===============================================${NC}"
    echo ""
    echo "可能的原因："
    echo "1. JAR文件已损坏"
    echo "2. Java版本太旧（需要Java 8+）"
    echo "3. 系统内存不足"
    echo ""
    echo "请检查以上问题后重试。"
    echo "==============================================="
    echo ""
    read -p "按Enter键退出..."
fi
