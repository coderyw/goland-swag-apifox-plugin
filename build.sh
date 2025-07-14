#!/bin/bash

# GoLand Swagger to Apifox Plugin 构建脚本

echo "🚀 开始构建 GoLand Swagger to Apifox Plugin..."

# 检查Java版本
echo "📋 检查Java版本..."
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
echo "Java版本: $java_version"

# 检查Gradle
echo "📋 检查Gradle..."
if ! command -v ./gradlew &> /dev/null; then
    echo "❌ Gradle wrapper not found"
    exit 1
fi

# 清理之前的构建
echo "🧹 清理之前的构建..."
./gradlew clean

# 构建插件
echo "🔨 构建插件..."
./gradlew build

# 检查构建结果
if [ $? -eq 0 ]; then
    echo "✅ 插件构建成功!"
    
    # 查找生成的插件文件
    plugin_file=$(find build/distributions -name "*.zip" | head -n 1)
    if [ -n "$plugin_file" ]; then
        echo "📦 插件文件位置: $plugin_file"
        echo "📏 文件大小: $(du -h "$plugin_file" | cut -f1)"
    else
        echo "⚠️  未找到生成的插件文件"
    fi
else
    echo "❌ 插件构建失败!"
    exit 1
fi

# 运行测试
echo "🧪 运行测试..."
./gradlew test

echo "🎉 构建完成!"
echo ""
echo "📝 使用说明:"
echo "1. 在GoLand中打开 File → Settings → Plugins"
echo "2. 点击齿轮图标 → Install Plugin from Disk"
echo "3. 选择生成的插件文件: $plugin_file"
echo "4. 重启GoLand"
echo ""
echo "🔧 配置插件:"
echo "1. 打开 File → Settings → Tools → My Plugin Settings"
echo "2. 填写Apifox配置信息"
echo "3. 检查系统环境状态" 