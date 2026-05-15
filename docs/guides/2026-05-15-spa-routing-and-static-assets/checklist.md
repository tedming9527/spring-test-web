# 学习清单：SPA 路由 + 静态资源 MIME 处理

**日期**：2026-05-15  
**问题背景**：React SPA 部署到 Spring Boot 后，直接访问 `/welcome` 返回 404；`/assets/index-xxx.js` 返回 `text/html`，浏览器拒绝执行。

---

## 一、必须理解的概念

### 1. Filter vs Interceptor（最重要的区别）

```
浏览器
  │
  ▼
Servlet Filter（容器层，Spring 之前）
  │  ← SpaRoutingFilter 在这里
  ▼
DispatcherServlet（Spring MVC 入口）
  │
  ▼
HandlerInterceptor（Spring MVC 层）
  │  ← LogInterceptor / AuthTokenInterceptor 在这里
  ▼
Controller
```

- **Filter**：Servlet 规范定义，处理所有请求（包括静态资源），可以 forward/redirect
- **Interceptor**：Spring MVC 定义，只处理经过 DispatcherServlet 的请求

> 问：为什么 SPA 路由用 Filter 而不是 Interceptor？  
> 答：Interceptor 无法拦截静态资源请求（ResourceHandler 绕过了 Controller 流程），用 Filter 才能在最早的位置做分流。

### 2. `OncePerRequestFilter`

Spring 提供的 Filter 基类，保证 `doFilterInternal` 每个请求只执行一次（`forward` 会触发新的 Filter 链，`OncePerRequestFilter` 内部通过 request attribute 防止重复执行）。

```java
// 继承它，只需实现这一个方法
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ...
```

### 3. `RequestDispatcher.forward()`

服务端内部转发：
- URL 不变（浏览器地址栏仍显示 `/welcome`）
- 响应内容换成 `/index.html` 的内容
- React Router 读取地址栏 URL，渲染 `WelcomePage` 组件

```java
request.getRequestDispatcher("/index.html").forward(request, response);
```

### 4. MIME 类型与 Strict MIME Checking

浏览器对 `<script type="module">` 强制校验 Content-Type，必须为 `text/javascript`。  
Spring 的 `ResourceHttpRequestHandler` 通过 `MediaTypeFactory` 自动推断：

| 文件扩展名 | Content-Type |
|---|---|
| `.js` / `.mjs` | `text/javascript` |
| `.css` | `text/css` |
| `.woff2` | `font/woff2` |
| `.svg` | `image/svg+xml` |
| `.webmanifest` | `application/manifest+json` |
| `.png` / `.jpg` | `image/png` / `image/jpeg` |

### 5. HTTP 缓存策略

```
Cache-Control: max-age=31536000, immutable
```
- `max-age=31536000`：缓存 1 年（秒）
- `immutable`：告诉浏览器文件内容永不变，无需发 If-None-Match 确认请求
- 适用于：Vite 构建的 `/assets/index-DPJ0GAa3.js`（文件名含内容哈希）

```
Cache-Control: no-cache
```
- 每次使用前向服务器确认是否有新版本（发 304 条件请求）
- 适用于：`/index.html`（SPA 入口，必须始终是最新版）

### 6. Vite 构建产物结构

```
dist/
├── index.html           ← SPA 入口，no-cache
└── assets/
    ├── index-DPJ0GAa3.js   ← 含哈希，immutable 缓存
    └── index-COJ16oVQ.css  ← 含哈希，immutable 缓存
```

Dockerfile 中：
```dockerfile
COPY --from=frontend-build /frontend/dist/ src/main/resources/static/
```
→ 打包进 jar 后路径：`BOOT-INF/classes/static/`  
→ Spring 通过 `classpath:/static/` 访问

---

## 二、延伸阅读

- [Spring ResourceHttpRequestHandler 文档](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/resource/ResourceHttpRequestHandler.html)
- [MDN: MIME types](https://developer.mozilla.org/en-US/docs/Web/HTTP/MIME_types)
- [MDN: Cache-Control](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control)
- [MDN: History API（SPA 路由原理）](https://developer.mozilla.org/en-US/docs/Web/API/History_API)
