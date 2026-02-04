#!/bin/bash

echo "====================================="
echo "歌词转PPT工具 - 启动中..."
echo "====================================="
echo

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误：未检测到Java运行环境！"
    echo "请先安装Java 11或更高版本。"
    echo
    exit 1
fi

# 运行应用
java -jar target/lyrics-to-slides-1.0-SNAPSHOT-jar-with-dependencies.jar

if [ $? -ne 0 ]; then
    echo
    echo "应用运行出错！"
    echo "请确保已使用 'mvn clean package' 构建项目。"
    echo
fi
