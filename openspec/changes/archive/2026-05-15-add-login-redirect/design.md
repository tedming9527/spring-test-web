## Context

当前前端路由守卫 `RequireAuth` 检测到用户未登录时，直接重定向到 `/auth/login`，未保存用户原本要访问的路径。登录成功后，`LoginPage` 固定跳转到 `/welcome`，导致用户需要重新导航到目标页面。

技术栈：React + TypeScript + React Router v6，已有 `useNavigate`、`useLocation`、`Navigate` 等 API 可直接使用，无需新增依赖。

## Goals / Non-Goals

**Goals:**
- 登录成功后自动跳回用户原本请求的受保护路由
- 无原始路由时降级跳转到 `/welcome`
- 改动最小，仅修改两处前端文件

**Non-Goals:**
- 不处理后端 session 或 cookie 的 redirect 逻辑
- 不支持跨域 redirect 或外部 URL redirect（安全考量）
- 不持久化 redirect 目标到 localStorage（仅内存 state，页面刷新后降级到 `/welcome` 可接受）

## Decisions

### 使用 React Router `location.state` 传递来源路由

React Router v6 的 `<Navigate state={{ from: location }} />` 与 `useLocation().state` 是标准做法，无需额外状态管理（Redux/Context）。

**替代方案**：URL query param（`/auth/login?redirect=/sys2`）—— 被排除，因为 redirect 目标明文暴露在 URL 中，且需要手动编解码，复杂度更高，同时存在开放重定向（Open Redirect）风险。

### 限制 redirect 仅允许相对路径

登录成功时，校验 `from.pathname` 只接受以 `/` 开头的相对路径，防止 Open Redirect 安全漏洞（OWASP A01）。

## Risks / Trade-offs

- **[风险] 页面刷新丢失 redirect 目标** → `location.state` 不持久化，刷新后丢失。可接受，降级到 `/welcome` 对用户影响轻微。
- **[风险] Open Redirect** → 校验 `from.pathname` 必须为相对路径（startsWith `/`），拒绝绝对 URL。
- **[Trade-off]** 改动仅 2 个文件，范围极小，引入风险低。
