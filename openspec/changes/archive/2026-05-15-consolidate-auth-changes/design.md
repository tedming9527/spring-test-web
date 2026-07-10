## Context

本变更合并前端和后端的认证相关工作：前端登录与 redirect、后端 JWT 签发与校验、以及 `User` 表的标准化。现有代码包含示例级实现（如演示 token 字符串、简易 `User` 实体），需要在保证向后兼容的前提下完成生产级改造。

技术栈：Spring Boot（Jakarta/JPA）、React + React Router v6、SQLite/MySQL 等支持的关系型数据库。

## Goals / Non-Goals

**Goals:**
- 以最小破坏策略引入 JWT 认证，支持逐步切换
- 为登录功能提供安全的 redirect 行为（防止 Open Redirect）
- 标准化 `User` 表，添加密码哈希与角色字段，并提供迁移脚本

**Non-Goals:**
- 不实现单点登录（SSO）跨域信任本次变更不涉及多个独立服务的 SSO 协议实现

## Decisions

1. JWT 库选择：使用 `io.jsonwebtoken:jjwt` 或 Spring Security JWT 支持。选择 `jjwt` 作为轻量方案，便于手动处理 token 签名与过期策略。
2. 密码存储：使用 BCrypt 哈希（`BCryptPasswordEncoder`），不存明文密码。
3. 数据库迁移：使用 SQL migration 脚本（手写或通过 Flyway/Liquibase），在变更脚本中先新增列设为 nullable，应用上线后逐步填充，再改为 non-null 并移除旧列（如果有）。
4. 前端 redirect：使用 `location.state.from` 传递来源并在登录成功后校验 `from.pathname.startsWith('/')`，防止开放重定向。

## Risks / Trade-offs

- [风险] 数据迁移错误导致用户登录失败 → Mitigation: 在预发布环境先运行迁移并验证登录流程，保持旧 token 兼容 7 天。
- [风险] JWT 签名泄露 → Mitigation: 使用环境变量管理密钥，并短期内轮换密钥策略

## Migration Plan

1. 添加新列（password_hash, roles, last_login）为 nullable 并部署后端
2. 提供脚本将现有用户转换为带默认密码策略或触发重设密码流程
3. 启用 JWT 签发（后端同时支持旧 token 字符串与 JWT，短期兼容）
4. 在完成客户端升级且大部分用户已迁移后，切换为仅接受 JWT

## Open Questions

- 是否使用 Flyway/Liquibase 之一来管理迁移？
- 是否要求所有现有用户强制重置密码，或允许后台自动迁移（更好体验但存在安全权衡）
