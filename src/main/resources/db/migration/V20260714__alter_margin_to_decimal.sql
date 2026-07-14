-- V20260714: margin 列从 DOUBLE 改为 DECIMAL，与 Entity/VO 的 BigDecimal 类型统一
-- DOUBLE 存在浮点精度问题，DECIMAL 保证精确存储
ALTER TABLE `goods_category`
    MODIFY COLUMN `margin` DECIMAL(16,2) DEFAULT NULL COMMENT '保证金';
