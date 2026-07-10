## ADDED Requirements

### Requirement: 路由守卫携带来源路径跳转登录页
当用户访问受保护路由时，`RequireAuth` 组件 SHALL 在重定向到 `/auth/login` 时，通过 React Router `Navigate` 的 `state` 属性携带当前 `location` 对象（即 `state={{ from: location }}`），以便登录后可还原原始路由。

#### Scenario: 未登录用户访问受保护路由
- **WHEN** 用户在未登录状态下直接访问 `/sys1`、`/sys2` 等受保护路由
- **THEN** 系统 SHALL 重定向到 `/auth/login`，并在路由 state 中携带 `from.pathname = "/sys1"`（或对应路径）

#### Scenario: 已登录用户访问受保护路由
- **WHEN** 用户已登录（localStorage 存有有效 token）访问受保护路由
- **THEN** 系统 SHALL 正常渲染目标页面，不触发重定向

### Requirement: 登录成功后重定向回原始路由
`LoginPage` 在登录成功后 SHALL 优先从 `location.state.from.pathname` 读取来源路径，并使用 `navigate(from, { replace: true })` 跳回原路由；若来源路径不存在或不合法，SHALL 降级跳转到 `/welcome`。

#### Scenario: 有来源路径时登录成功
- **WHEN** 用户从 `/sys2` 被重定向到登录页后完成登录
- **THEN** 系统 SHALL 自动跳转到 `/sys2`，而非 `/welcome`

#### Scenario: 无来源路径时登录成功
- **WHEN** 用户直接访问 `/auth/login` 并完成登录（无 `location.state.from`）
- **THEN** 系统 SHALL 跳转到 `/welcome`

#### Scenario: 来源路径为绝对 URL（安全校验）
- **WHEN** `location.state.from.pathname` 不以 `/` 开头（异常/伪造情况）
- **THEN** 系统 SHALL 忽略该值，降级跳转到 `/welcome`，防止 Open Redirect 攻击
