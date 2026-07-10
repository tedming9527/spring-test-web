## 1. 修改路由守卫（RequireAuth）

- [ ] 1.1 在 `frontend/src/App.tsx` 中，为 `RequireAuth` 组件添加 `useLocation` 导入
- [ ] 1.2 修改 `RequireAuth` 中的 `<Navigate to="/auth/login" replace />` 为 `<Navigate to="/auth/login" state={{ from: location }} replace />`

## 2. 修改登录成功跳转逻辑（LoginPage）

- [ ] 2.1 在 `frontend/src/pages/auth/LoginPage.tsx` 中，从 `react-router-dom` 导入 `useLocation`
- [ ] 2.2 在 `LoginPage` 组件中调用 `useLocation()` 获取 `location`
- [ ] 2.3 从 `location.state` 中读取 `from.pathname`，校验其以 `/` 开头（防止 Open Redirect）
- [ ] 2.4 将 `navigate("/welcome")` 替换为 `navigate(from, { replace: true })`，`from` 为校验后的路径或默认值 `/welcome`

## 3. 验证

- [ ] 3.1 未登录时访问 `/sys1`，确认重定向到 `/auth/login` 且登录后自动跳回 `/sys1`
- [ ] 3.2 直接访问 `/auth/login` 登录，确认跳转到 `/welcome`
- [ ] 3.3 确认已登录用户访问受保护路由不受影响
