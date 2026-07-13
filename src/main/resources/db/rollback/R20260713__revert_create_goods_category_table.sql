-- Rollback: revert V20260713__create_goods_category_table.sql
-- 回滚操作: 删除 goods_category 分类表
-- 注意: 该表被 Category 实体引用，回滚前请确认无业务依赖
DROP TABLE IF EXISTS `goods_category`;
