## Context

目标是在当前仓库最小改动范围内新增前端 demo 页面，演示登录后如何带 JWT 访问受保护后端接口并在页面间跳转。后端已有 `AuthController`（当前返回 demo token），将复用并按需改为返回 JWT（或新增 demo endpoint）以进行验证。

## Goals / Non-Goals

**Goals:**
- 快速搭建前端 demo 页面并验证 JWT 在请求头中正确发送
- 保持改动局部化，优先改前端并新增 demo 后端接口

**Non-Goals:**
- 不在此变更中做完整的密码或用户表迁移（这些在 `consolidate-auth-changes` 中处理）

## Decisions

- 前端将使用 `fetch` 增加 `Authorization` 头；不引入全局 HTTP 客户端库
- 新增 demo 受保护端点 `/api/demo/protected`，仅用于演示与测试
- 提供两种验证方式：手动 curl 示例 与 浏览器页面交互（页面显示接口返回的用户名或 200/401）

## Risks / Trade-offs

- 如果后端还未返回真正 JWT，demo 可对接现有简单 token 或短期临时 JWT 实现
- 该变更会修改前端路由，需保留原有路由不变以便回退

## Migration Plan

- 在 feature 分支实现前端页面与 demo endpoint
- 在本地与 staging 上验证 JWT header 被发送并后端返回 200

