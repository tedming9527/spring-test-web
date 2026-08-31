ALTER TABLE `goods_category`
    ADD COLUMN `category_version` BIGINT NOT NULL DEFAULT 0
        COMMENT '分类变更版本号';
