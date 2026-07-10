## Why

需要一个小型端到端演示：通过新增若干前端页面并在登录后互相跳转，验证后端 JWT 签发与前端 `Authorization: Bearer <token>` 使用是否生效。该演示可作为 smoke-test 与团队示例，帮助验证合并后的 JWT 与前端跳转逻辑正确。

## What Changes

- 在前端新增示例页面（如 `/demo/a`、`/demo/b`），页面间可互相跳转并调用受保护后端接口
- 修改前端登录逻辑以在登录后存储 JWT 并在后续请求中带上 `Authorization` 头
- 后端更新 `AuthController`（或新增测试端点）以接受并验证 JWT
- 添加一个简单的 e2e 验证脚本或说明，证明 JWT 有效

## Capabilities

### New Capabilities
- `jwt-e2e-demo`: 前端示例页面与自动化或手动验证流程，用于证明 JWT 签发与校验生效

### Modified Capabilities
- `login-redirect`: 结合已有 redirect 逻辑确保登录后能返回 demo 页面

## Impact

- **前端**: 新增页面 `frontend/src/pages/demo/ADemo.tsx`、`BDemo.tsx`，并在 `App.tsx` 中注册路由；修改 `LoginPage` 以存 token 并在 fetch 请求中附带 `Authorization`
- **后端**: 可复用已有 `AuthController`，或新增 `/api/demo/protected` 端点用于验证 token
- **测试**: 增加手动/脚本化验证说明或小脚本（curl 或 node）来证明 JWT 生效
