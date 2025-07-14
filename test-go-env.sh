#!/bin/bash

# Go环境测试脚本

echo "🔍 测试Go环境配置..."

# 检查Go是否安装
echo "📋 检查Go安装..."
if command -v go &> /dev/null; then
    go_version=$(go version)
    echo "✅ Go已安装: $go_version"
    
    # 解析Go版本
    if [[ $go_version =~ go([0-9]+)\.([0-9]+) ]]; then
        major=${BASH_REMATCH[1]}
        minor=${BASH_REMATCH[2]}
        echo "📊 Go版本: $major.$minor"
        
        # 检查版本兼容性
        if [ "$major" -gt 1 ] || ([ "$major" -eq 1 ] && [ "$minor" -ge 16 ]); then
            echo "✅ Go版本兼容 (需要1.16+)"
        else
            echo "❌ Go版本过低，需要1.16或更高版本"
        fi
    else
        echo "⚠️  无法解析Go版本"
    fi
else
    echo "❌ Go未安装"
    exit 1
fi

# 检查Go环境变量
echo ""
echo "📋 检查Go环境变量..."
echo "GOROOT: ${GOROOT:-未设置}"
echo "GOPATH: ${GOPATH:-未设置}"
echo "PATH: $PATH"

# 检查swag工具
echo ""
echo "📋 检查swag工具..."
if command -v swag &> /dev/null; then
    swag_version=$(swag version)
    echo "✅ swag已安装: $swag_version"
else
    echo "❌ swag未安装"
    echo "💡 安装命令: go install github.com/swaggo/swag/cmd/swag@latest"
fi

# 测试swagger生成
echo ""
echo "📋 测试swagger生成..."
if [ -d "src/main/resources/test/test-swag-apifox" ]; then
    cd src/main/resources/test/test-swag-apifox
    
    echo "🧹 清理旧的swagger文件..."
    rm -rf docs/
    
    echo "🔨 生成swagger文档..."
    if command -v swag &> /dev/null; then
        swag init
        if [ $? -eq 0 ]; then
            echo "✅ swagger文档生成成功!"
            
            # 检查生成的文件
            if [ -f "docs/swagger.yaml" ]; then
                echo "📄 生成文件: docs/swagger.yaml"
                echo "📏 文件大小: $(du -h docs/swagger.yaml | cut -f1)"
                echo "📊 文件行数: $(wc -l < docs/swagger.yaml)"
            fi
            
            if [ -f "docs/swagger.json" ]; then
                echo "📄 生成文件: docs/swagger.json"
                echo "📏 文件大小: $(du -h docs/swagger.json | cut -f1)"
            fi
            
            if [ -f "docs/docs.go" ]; then
                echo "📄 生成文件: docs/docs.go"
                echo "📏 文件大小: $(du -h docs/docs.go | cut -f1)"
            fi
        else
            echo "❌ swagger文档生成失败!"
        fi
    else
        echo "❌ swag工具不可用"
    fi
    
    cd ../../../../../
else
    echo "⚠️  测试项目目录不存在"
fi

echo ""
echo "🎉 环境测试完成!" 