## Why

登录后直接跳转到固定页面（`/welcome`），忽略了用户的原始访问意图。当用户访问受保护路由被重定向到登录页时，登录成功后应返回原路由，而不是默认首页，从而减少用户操作步骤，提升体验连贯性。

## What Changes

- `RequireAuth` 路由守卫在重定向到 `/auth/login` 时，通过 React Router 的 `state` 携带当前路径（`from`）
- `LoginPage` 登录成功后，优先读取 `location.state.from` 跳回原路由，若无则降级跳转到 `/welcome`

## Capabilities

### New Capabilities

- `login-redirect`: 登录成功后自动重定向回用户原本请求的受保护路由

### Modified Capabilities

（无现有 spec 需要变更）

## Impact

- **前端文件**: `frontend/src/App.tsx`（`RequireAuth` 组件）、`frontend/src/pages/auth/LoginPage.tsx`（登录成功跳转逻辑）
- **依赖**: 无新增依赖，仅使用已有的 `react-router-dom` 的 `useLocation` / `Navigate` state 传参能力
- **后端**: 无影响
