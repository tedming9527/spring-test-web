-- ============================================================
-- 回滚脚本：撤销 V2__add_username_bcrypt_passwords.sql
-- 警告：执行后所有密码恢复为明文 '123456'，username 列被删除
-- ============================================================

-- 1. 删除 admin 超管账号
DELETE FROM user WHERE username = 'admin' AND email = 'admin@system.local';

-- 2. 将密码恢复为明文 123456（回滚到升级前状态）
UPDATE user SET password_hash = '123456';

-- 3. 移除 username 唯一约束（先删索引，再删列）
ALTER TABLE user DROP INDEX uk_username;

-- 4. 删除 username 列
ALTER TABLE user DROP COLUMN username;

-- 验证
SELECT id, name, email, password_hash, roles FROM user LIMIT 5;
