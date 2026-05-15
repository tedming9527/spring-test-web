-- Rollback: revert_add_user_auth_fields
-- Removes columns added by V20260515__add_user_auth_fields.sql
ALTER TABLE `user` DROP COLUMN IF EXISTS `password_hash`,
	DROP COLUMN IF EXISTS `roles`,
	DROP COLUMN IF EXISTS `last_login`;