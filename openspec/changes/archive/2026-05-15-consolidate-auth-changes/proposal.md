## Why

当前仓库存在多项相关但分散的认证/登录工作流变更（例如 `add-login-with-react-frontend`、`add-login-redirect`、`upgrade-user-table-with-jwt-auth`）。这些变更在实现上存在重叠（前端登录/redirect、后端 JWT 签发与校验、用户表模型调整），分散处理会导致重复工作、版本不一致和发布复杂度。将相关需求合并为一个变更可以统一设计、协调迁移计划并减少回归风险。

## What Changes

- 合并并统一实现用户表升级（包含登录信息字段）与 JWT 认证/授权
- 将前端登录跳转逻辑（login redirect）作为统一认证体验的一部分实现
- 增加后端 JWT 签发与全局校验拦截器，替换当前演示用的简单字符串 token
- 添加数据库迁移脚本和兼容性策略以保留现有用户数据

## Capabilities

### New Capabilities
- `user-auth-jwt`: 后端使用 JWT 签发和验证用户认证信息（包含过期、签名、角色）
- `login-redirect`: 前端支持登录成功后重定向回原始受保护路由的能力
- `user-table-standardization`: 将 `User` 表扩展为包含登录所需字段（密码哈希、salt/algorithm、角色、lastLogin）并建立迁移策略

### Modified Capabilities
- `add-login-with-react-frontend` (合并入本变更)：将原变更的实现与本次统一设计对齐

## Impact

- **前端**: `frontend/src/pages/auth/LoginPage.tsx`、`frontend/src/App.tsx`（路由守卫与跳转）
- **后端**: `AuthController`、拦截器（`AuthTokenInterceptor`）与 `User` 实体及 `UserRepository` 的修改
- **数据库**: 需要新增列与迁移脚本（兼容现有数据）
- **部署**: 需要滚动部署策略以避免短时间不可用或认证中断
