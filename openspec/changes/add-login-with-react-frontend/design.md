# 登录功能设计（含 React 前端）

## 总体架构
- 前端：React 应用，包含登录页和欢迎页，通过 fetch/axios 调用后端 API
- 后端：Spring Boot，新增 /api/login POST 接口，校验用户名密码，返回登录结果（token 或 session）
- 部署：Dockerfile 多阶段构建，前端静态文件与后端 jar 一体化部署

## 前端设计
- 登录页（/login）：输入用户名、密码，提交后调用 /api/login
- 欢迎页（/welcome）：登录成功后跳转，显示欢迎信息
- 状态管理：登录成功后保存 token/session（localStorage 或 cookie）
- 路由保护：未登录访问 /welcome 自动跳转到 /login

## 后端设计
- /api/login POST 接口，接收 JSON（username, password）
- 用户校验：内存用户（如 admin/123456），校验通过返回 token（可用 JWT 或简单字符串）
- 登录失败返回 401
- CORS 支持，允许前端跨域请求

## Dockerfile 集成
- 第一阶段：构建 React 前端，输出到 build/
- 第二阶段：复制前端 build 到 Spring Boot resources/static 或 nginx，后端 jar 运行
- EXPOSE 8080，ENTRYPOINT 统一启动

## 安全与扩展
- 仅基础校验，后续可扩展为数据库用户、OAuth、权限体系
- token 方案可升级为 JWT

## 依赖
- React, react-router, axios
- Spring Boot, spring-boot-starter-web, spring-boot-starter-security（可选）

