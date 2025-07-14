# 线程安全修复说明

## 问题描述

项目运行时出现以下错误：
```
Failed to run system application: Access is allowed from Event Dispatch Thread (EDT) only; 
If you access or modify model on EDT consider wrapping your code in WriteIntentReadAction or ReadAction; 
see https://jb.gg/ij-platform-threading for details
Current thread: Thread[#3644,ApplicationImpl pooled thread 657,4,main] 2103498127 (EventQueue.isDispatchThread()=false)
SystemEventQueueThread: Thread[#58,AWT-EventQueue-0,6,main] 2125070700
```

## 问题原因

在IntelliJ平台中，所有对数据模型（如VirtualFile、Project等）的访问都必须在正确的线程中执行：
- **EDT (Event Dispatch Thread)**: UI操作和某些只读操作
- **ReadAction**: 读取数据模型的操作
- **WriteAction**: 修改数据模型的操作
- **后台线程**: 长时间运行的操作

原代码在后台线程中直接访问了VirtualFile API，违反了线程安全规则。

## 修复方案

### 1. 修复 send2Apifox 方法

**问题**: 在后台线程中直接访问VirtualFile
**解决方案**: 使用 `ApplicationManager.getApplication().invokeAndWait()` 确保在EDT中执行

```java
public void send2Apifox(VirtualFile projectRoot) {
    // 在EDT中执行所有VirtualFile操作
    ApplicationManager.getApplication().invokeAndWait(() -> {
        try {
            // 强制刷新文件系统
            projectRoot.refresh(true, true);
            
            // 所有VirtualFile操作都在EDT中执行
            VirtualFile docs = projectRoot.findChild("docs");
            // ... 其他操作
            
            // 在后台线程中发送HTTP请求
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                // HTTP请求操作
            });
        } catch (Exception e) {
            // 错误处理
        }
    });
}
```

### 2. 修复 readFileContent 方法

**问题**: 文件读取操作可能不在正确的线程中
**解决方案**: 使用 `ReadAction.compute()` 确保在正确的线程中读取

```java
public String readFileContent(@NotNull VirtualFile file) throws IOException {
    return com.intellij.openapi.application.ReadAction.compute(() -> {
        try {
            byte[] contentBytes = file.contentsToByteArray();
            return new String(contentBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
}
```

### 3. 修复 runCommand 方法

**问题**: 调用send2Apifox时不在正确的线程中
**解决方案**: 使用 `invokeAndWait` 确保在EDT中调用

```java
// 在EDT中调用send2Apifox
ApplicationManager.getApplication().invokeAndWait(() -> {
    send2Apifox(projectRoot);
});
```

## 线程模型说明

### IntelliJ平台线程模型

1. **EDT (Event Dispatch Thread)**
   - 处理所有UI事件
   - 执行UI更新操作
   - 某些只读操作可以在这里执行

2. **ReadAction**
   - 用于读取数据模型
   - 可以在任何线程中执行
   - 不会阻塞其他操作

3. **WriteAction**
   - 用于修改数据模型
   - 必须在EDT中执行
   - 会阻塞其他操作

4. **后台线程**
   - 用于长时间运行的操作
   - 不能直接访问数据模型
   - 需要通过invokeAndWait或invokeLater与EDT交互

### 修复后的线程流程

```
用户点击按钮
    ↓
EDT: 启动后台线程
    ↓
后台线程: 执行swag命令
    ↓
后台线程: 调用invokeAndWait
    ↓
EDT: 执行VirtualFile操作
    ↓
EDT: 启动新的后台线程发送HTTP请求
    ↓
后台线程: 发送HTTP请求
    ↓
后台线程: 使用invokeLater更新UI
    ↓
EDT: 更新UI显示结果
```

## 测试验证

### 测试步骤
1. 在GoLand中安装插件
2. 配置Apifox设置
3. 在Go项目中添加Swagger注释
4. 运行插件生成和上传功能
5. 检查是否还有线程安全错误

### 预期结果
- ✅ 不再出现线程安全错误
- ✅ 插件正常工作
- ✅ UI响应正常
- ✅ 文件操作成功

## 注意事项

1. **性能考虑**: `invokeAndWait` 会阻塞当前线程，只在必要时使用
2. **错误处理**: 确保所有异常都被正确捕获和处理
3. **UI更新**: 所有UI更新都使用 `SwingUtilities.invokeLater`
4. **资源管理**: 确保所有资源都被正确释放

## 相关文档

- [IntelliJ Platform Threading](https://jb.gg/ij-platform-threading)
- [IntelliJ Platform SDK Documentation](https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html)
- [Virtual File System](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html) 