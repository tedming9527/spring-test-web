-- V20260715: 为 goods_category 表增加审计字段
-- 对应实体: BaseEntity.java (org.example.springtestweb.entity)
--
-- creator / createTime   → 插入时自动填充
-- updater / updateTime   → 插入和更新时自动填充
ALTER TABLE `goods_category`
    ADD COLUMN `creator`     VARCHAR(64)  DEFAULT NULL COMMENT '创建人' AFTER `new_user_incentive_rate`,
    ADD COLUMN `updater`     VARCHAR(64)  DEFAULT NULL COMMENT '更新人' AFTER `creator`,
    ADD COLUMN `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间' AFTER `updater`,
    ADD COLUMN `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间' AFTER `create_time`;
