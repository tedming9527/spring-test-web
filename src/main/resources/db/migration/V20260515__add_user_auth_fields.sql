-- Migration: add_user_auth_fields
-- Adds password_hash, roles, last_login to user table (nullable)
ALTER TABLE `user`
ADD COLUMN `password_hash` VARCHAR(255) NULL,
	ADD COLUMN `roles` VARCHAR(255) NULL,
	ADD COLUMN `last_login` DATETIME NULL;