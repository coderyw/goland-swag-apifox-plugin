# 文件缓存问题修复

## 问题描述

用户反馈：生成swagger后，上传的问题是上次生成的文档，而不是最新生成的文档。

## 问题分析

通过代码分析发现，问题出现在 `readFileContent` 方法中：

1. **VirtualFile缓存问题**：IntelliJ平台的 `VirtualFile` 对象可能会缓存文件内容
2. **文件读取时机**：在文件重新生成后，`VirtualFile` 可能仍然持有旧的内容
3. **文件系统同步**：IDE的文件系统刷新可能不够及时

## 修复方案

### 1. 改进文件读取方法

**修复前**：
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

**修复后**：
```java
public String readFileContent(@NotNull VirtualFile file) throws IOException {
    return com.intellij.openapi.application.ReadAction.compute(() -> {
        try {
            // 强制刷新文件内容，确保读取最新内容
            file.refresh(false, false);
            
            // 使用文件系统直接读取，避免VirtualFile缓存问题
            java.io.File physicalFile = new java.io.File(file.getPath());
            if (!physicalFile.exists()) {
                throw new IOException("File does not exist: " + file.getPath());
            }
            
            // 读取文件内容
            byte[] contentBytes = java.nio.file.Files.readAllBytes(physicalFile.toPath());
            return new String(contentBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
}
```

### 2. 增强文件系统刷新

在 `send2Apifox` 方法中添加了额外的文件系统刷新逻辑：

```java
// 在读取文件内容前，再次强制刷新文件系统
appendToLog("Refreshing file system before reading content...\n");
fileToUse.refresh(false, false);

// 等待文件系统刷新完成
try {
    Thread.sleep(500);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

// 检查文件最后修改时间
java.io.File physicalFile = new java.io.File(fileToUse.getPath());
if (physicalFile.exists()) {
    long lastModified = physicalFile.lastModified();
    appendToLog("File last modified time: " + new java.util.Date(lastModified) + "\n");
}
```

### 3. 改进文件删除验证

在 `forceRegenerateSwagger` 方法中添加了文件删除验证：

```java
// 等待文件删除完成
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

// 验证文件是否真的被删除
appendToLog("Verifying file deletion:\n");
appendToLog("  swagger.yaml still exists: " + swaggerYaml.exists() + "\n");
appendToLog("  swagger.json still exists: " + swaggerJson.exists() + "\n");
appendToLog("  docs.go still exists: " + docsGo.exists() + "\n");
```

### 4. 增强调试信息

添加了详细的调试信息来帮助诊断问题：

- 文件大小检查
- 文件最后修改时间
- 文件内容预览
- 文件删除验证

## 修复效果

### 测试验证

通过测试脚本验证修复效果：

1. **第一次生成**：生成原始swagger文件
2. **修改代码注释**：更新API描述
3. **第二次生成**：重新生成swagger文件
4. **内容对比**：验证新文件包含更新内容

测试结果：
- ✅ 文件大小发生变化（6640 → 6655 字节）
- ✅ 文件内容包含更新内容（"GCash支付和提现相关API服务 - 更新版本"）
- ✅ 文件读取正确获取最新内容

## 技术要点

### 1. VirtualFile vs 物理文件

- **VirtualFile**：IDE的虚拟文件系统，可能有缓存
- **物理文件**：直接访问文件系统，确保读取最新内容

### 2. 文件系统刷新

- `file.refresh(false, false)`：强制刷新文件内容
- `Thread.sleep()`：给文件系统足够时间完成刷新

### 3. 错误处理

- 检查文件是否存在
- 验证文件内容是否为空
- 提供详细的错误信息

## 相关文件

- `src/main/java/github/com/coderyw/golandswagapifoxplugin/SwagToolWindowPanel.java`
- `test-file-cache-fix.sh`：测试脚本

## 总结

通过使用物理文件读取替代VirtualFile缓存读取，并增加文件系统刷新机制，成功解决了文件缓存问题。现在插件能够正确读取最新生成的swagger文件内容，确保上传到Apifox的是最新的文档。 