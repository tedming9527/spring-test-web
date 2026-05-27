-- ============================================================
-- 升级脚本：添加 username 列 + BCrypt 加密所有密码
-- 执行时间：2026-05-15
-- 对应回滚：V2__rollback_add_username_bcrypt.sql
-- ============================================================
-- 1. 添加 username 列（允许 NULL，稍后填充后再加约束）
ALTER TABLE user
ADD COLUMN username VARCHAR(50) NULL;
-- 2. 为现有用户自动派生 username（取 email @ 前缀，替换非法字符为下划线）
UPDATE user
SET username = LOWER(
		REGEXP_REPLACE(
			SUBSTRING_INDEX(email, '@', 1),
			'[^a-zA-Z0-9_]',
			'_'
		)
	)
WHERE username IS NULL;
-- 3. 处理 username 冲突（同名时后缀 _id）
UPDATE user u
	INNER JOIN (
		SELECT id,
			username,
			ROW_NUMBER() OVER (
				PARTITION BY username
				ORDER BY id
			) AS rn
		FROM user
	) dup ON u.id = dup.id
	AND dup.rn > 1
SET u.username = CONCAT(u.username, '_', u.id);
-- 4. 将所有密码升级为 BCrypt(cost=10) 哈希（原始明文：123456）
-- hash = $2a$10$Ho15GPUjj9ZW8tkPq1etYeCy4nXuJJXCbcvzEzz0p3uedR9ySBg3C
UPDATE user
SET password_hash = '$2a$10$Ho15GPUjj9ZW8tkPq1etYeCy4nXuJJXCbcvzEzz0p3uedR9ySBg3C';
-- 5. 插入 admin 超管账号（不存在则插入）
INSERT INTO user (name, email, username, password_hash, roles)
VALUES (
		'admin',
		'admin@system.local',
		'admin',
		'$2a$10$Ho15GPUjj9ZW8tkPq1etYeCy4nXuJJXCbcvzEzz0p3uedR9ySBg3C',
		'ROLE_ADMIN'
	) ON DUPLICATE KEY
UPDATE password_hash = '$2a$10$Ho15GPUjj9ZW8tkPq1etYeCy4nXuJJXCbcvzEzz0p3uedR9ySBg3C',
	roles = 'ROLE_ADMIN';
-- 6. 为现有用户设置默认角色（跳过 admin）
UPDATE user
SET roles = 'ROLE_USER'
WHERE roles IS NULL;
-- 7. 添加 NOT NULL + UNIQUE 约束
ALTER TABLE user
MODIFY COLUMN username VARCHAR(50) NOT NULL;
ALTER TABLE user
ADD CONSTRAINT uk_username UNIQUE (username);
-- 验证
SELECT id,
	name,
	username,
	LEFT(password_hash, 20) AS hash_prefix,
	roles
FROM user
LIMIT 5;