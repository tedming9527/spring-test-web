## ADDED Requirements

### Requirement: 后端使用 JWT 进行认证
系统 SHALL 在 `/auth/login` 成功后返回一个签名的 JWT，JWT SHALL 包含 `sub`（用户 id）、`exp`（过期时间）、`roles`（角色列表）等声明。

#### Scenario: 登录成功返回 JWT
- **WHEN** 用户使用正确凭证请求 `/auth/login`
- **THEN** 系统 SHALL 返回 HTTP 200 并在响应体中包含 `token`（JWT 字符串）

### Requirement: 所有受保护接口使用 Authorization 头校验 JWT
系统 SHALL 校验 `Authorization: Bearer <token>` 头内的 JWT，校验失败 SHALL 返回 401。

#### Scenario: 访问受保护接口且 JWT 合法
- **WHEN** 请求携带合法未过期 JWT
- **THEN** 系统 SHALL 返回资源并视角色授权情况进行响应
