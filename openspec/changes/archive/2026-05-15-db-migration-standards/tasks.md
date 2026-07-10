## 1. 规范与工具选型

- [ ] 1.1 决定迁移工具（Flyway 或 Liquibase）并在文档中记录理由
- [ ] 1.2 在仓库中创建迁移脚本目录 `resources/db/migration/` 与回滚目录 `resources/db/rollback/`

## 2. 编写样例迁移（以 `User` 表升级为例）

- [ ] 2.1 编写迁移脚本 `V20260515__add_user_auth_fields.sql`（新增 `password_hash`、`roles`、`last_login`，先 nullable）
- [ ] 2.2 编写回滚脚本 `R20260515__revert_add_user_auth_fields.sql`
- [ ] 2.3 编写迁移说明文档 `docs/db-migrations/add_user_auth_fields.md`（含预检、后检、回滚步骤、停机窗口估计）

## 3. 集成与验证

- [ ] 3.1 在 CI 中加入 staging 迁移与验证步骤（失败则阻止合并）
- [ ] 3.2 在 staging 环境执行迁移并运行验证脚本
- [ ] 3.3 编写恢复演练步骤并进行一次恢复演练

## 4. 部署与运维准备

- [ ] 4.1 更新部署文档，列出迁移负责人与回滚负责人
- [ ] 4.2 提供备份命令示例（mysqldump / pg_dump / 快照）
- [ ] 4.3 在生产发布前创建快照并记录时间点
