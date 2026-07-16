-- R20260715: 回退 goods_category 表审计字段
ALTER TABLE `goods_category`
    DROP COLUMN `update_time`,
    DROP COLUMN `create_time`,
    DROP COLUMN `updater`,
    DROP COLUMN `creator`;
