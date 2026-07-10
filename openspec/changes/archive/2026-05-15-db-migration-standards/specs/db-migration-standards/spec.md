## ADDED Requirements

### Requirement: 每次数据库变更必须提供可执行的迁移脚本与回滚脚本
每个 schema 变更 SHALL 附带两个 SQL 脚本：一个用于迁移（`V...sql`），一个用于回滚（`R...sql`），并放置在约定目录。脚本 SHALL 明确记录作者、目的、影响范围与运行顺序。

#### Scenario: 提交 PR 包含迁移
- **WHEN** 开发提交 PR 修改涉及 schema
- **THEN** PR SHALL 包含迁移脚本、回滚脚本、以及 `docs/db-migrations/<name>.md` 的变更说明

### Requirement: 迁移需可自动化验证
每个迁移 SHALL 包含运行后验证脚本，验证包括数据完整性、索引存在性及关键查询结果一致性。

#### Scenario: Staging 验证
- **WHEN** CI 在 staging 环境执行迁移
- **THEN** 系统 SHALL 执行验证脚本并在失败时阻止合并或标记为失败
