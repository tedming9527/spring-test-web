## Why

数据库升级是高风险操作，尤其在涉及用户模型与认证字段调整时（例如本仓库即将进行 `User` 表扩展与 JWT 引入）。缺乏标准化的迁移文档、可执行脚本与回滚方案会导致服务中断、数据不一致或安全问题。

## What Changes

- 增加统一的数据库升级规范文档（含运行步骤、预检项、回滚策略）
- 提供可执行的迁移脚本和对应的回滚脚本存放位置 `resources/db/migration/`（或 `db/migrations/`）
- 在 CI/CD 中加入自动化校验（在 staging 环境执行迁移并执行验证脚本）
- 针对本次 `User` 表标准化/JWT 引入，预先准备具体迁移示例和降级计划

## Capabilities

### New Capabilities

- `db-migration-standards`: 标准化迁移流程、文档、脚本与回滚策略

### Modified Capabilities

- `user-table-standardization` (from `consolidate-auth-changes`): 在执行前需遵守本迁移标准并提供 migration + rollback

## Impact

- **后端**: 需新增迁移脚本目录与脚本，代码中新增字段先设为 nullable，后端服务启动时读取迁移状态或通过 CI 控制切换时机
- **部署/运维**: 需要预备数据库备份/快照流程、维护维护窗口、并在 CI/CD 中加入可验证步骤
- **团队流程**: 发布时需包含迁移执行负责人、回滚负责人与验证人员名单
