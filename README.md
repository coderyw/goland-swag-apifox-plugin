# 🚀 Swag Import Apifox - Go API 文档自动同步工具

[![IntelliJ IDEA Plugin](https://img.shields.io/badge/IntelliJ%20IDEA-Plugin-green.svg)](https://plugins.jetbrains.com/plugin/your-plugin-id)
[![Go Version](https://img.shields.io/badge/Go-1.16+-blue.svg)](https://golang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 一键将 Go 项目中的 Swagger 文档自动生成并同步到 Apifox，让 API 文档管理变得简单高效！

## ✨ 主要特性

- 🎯 **自动生成**：基于 Go 代码注释自动生成 Swagger 文档
- 📤 **一键上传**：将生成的文档直接同步到 Apifox 项目
- 🔄 **智能合并**：支持多种接口合并策略，避免重复
- ⚙️ **灵活配置**：支持自定义上传目录和合并规则
- 📊 **实时反馈**：详细的操作日志和结果统计
- 🛠️ **错误诊断**：智能错误提示和解决方案

## 🚀 快速开始

### 环境要求

- ✅ **Go 环境**：Go 1.16 或更高版本
- ✅ **Swag 工具**：自动安装或手动安装
- ✅ **Apifox 账号**：需要 API Token 和项目 ID
- ✅ **IntelliJ IDEA**：2022.2 或更高版本

### 安装步骤

1. **安装插件**
   - 在 IntelliJ IDEA 中打开 `Settings/Preferences` → `Plugins`
   - 搜索 "Swag Import Apifox" 并安装
   - 重启 IDE

2. **安装 Swag 工具**
   ```bash
   go install github.com/swaggo/swag/cmd/swag@latest
   ```

3. **配置 Apifox**
   - 打开 `Settings/Preferences` → `Tools` → `Swag2Apifox`
   - 填入 API Token 和项目 ID

### 使用示例

#### 1. 添加 Swagger 注释

```go
// @title My API
// @version 1.0
// @description API 服务描述
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
    // 实现代码
}
```

#### 2. 生成并上传文档

1. 打开右侧工具窗口 `Swag2Apifox`
2. 点击 `Generate & Upload` 按钮
3. 查看操作日志，确认上传成功

## ⚙️ 配置说明

### Apifox 配置

| 配置项 | 说明 | 必填 |
|--------|------|------|
| **API Secret Key** | Apifox API Token，在[个人设置](https://apifox.com/web/account/tokens)中获取 | ✅ |
| **Project ID** | 目标项目 ID，在项目设置中查看 | ✅ |
| **Parent Folder ID** | 父级目录 ID，不填则同步到根目录 | ❌ |
| **Endpoint Overwrite Behavior** | 接口合并策略 | ❌ |
| **Add Base Path** | 是否在接口路径前添加基础路径 | ❌ |

### 接口合并策略

- 🔄 **Overwrite Existing**：覆盖现有接口
- 🔗 **Auto Merge**：自动合并（推荐）
- 💾 **Keep Existing**：保留现有接口
- ➕ **Create New**：创建新接口

### Swag 配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| **Parse Dependency** | 是否扫描依赖包中的实体 | false |

## 📖 使用教程

### 完整工作流程

1. **准备 Go 项目**
   ```bash
   # 确保项目有 go.mod 文件
   go mod init myproject
   ```

2. **添加 Swagger 依赖**
   ```bash
   go get github.com/swaggo/swag/cmd/swag
   go get github.com/swaggo/gin-swagger
   go get github.com/swaggo/files
   ```

3. **编写 API 注释**
   ```go
   package main

   import (
       "github.com/gin-gonic/gin"
       swaggerFiles "github.com/swaggo/files"
       ginSwagger "github.com/swaggo/gin-swagger"
       _ "yourproject/docs" // 导入生成的docs包
   )

   // @title Your API
   // @version 1.0
   // @description Your API description
   // @host localhost:8080
   // @BasePath /api/v1
   func main() {
       r := gin.Default()
       
       // Swagger文档路由
       r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
       
       r.Run(":8080")
   }
   ```

4. **配置插件并上传**

### 最佳实践

1. **注释规范**
   - 使用清晰的 API 描述
   - 正确设置请求/响应格式
   - 添加适当的标签分组

2. **配置建议**
   - 使用 "Auto Merge" 策略避免重复
   - 合理设置父级目录组织接口
   - 开启详细日志便于调试

3. **错误处理**
   - 查看详细日志定位问题
   - 检查网络连接和权限
   - 验证配置信息正确性

## 🔧 常见问题

### Q: 提示 "swag command not found"
**A:** 运行以下命令安装 swag 工具：
```bash
go install github.com/swaggo/swag/cmd/swag@latest
```

### Q: 提示 "No swagger file found"
**A:** 检查以下几点：
1. Go 代码中的 Swagger 注释是否正确
2. 查看详细日志定位具体问题
3. 确保项目结构正确

### Q: 上传失败
**A:** 检查以下配置：
1. API Token 是否正确
2. Project ID 是否存在
3. 网络连接是否正常
4. 项目权限是否足够

### Q: 接口重复或冲突
**A:** 调整合并策略：
1. 使用 "Auto Merge" 自动合并
2. 使用 "Create New" 创建新接口
3. 检查接口路径是否重复

## 📚 相关资源

- 📖 [Swag 官方文档](https://github.com/swaggo/swag)
- 📖 [Apifox 帮助文档](https://apifox.com/help)
- 📖 [Go Swagger 教程](https://github.com/swaggo/swag#declarative-comments-format)
- 🐛 [问题反馈](https://github.com/coderyw/goland-swag-apifox-plugin/issues)

## 🎯 适用场景

- Go 微服务 API 文档管理
- 团队协作开发中的接口同步
- API 文档版本控制
- 前后端接口对接
- 自动化文档生成

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- [Swag](https://github.com/swaggo/swag) - Go Swagger 文档生成工具
- [Apifox](https://apifox.com/) - API 文档管理平台
- [IntelliJ Platform](https://plugins.jetbrains.com/) - 插件开发平台

---

⭐ 如果这个项目对你有帮助，请给它一个星标！
