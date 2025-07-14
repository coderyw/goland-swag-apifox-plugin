# GoLand Swagger to Apifox Plugin

一个用于GoLand的插件，可以自动生成Swagger文档并上传到Apifox。

## 功能特性

### 🚀 核心功能
- 自动生成Swagger文档（支持YAML和JSON格式）
- 一键上传到Apifox
- 支持多种导入策略（覆盖、合并、保留、新建）
- 实时日志显示

### 🔧 系统兼容性优化
- **Go版本检测**: 自动检测Go版本，要求Go 1.16+
- **环境变量自动配置**: 自动设置GOROOT、GOPATH和PATH
- **Swag工具检查**: 自动检测swag工具是否安装
- **系统状态监控**: 在设置界面实时显示Go和Swag的安装状态

### 📝 文档更新优化
- **强制重新生成**: 每次运行前自动清理旧的swagger文件
- **文件系统同步**: 确保读取到最新生成的文档
- **多格式支持**: 优先使用swagger.yaml，备选swagger.json
- **内容验证**: 检查生成的文档是否为空

## 系统要求

### 必需软件
- **Go**: 版本 1.16 或更高
- **Swag**: 最新版本 (`go install github.com/swaggo/swag/cmd/swag@latest`)
- **GoLand**: 2022.3 或更高版本

### 推荐配置
- **Go**: 1.21+ (推荐使用最新稳定版)
- **Swag**: v1.8.12+ (支持最新的OpenAPI规范)

## 安装配置

### 1. 安装插件
1. 下载插件文件
2. 在GoLand中打开 `File` → `Settings` → `Plugins`
3. 点击齿轮图标 → `Install Plugin from Disk`
4. 选择下载的插件文件

### 2. 配置Apifox
1. 打开 `File` → `Settings` → `Tools` → `My Plugin Settings`
2. 填写以下信息：
   - **API URL**: `https://api.apifox.com/` (默认)
   - **API Key**: 从Apifox获取的API Token
   - **Project ID**: 目标项目的ID
   - **Parent folder ID**: (可选) 指定导入的父文件夹ID

### 3. 检查系统环境
在设置界面中，插件会自动检查：
- ✅ Go版本兼容性
- ✅ Swag工具安装状态
- 点击"检查系统环境"按钮可手动刷新状态

## 使用方法

### 基本使用
1. 在Go项目中添加Swagger注释
2. 点击工具栏的插件图标或使用快捷键
3. 确认生成和上传操作
4. 查看日志输出确认成功

### 高级配置
在设置中可以配置：
- **Endpoint overwrite behavior**: 选择导入策略
  - `Overwrite existing`: 覆盖现有接口
  - `Auto merge`: 自动合并
  - `Keep existing`: 保留现有
  - `Create new`: 创建新的
- **Add base path**: 是否添加基础路径
- **Swag PD**: 在依赖关系文件夹中进行解析

## 故障排除

### 常见问题

#### 1. Go版本不兼容
```
错误: Go version too old, requires Go 1.16+
解决: 升级Go到1.16或更高版本
```

#### 2. Swag工具未安装
```
错误: The system is missing the swag command
解决: 运行 go install github.com/swaggo/swag/cmd/swag@latest
```

#### 3. 文档更新不及时
```
问题: 上传的文档是旧版本
解决: 插件已优化，会自动清理旧文件并强制重新生成
```

#### 4. 环境变量问题
```
问题: Go命令找不到或环境变量错误
解决: 插件会自动配置GOROOT、GOPATH和PATH环境变量
```

### 调试步骤
1. 检查设置界面的系统环境状态
2. 查看工具窗口的详细日志输出
3. 确认Go代码中的Swagger注释格式正确
4. 验证Apifox的API Token和Project ID

## 更新日志

### v1.5 (当前版本)
- ✅ 添加Go版本兼容性检查
- ✅ 优化环境变量自动配置
- ✅ 强制重新生成swagger文档
- ✅ 改进文件系统同步机制
- ✅ 添加系统状态监控界面
- ✅ 增强错误处理和用户反馈

### v1.4
- 基础功能实现
- Apifox集成
- 基本配置界面

## 开发信息

### 技术栈
- **语言**: Java 17
- **框架**: IntelliJ Platform SDK
- **构建工具**: Gradle
- **HTTP客户端**: OkHttp
- **JSON处理**: Gson

### 项目结构
```
src/main/java/github/com/coderyw/golandswagapifoxplugin/
├── MyAction.java              # 主动作类
├── PluginConfigurable.java    # 插件配置
├── PluginSettings.java        # 设置数据模型
├── PluginSettingsForm.java    # 设置界面
├── SwagToolWindowFactory.java # 工具窗口工厂
└── SwagToolWindowPanel.java   # 工具窗口面板
```

## 贡献

欢迎提交Issue和Pull Request来改进这个插件。

## 许可证

MIT License
