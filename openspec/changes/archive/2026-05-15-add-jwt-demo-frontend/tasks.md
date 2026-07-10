## 1. 前端：新增 demo 页面与路由

- [ ] 1.1 新增页面 `frontend/src/pages/demo/ADemo.tsx`（显示 "Demo A" 并含向受保护接口请求按钮）
- [ ] 1.2 新增页面 `frontend/src/pages/demo/BDemo.tsx`（显示 "Demo B" 并含跳回 A 的按钮）
- [ ] 1.3 在 `frontend/src/App.tsx` 中注册 `/demo/a` 和 `/demo/b` 路由并在导航中加入链接

## 2. 登录与 token 使用

- [ ] 2.1 确保 `LoginPage` 在登录成功后保存 `token`（localStorage）
- [ ] 2.2 在 demo 页面调用受保护接口时，使用 `fetch` 并设置 `Authorization: Bearer <token>` 头

## 3. 后端：示例受保护端点

- [ ] 3.1 新增或复用后端端点 `/api/demo/protected`，该端点校验 token 并返回 200/401
- [ ] 3.2 如果后端尚未返回 JWT，可临时使 `/auth/login` 返回简短 JWT 用于演示

## 4. 验证

- [ ] 4.1 手动验证：登录 -> 打开 `/demo/a` -> 点击请求按钮 -> 确认返回 200 且页面显示 user
- [ ] 4.2 脚本验证：提供 `curl` 示例或小脚本展示如何获取 token 并调用受保护端点

