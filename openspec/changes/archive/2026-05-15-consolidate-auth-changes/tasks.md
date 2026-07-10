## 1. 后端：用户表与 JWT

- [x] 1.1 在 `src/main/java/.../model/User.java` 中添加字段：`passwordHash`、`roles`、`lastLogin`（先 nullable）
- [x] 1.2 添加数据库迁移脚本（`resources/db/migration` 或手写 SQL），新增列为 nullable
- [x] 1.3 集成 `jjwt`（或 Spring Security JWT）依赖并实现 JWT 工具类（签发/解析/验证）
- [x] 1.4 修改 `AuthController`：在登录成功时返回 `token`（JWT），并在返回体中包含过期信息
- [x] 1.5 修改或新增拦截器（`AuthTokenInterceptor`）以校验 `Authorization: Bearer <token>`

## 2. 前端：登录与 redirect

 [x] 2.1 修改 `RequireAuth`（`frontend/src/App.tsx`）以在重定向到 `/auth/login` 时传递 `state.from = location`
 [x] 2.2 修改 `LoginPage`（`frontend/src/pages/auth/LoginPage.tsx`）以读取 `location.state.from`，校验安全后跳转回来源或 `/welcome`
 [x] 2.3 在登录 API 调用后持久化 `token`（localStorage）并在后续请求中使用 `Authorization` 头
## 3. 迁移与兼容

- [x] 3.1 提供迁移脚本与回滚 SQL
- [x] 3.2 ~~在后端同时支持旧版 demo token（短期）~~ — 不保留旧 token 兼容，直接使用 JWT
- [x] 3.3 在预发布环境执行端到端验证（前端、后端、DB）
  - 运行迁移脚本并确认 user 表新增列存在
  - 启动后端（注入 `JWT_SECRET` 环境变量）并测试 `/auth/login` 返回 JWT
  - 在浏览器中访问 `/sys1`（未登录）→ 验证跳转到 `/auth/login`
  - 登录成功后验证跳转回 `/sys1` 并能调用受保护接口
  - 点击"退出登录"后验证再次访问受保护路由需重新登录

## 4. 测试与部署

- [x] 4.1 编写后端集成测试覆盖 JWT 签发与校验
- [x] 4.2 编写前端 e2e 测试验证 redirect 行为
- [x] 4.3 更新 Dockerfile / CI 流程以包含新的依赖和迁移步骤
