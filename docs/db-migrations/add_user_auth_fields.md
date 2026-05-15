# Migration: add_user_auth_fields

Purpose
- Add `password_hash`, `roles`, and `last_login` columns to `user` table. Columns are initially nullable to avoid blocking existing data.

Pre-checks
- Ensure recent backup exists (mysqldump or snapshot)
- Verify no long-running transactions
- Verify staging migration passed validation scripts

Run
- Apply migration script:

```sql
-- from src/main/resources/db/migration/V20260515__add_user_auth_fields.sql
ALTER TABLE `user`
  ADD COLUMN `password_hash` VARCHAR(255) NULL,
  ADD COLUMN `roles` VARCHAR(255) NULL,
  ADD COLUMN `last_login` DATETIME NULL;
```

Post-checks
- Verify columns exist: `SHOW COLUMNS FROM user` and check `password_hash`, `roles`, `last_login`
- Run validation queries (e.g., sample selects)

Rollback
- If needed, run rollback script (from `resources/db/rollback`):

```sql
ALTER TABLE `user`
  DROP COLUMN IF EXISTS `password_hash`,
  DROP COLUMN IF EXISTS `roles`,
  DROP COLUMN IF EXISTS `last_login`;
```

Notes
- Dropping columns may lead to irreversible data loss. Always rely on backup to restore lost data.
