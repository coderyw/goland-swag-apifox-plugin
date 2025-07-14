# 🚀 快速开始指南

## 5分钟快速上手

### 第一步：环境准备 (1分钟)

1. **确保 Go 环境**
   ```bash
   go version  # 确保版本 >= 1.16
   ```

2. **安装 Swag 工具**
   ```bash
   go install github.com/swaggo/swag/cmd/swag@latest
   ```

3. **准备 Apifox 信息**
   - 登录 [Apifox](https://apifox.com/)
   - 获取 API Token：[个人设置](https://apifox.com/web/account/tokens)
   - 获取项目 ID：项目设置中查看

### 第二步：安装插件 (1分钟)

1. 在 IntelliJ IDEA 中打开 `Settings/Preferences` → `Plugins`
2. 搜索 "Swag Import Apifox" 并安装
3. 重启 IDE

### 第三步：配置插件 (1分钟)

1. 打开 `Settings/Preferences` → `Tools` → `Swag2Apifox`
2. 填入配置信息：
   - **API Secret Key**: 你的 Apifox API Token
   - **Project ID**: 目标项目 ID
   - **Endpoint Overwrite Behavior**: 选择 "Auto merge"（推荐）

### 第四步：添加代码注释 (1分钟)

在你的 Go 项目中添加 Swagger 注释：

```go
// @title My API
// @version 1.0
// @description 我的API服务
// @host localhost:8080
// @BasePath /api/v1

// @Summary 获取用户信息
// @Description 根据用户ID获取详细信息
// @Tags 用户管理
// @Accept json
// @Produce json
// @Param id path int true "用户ID"
// @Success 200 {object} User
// @Router /users/{id} [get]
func getUser(c *gin.Context) {
    // 你的代码实现
}
```

### 第五步：生成并上传 (1分钟)

1. 打开右侧工具窗口 `Swag2Apifox`
2. 点击 `Generate & Upload` 按钮
3. 查看日志确认成功

## 🎯 常见问题快速解决

### ❌ 问题：提示 "swag command not found"
**✅ 解决：**
```bash
go install github.com/swaggo/swag/cmd/swag@latest
```

### ❌ 问题：提示 "No swagger file found"
**✅ 解决：**
1. 检查 Go 代码注释是否正确
2. 查看详细日志定位问题
3. 确保项目有 `go.mod` 文件

### ❌ 问题：上传失败
**✅ 解决：**
1. 检查 API Token 是否正确
2. 确认 Project ID 存在
3. 检查网络连接

## 📋 检查清单

在开始使用前，请确认：

- [ ] Go 版本 >= 1.16
- [ ] Swag 工具已安装
- [ ] 插件已安装并重启 IDE
- [ ] Apifox 配置已完成
- [ ] Go 代码中有 Swagger 注释
- [ ] 项目有 `go.mod` 文件

## 🎉 恭喜！

现在你已经成功配置了 Swag Import Apifox 插件！每次修改 API 代码后，只需要点击一下按钮就能自动更新 Apifox 中的文档。

## 📚 更多资源

- 📖 [详细使用教程](../README.md)
- 📖 [配置说明](../README.md#配置说明)
- 📖 [常见问题](../README.md#常见问题)
- 🐛 [问题反馈](https://github.com/coderyw/goland-swag-apifox-plugin/issues)

---

💡 **小贴士**：建议在团队中统一使用 "Auto merge" 策略，这样可以避免接口重复，同时保持文档的同步更新。 