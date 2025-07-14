#!/bin/bash

echo "Testing Swagger Generation Fix"
echo "=============================="

# 检查 Go 是否安装
if ! command -v go &> /dev/null; then
    echo "ERROR: Go is not installed"
    exit 1
fi

echo "✓ Go is installed: $(go version)"

# 检查 swag 是否安装
if ! command -v swag &> /dev/null; then
    echo "Installing swag..."
    go install github.com/swaggo/swag/cmd/swag@latest
fi

echo "✓ Swag is installed: $(swag version)"

# 进入测试项目目录
cd src/main/resources/test/test-swag-apifox

echo "Testing swagger generation in test project..."

# 清理旧的 docs 目录
if [ -d "docs" ]; then
    echo "Cleaning up old docs directory..."
    rm -rf docs
fi

# 生成 swagger 文档
echo "Generating swagger documentation..."
swag init

# 检查生成的文件
echo "Checking generated files..."
if [ -f "docs/swagger.yaml" ]; then
    echo "✓ swagger.yaml generated successfully"
    echo "  Size: $(wc -c < docs/swagger.yaml) bytes"
else
    echo "✗ swagger.yaml not found"
fi

if [ -f "docs/swagger.json" ]; then
    echo "✓ swagger.json generated successfully"
    echo "  Size: $(wc -c < docs/swagger.json) bytes"
else
    echo "✗ swagger.json not found"
fi

if [ -f "docs/docs.go" ]; then
    echo "✓ docs.go generated successfully"
    echo "  Size: $(wc -c < docs/docs.go) bytes"
else
    echo "✗ docs.go not found"
fi

echo ""
echo "Test completed. Check the output above for any errors." 