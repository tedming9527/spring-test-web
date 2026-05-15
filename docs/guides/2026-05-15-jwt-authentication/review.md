# Code Review 指南：JWT 认证实现

**日期**：2026-05-15  
**涉及文件**：
- `src/main/java/.../interceptor/AuthTokenInterceptor.java`
- `src/main/java/.../interceptor/TimeInterceptor.java`
- `src/main/java/.../controller/AuthController.java`
- `src/main/resources/application.yml`

---

## 架构图

```
POST /api/auth/login
    └─ AuthController.login()
         ├─ 验证用户名密码
         ├─ JwtUtil.generateToken(username)
         └─ 返回 {token, expiresIn}

GET /api/**（受保护接口）
    └─ AuthTokenInterceptor.preHandle()
         ├─ 读取 Authorization: Bearer <token>
         ├─ JwtUtil.isTokenValid(token)
         │   ├─ 验证签名
         │   └─ 验证过期时间
         ├─ valid → return true（继续）
         └─ invalid → response 401 + return false（中断）
```

---

## 检查点清单

### AuthTokenInterceptor

- [ ] **只保护 `/api/**`，非 API 路径不鉴权**
  ```java
  registry.addInterceptor(authTokenInterceptor).addPathPatterns("/api/**");
  ```
  登录接口 `/api/auth/login` 本身不需要 token，检查 `preHandle` 中是否排除了它。

- [ ] **401 响应是否正确写入**  
  `response.setStatus(401)` 之后必须 `return false`，否则请求继续执行。

- [ ] **JWT 异常是否被捕获**  
  `parseClaimsJws` 可能抛出：
  - `ExpiredJwtException`：token 过期
  - `MalformedJwtException`：token 格式错误
  - `SignatureException`：签名不匹配  
  这些都属于预期的"无效 token"情况，应 catch 后返回 401，不应让异常向上传播。

### TimeInterceptor（ThreadLocal 使用规范）

- [ ] **`afterCompletion` 中必须 `remove()`**
  ```java
  @Override
  public void afterCompletion(...) {
      // ✅ 正确：处理 postHandle 被跳过的情况
      LocalTime start = threadLocalStart.get();
      if (start != null) { ... }
      // ✅ 必须：防止线程池复用时 ThreadLocal 污染
      threadLocalStart.remove();
      threadLocalEnd.remove();
  }
  ```
  Tomcat 使用线程池，不 `remove()` 会导致下一个请求读到旧数据。

- [ ] **`postHandle` 被跳过时不能 NPE**  
  `AuthTokenInterceptor` 返回 `false` 时，`postHandle` 不执行，  
  `threadLocalEnd` 为 null。`afterCompletion` 中必须用 null check 或 `LocalTime.now()` 兜底。

### application.yml

- [ ] **默认 secret 长度是否 ≥ 32 字节**
  ```yaml
  jwt:
    secret: ${JWT_SECRET:default-secret-change-me-in-prod-32x}
  ```
  `default-secret-change-me-in-prod-32x` = 38 字节 ✅

- [ ] **生产环境是否通过环境变量注入真实 secret**  
  CI/CD 中检查 `JWT_SECRET` 是否在 GitHub Secrets 中配置，且不为空字符串。

---

## 常见错误排查

| 症状 | 可能原因 | 排查方法 |
|---|---|---|
| 启动时 `WeakKeyException` | secret < 32 字节 | `echo -n "your-secret" \| wc -c` |
| 登录返回 401 | 密码验证逻辑错误 | 加日志打印入参和数据库查询结果 |
| 所有请求返回 401 | `JWT_SECRET` 环境变量为空字符串 | `docker exec <container> env \| grep JWT` |
| token 验证失败 | 签发和验证用了不同 key | 确认 `JwtUtil` 是单例，key 只初始化一次 |
