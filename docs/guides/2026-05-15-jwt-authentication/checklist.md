# 学习清单：JWT 认证实现

**日期**：2026-05-15  
**问题背景**：需要为 REST API 添加无状态认证，让前端登录后用 token 访问受保护接口。

---

## 一、必须理解的概念

### 1. JWT 结构

```
eyJhbGciOiJIUzI1NiJ9   .   eyJzdWIiOiJ1c2VyMSJ9   .   SflKxwRJSMeKKF2QT4fwpMeJf
      Header（Base64）           Payload（Base64）         Signature（HMAC-SHA256）
```

- **Header**：算法类型（`HS256`）
- **Payload**：Claims，如 `sub`（用户名）、`exp`（过期时间）
- **Signature**：`HMAC-SHA256(base64(header) + "." + base64(payload), secret)`

> 关键：JWT 是**签名**不是**加密**，Payload 内容任何人都能 Base64 解码看到，不要放密码等敏感信息。

### 2. 无状态认证流程

```
前端                          后端
 │                              │
 │── POST /api/auth/login ──────▶│
 │   {username, password}        │  验证密码，生成 JWT
 │◀── {token: "eyJ..."} ────────│
 │                              │
 │  localStorage.setItem(token) │
 │                              │
 │── GET /api/users ────────────▶│
 │   Authorization: Bearer eyJ... │  AuthTokenInterceptor 验证签名+过期
 │◀── [用户列表] ───────────────│
```

### 3. JJWT 库核心 API

```java
// 签发 token
String token = Jwts.builder()
    .setSubject(username)
    .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
    .signWith(key, SignatureAlgorithm.HS256)
    .compact();

// 验证 token
Claims claims = Jwts.parserBuilder()
    .setSigningKey(key)
    .build()
    .parseClaimsJws(token)
    .getBody();
```

### 4. Secret Key 要求

- HS256 要求密钥 ≥ 256 bits（32 字节）
- 生成方法：`openssl rand -base64 32`
- Spring Boot `${JWT_SECRET:default}` 只对**缺失**的环境变量使用默认值，**空字符串**不触发默认值

### 5. HandlerInterceptor 生命周期

```java
preHandle()    // 请求处理前 → 返回 false 则中断，不执行 Controller
postHandle()   // Controller 执行后，视图渲染前
afterCompletion() // 响应发送后（无论成功失败） → 必须在这里清理 ThreadLocal
```

---

## 二、延伸阅读

- [jwt.io（JWT 调试工具）](https://jwt.io)
- [JJWT GitHub](https://github.com/jwtk/jjwt)
- [MDN: Authorization header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Authorization)
