-- V20260812：创建商品表并初始化练习数据
--
-- 金额统一按人民币最小单位“分”存储：
-- 1990 = 19.90 元
-- 550  = 5.50 元
--
-- 优点：
-- 1. 避免浮点数精度问题
-- 2. 与支付接口的金额单位保持一致
-- 3. 方便后续进行订单、支付和退款练习

CREATE TABLE IF NOT EXISTS `goods`
(
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name`        VARCHAR(255)  NOT NULL COMMENT '商品名称',
    `category_id` BIGINT        NOT NULL COMMENT '商品分类ID',
    `price_cent`  BIGINT        NOT NULL COMMENT '销售价格，单位：分',
    `stock`       INT           NOT NULL DEFAULT 0 COMMENT '库存数量',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1上架，0下架',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT '商品描述',
    `creator`     VARCHAR(64)   DEFAULT NULL COMMENT '创建人',
    `updater`     VARCHAR(64)   DEFAULT NULL COMMENT '更新人',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`),
    INDEX `idx_goods_category_id` (`category_id`),
    INDEX `idx_goods_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT = '商品表';


-- 初始化商品数据
--
-- 使用固定ID，方便接口和缓存测试。
-- INSERT IGNORE 使迁移脚本重复执行时不会重复插入。

INSERT IGNORE INTO `goods`
(
    `id`,
    `name`,
    `category_id`,
    `price_cent`,
    `stock`,
    `status`,
    `description`,
    `creator`,
    `updater`,
    `create_time`,
    `update_time`
)
VALUES
    (
        1001,
        '红富士苹果',
        110101,
        1990,
        100,
        1,
        '新鲜红富士苹果，约2.5kg',
        'system',
        'system',
        NOW(),
        NOW()
    ),
    (
        1002,
        '茉莉花茶饮料',
        120202,
        550,
        200,
        1,
        '低糖茉莉花茶饮料',
        'system',
        'system',
        NOW(),
        NOW()
    ),
    (
        1003,
        '原木抽纸',
        130102,
        2990,
        80,
        1,
        '三层原木抽纸，24包',
        'system',
        'system',
        NOW(),
        NOW()
    );