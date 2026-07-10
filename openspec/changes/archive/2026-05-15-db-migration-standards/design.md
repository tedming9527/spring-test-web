## Context

本设计为项目级数据库迁移标准，目标是提供可重复、可验证、可回滚的迁移流程，降低因数据模型变更导致的生产事故风险。将为 `User` 表升级与 JWT 引入提供特定示例，但规范可复用于未来任何表结构变更。

## Goals / Non-Goals

**Goals:**
- 定义迁移脚本规范（位置、命名、幂等性、回滚脚本）
- 提供运行前检查与运行后验证脚本
- 集成 CI 流程以在 staging 环境自动执行迁移并验证

**Non-Goals:**
- 自动化备份存储（仅定义备份步骤与验证，不实现外部备份托管）

## Decisions

- 使用 Flyway（或 Liquibase）作为主流迁移管理工具，并同时保留手写 SQL 脚本作为可审计备选；首选 Flyway 以便与 CI 集成。
- 所有迁移脚本放置在 `resources/db/migration/`（后端仓库）并以 `V<timestamp>__<description>.sql` 命名；回滚脚本放在 `resources/db/rollback/` 对应命名 `R<timestamp>__<description>.sql`。
- 每个迁移必须伴随：
  - 变更说明（markdown，`docs/db-migrations/<name>.md`）
  - 运行前检查脚本（SQL 或小脚本）
  - 运行后验证脚本（assertions）
  - 明确的回滚步骤和回滚脚本

## Migration Plan

1. 开发分支准备阶段：编写迁移脚本与回滚脚本，编写说明文档并在 PR 中包含迁移影响评估
2. Staging 执行：在 staging 环境通过 CI 自动运行迁移、执行验证脚本，收集结果
3. Canary/灰度：在小流量环境执行迁移并监控
4. Production 运行：选择维护窗口、先执行备份（物理或逻辑）、执行迁移、执行验证脚本、若失败则执行回滚脚本

## Rollback Strategy

- 回滚脚本必须可以将 schema 回退到迁移前状态，并包含数据恢复建议（如从备份恢复、或迁移中生成的补偿脚本）
- 在无法无损回滚（例如删列导致数据丢失）的情况下，回滚策略需依赖前置备份来恢复被删除数据

## Open Questions

- 是否正式采用 Flyway？（若否，确定替代工具）
- 是否需要为每次迁移提供预估停机时间？
