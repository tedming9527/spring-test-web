## ADDED Requirements

### Requirement: 前端页面在登录后使用 JWT 访问受保护端点
前端示例页面 SHALL 在调用受保护端点时通过 `Authorization: Bearer <token>` 发送 token，后端 SHALL 验证 token 有效性并返回 HTTP 200，或在无效/缺失时返回 401。

#### Scenario: 登录后访问受保护端点成功
- **WHEN** 用户登录并前端在后续请求中携带后端返回的 token
- **THEN** 受保护端点 SHALL 返回 HTTP 200 与示例内容（例如 `"ok": true, "user": "admin"`）

#### Scenario: 无或无效 token 时访问受保护端点
- **WHEN** 前端请求未附带 token 或 token 无效
- **THEN** 后端 SHALL 返回 HTTP 401
