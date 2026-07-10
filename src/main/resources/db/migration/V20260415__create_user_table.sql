-- Migration: create_user_table
-- Creates the base user table (schema reflecting current state, baselined — will not execute)
CREATE TABLE IF NOT EXISTS `user`
(
    id            BIGINT AUTO_INCREMENT NOT NULL COMMENT '主键ID',
    name          VARCHAR(255) NULL DEFAULT NULL COMMENT '名称',
    email         VARCHAR(255) NULL DEFAULT NULL COMMENT '邮箱',
    birth_day     DATE NULL DEFAULT NULL COMMENT '生日',
    username      VARCHAR(255) NULL DEFAULT NULL COMMENT '用户名',
    password_hash VARCHAR(255) NULL DEFAULT NULL COMMENT 'BCrypt密码哈希',
    roles         VARCHAR(255) NULL DEFAULT NULL COMMENT '角色',
    last_login    DATETIME NULL DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (id)
);

-- 种子数据：admin 账号
INSERT IGNORE INTO `user` (id, name, email, birth_day, username, password_hash, roles, last_login) VALUES
(107, 'admin', 'admin@system.local', NULL, 'admin', '$2a$10$bx2ynl2h/7QsZbcwQTwg5.beNY7a6y2Es15t8D6yTRnaoz/3eBI/e', 'ROLE_ADMIN', NULL);
