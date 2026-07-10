## ADDED Requirements

### Requirement: 前端登录后重定向回来源路由
`LoginPage` SHALL 在登录成功后优先使用 `location.state.from.pathname` 跳回原始路由；若不存在或非法（非以 `/` 开头），SHALL 跳转到 `/welcome`。

#### Scenario: 有来源路径时登录成功
- **WHEN** 用户从 `/sys2` 被重定向到登录页后完成登录
- **THEN** 系统 SHALL 自动跳转到 `/sys2`

#### Scenario: 来源路径为绝对 URL（安全校验）
- **WHEN** `location.state.from.pathname` 不以 `/` 开头
- **THEN** 系统 SHALL 忽略该值并跳转到 `/welcome`，防止开放重定向攻击
