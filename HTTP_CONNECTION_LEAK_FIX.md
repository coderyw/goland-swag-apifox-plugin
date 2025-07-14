# HTTP连接泄漏修复说明

## 问题描述

插件运行时出现以下警告：
```
2025-07-14 22:41:26,148 [  77733]   WARN - okhttp3.OkHttpClient - A connection to https://api.apifox.com/ was leaked. Did you forget to close a response body? To see where this was allocated, set the OkHttpClient logger level to FINE: Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
```

## 问题原因

在 `sendPostRequest` 方法中，OkHttp 的响应体（ResponseBody）没有被正确关闭。当调用 `response.body().string()` 读取响应内容后，响应体仍然保持打开状态，导致连接泄漏。

## 修复方案

### 修复前的问题代码
```java
public void sendPostRequest(String url, String auth, String jsonData) throws IOException {
    // ... 构建请求 ...
    
    try {
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("HTTP POST request failed with response code: " + response.code());
        }
        String bodyStr = response.body().string(); // 读取响应体但没有关闭
        JSONObject jsonObject = new JSONObject(bodyStr);
        parseResult(jsonObject);
    } catch (IOException e) {
        // 错误处理
    }
    // Response 和 ResponseBody 没有被关闭，导致连接泄漏
}
```

### 修复后的正确代码
```java
public void sendPostRequest(String url, String auth, String jsonData) throws IOException {
    // ... 构建请求 ...
    
    try (Response response = client.newCall(request).execute()) { // 使用 try-with-resources
        if (!response.isSuccessful()) {
            throw new IOException("HTTP POST request failed with response code: " + response.code());
        }
        
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String bodyStr = responseBody.string();
            JSONObject jsonObject = new JSONObject(bodyStr);
            parseResult(jsonObject);
        } else {
            throw new IOException("Response body is null");
        }
    } catch (IOException e) {
        // 错误处理
    }
    // Response 和 ResponseBody 会自动关闭
}
```

## 修复要点

### 1. 使用 try-with-resources
- 将 `Response` 对象放在 try-with-resources 语句中
- 确保在方法结束时自动关闭响应

### 2. 空值检查
- 添加对 `ResponseBody` 的空值检查
- 避免在响应体为 null 时调用 `string()` 方法

### 3. 自动资源管理
- try-with-resources 确保即使发生异常也能正确关闭资源
- 符合 Java 7+ 的最佳实践

## 技术细节

### OkHttp 资源管理
- `Response` 对象包含底层的网络连接
- `ResponseBody` 包含响应数据流
- 必须显式关闭以避免连接泄漏

### 连接池影响
- 未关闭的连接会占用连接池中的连接
- 长时间运行可能导致连接池耗尽
- 影响应用的性能和稳定性

## 测试验证

### 测试步骤
1. 在 GoLand 中安装修复后的插件
2. 配置 Apifox 设置
3. 运行插件生成和上传功能
4. 检查日志中是否还有连接泄漏警告

### 预期结果
- ✅ 不再出现 "connection was leaked" 警告
- ✅ 插件功能正常工作
- ✅ 网络连接正确释放

## 相关文档

- [OkHttp 官方文档 - 响应体](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/)
- [Java try-with-resources 最佳实践](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)

## 版本信息

- **修复版本**: v1.5.1
- **修复日期**: 2025-07-14
- **影响范围**: HTTP 请求处理
- **兼容性**: 向后兼容，无破坏性变更 