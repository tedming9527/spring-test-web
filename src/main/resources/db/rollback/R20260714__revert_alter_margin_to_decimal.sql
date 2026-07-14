-- Rollback: revert V20260714__alter_margin_to_decimal.sql
-- 将 margin 列从 DECIMAL 恢复为 DOUBLE
ALTER TABLE `goods_category`
    MODIFY COLUMN `margin` DOUBLE(16,2) DEFAULT NULL COMMENT '保证金';
