## ADDED Requirements

### Requirement: 将 `User` 实体扩展为包含登录字段
`User` 实体 SHALL 包含如下新列：`password_hash`（字符串）、`roles`（字符串或 JSON）、`last_login`（timestamp）。

#### Scenario: 新用户注册
- **WHEN** 新用户注册时提供密码
- **THEN** 系统 SHALL 将密码存为 bcrypt hash 并保存到 `password_hash` 字段

#### Scenario: 读取用户用于认证
- **WHEN** 后端需要校验凭证
- **THEN** 系统 SHALL 使用 `password_hash` 校验密码
