# Code Review 指南：SPA 路由 + 静态资源 MIME 处理

**日期**：2026-05-15  
**涉及文件**：
- [filter/SpaRoutingFilter.java](../../../src/main/java/org/example/springtestweb/filter/SpaRoutingFilter.java)
- [configurer/WebConfigurer.java](../../../src/main/java/org/example/springtestweb/configurer/WebConfigurer.java)

---

## 架构图

```
浏览器请求
    │
    ▼
SpaRoutingFilter（@Order HIGHEST_PRECEDENCE）
    │
    ├─ path.startsWith("/api/")     → filterChain.doFilter()
    │                                    └─ Spring MVC Controller
    │
    ├─ hasFileExtension(path)       → filterChain.doFilter()
    │   (.js/.css/.png 等)               └─ ResourceHttpRequestHandler
    │                                         ├─ 自动 Content-Type（MediaTypeFactory）
    │                                         ├─ /assets/** → Cache immutable 1年
    │                                         ├─ /index.html → Cache no-cache
    │                                         └─ 文件不存在 → 404（不返回 HTML）
    │
    └─ 其他路径（/welcome 等）      → forward("/index.html")
                                         └─ React Router 渲染对应组件
```

---

## 检查点清单

### SpaRoutingFilter

- [ ] **放行条件是否完整？**  
  当前只放行 `/api/**` 和含扩展名路径。  
  如果新增以下端点，需要在此补充：
  - `/actuator/**`（Spring Boot Actuator）
  - `/swagger-ui/**`、`/v3/api-docs/**`（Swagger/OpenAPI）
  - `/error`（Spring 默认错误端点）

- [ ] **`hasFileExtension` 边界情况验证**

  | 输入路径 | `filename` | 返回值 | 预期 |
  |---|---|---|---|
  | `/assets/index-abc.js` | `index-abc.js` | `true` | ✅ 静态资源 |
  | `/welcome` | `welcome` | `false` | ✅ SPA 路由 |
  | `/` | `""` | `false` | ✅ SPA 路由 |
  | `/path.with.dots/page` | `page` | `false` | ✅ 只看最后一段 |
  | `/file.` | `file.` | `true` | ⚠️ 极端情况，实际不影响 |

- [ ] **`@Order(Ordered.HIGHEST_PRECEDENCE)` 是否足够早？**  
  如果引入 Spring Security，Security Filter Chain 默认优先级为 `-100`，  
  `HIGHEST_PRECEDENCE = Integer.MIN_VALUE`（更早），目前没问题。  
  引入 Security 后需要重新确认这里的顺序是否符合预期。

- [ ] **forward 后响应是否被提前 commit？**  
  `forward` 前不能已经调用过 `response.getWriter().write()` 或 `response.flushBuffer()`，  
  否则会抛 `IllegalStateException`。当前代码无此问题。

---

### WebConfigurer

- [ ] **资源路径是否与 Dockerfile COPY 目标一致？**
  ```dockerfile
  # Dockerfile 中
  COPY --from=frontend-build /frontend/dist/ src/main/resources/static/
  ```
  ```java
  // WebConfigurer 中
  .addResourceLocations("classpath:/static/assets/")  // 对应 dist/assets/
  .addResourceLocations("classpath:/static/")          // 对应 dist/
  ```

- [ ] **Vite 输出目录是否有变更？**  
  检查 `frontend/vite.config.ts` 中 `build.outDir`（默认 `dist`）和 `build.assetsDir`（默认 `assets`）。  
  若有自定义，`addResourceLocations` 和 `SpaRoutingFilter` 中的 `hasFileExtension` 判断均需同步更新。

- [ ] **`excludePathPatterns` 是否覆盖所有静态资源路径？**
  ```java
  String[] staticPaths = {
      "/assets/**", "/index.html",
      "/*.ico", "/*.svg", "/*.png", "/*.webmanifest", "/*.txt"
  };
  ```
  若 Vite 输出了新类型文件（如 `/fonts/**`），需要在这里补充，否则 `logInterceptor` 会对这些资源打日志。

- [ ] **缓存策略是否适合当前部署方式？**  
  `immutable` 缓存依赖文件名含哈希来实现缓存失效。  
  若 `index.html` 中的 `<script src>` 和 `<link href>` 引用了哈希文件名，则缓存策略正确。  
  验证方法：查看 `frontend/dist/index.html` 中的资源引用。

---

## 常见错误排查

| 症状 | 可能原因 | 排查方法 |
|---|---|---|
| JS 返回 `text/html` | `SpaRoutingFilter` 的 `hasFileExtension` 未匹配到该路径 | 在 filter 中加日志打印 `path` |
| `/welcome` 直接返回 404 | `SpaRoutingFilter` 未注册（`@Component` 缺失）或 `forward` 目标 `/index.html` 不存在 | 检查 jar 内是否包含 `static/index.html` |
| 静态资源返回 404 | `addResourceLocations` 路径与 jar 内实际路径不符 | `jar tf app.jar \| grep index.html` |
| 修改代码后浏览器不更新 | `index.html` 被浏览器缓存（应为 no-cache） | 硬刷新（Ctrl+Shift+R），检查响应头 |
