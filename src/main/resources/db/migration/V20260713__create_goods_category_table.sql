-- V20260713: 创建 goods_category 分类表
-- 对应实体: Category.java (org.example.springtestweb.category.entity)
--
-- 字段对照备注:
--   实体 id                → 列 id（主键，与 User 实体统一命名，不用 category_id）
--   实体 leaf              → 列 is_leaf（字段名与列名不一致，实体用 @TableField("is_leaf") 映射）
--   实体 p2f2RateKeeper    → 列 p2f2_rate_keeper（原 DDL 缺失，本次补充）
--   原 DDL 的 ordered、show_name 实体中不存在，已移除
--   topPic / isConfigLock / configId / showWeight 为 @TableField(exist = false)，不入表
--
CREATE TABLE IF NOT EXISTS `goods_category`
(
    `id`                       BIGINT        NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`                     VARCHAR(255)  NOT NULL DEFAULT ''     COMMENT '分类名称',
    `spell`                    VARCHAR(255)  DEFAULT NULL            COMMENT '拼音',
    `is_leaf`                  BIT(1)        NOT NULL                COMMENT '是否叶子节点',
    `parent_id`                BIGINT        DEFAULT NULL            COMMENT '父分类ID',
    `weight`                   INT           DEFAULT NULL            COMMENT '权重',
    `hidden`                   BIT(1)        DEFAULT b'1'            COMMENT '是否隐藏',
    `icon_url`                 VARCHAR(500)  DEFAULT NULL            COMMENT '图标URL',
    `convenient`               BIT(1)        DEFAULT b'0'            COMMENT '是否便民服务类型',
    `item_number`              INT           DEFAULT NULL            COMMENT '商品数量',
    `margin`                   DOUBLE(16,2)  DEFAULT NULL            COMMENT '保证金',
    `category_rate`            DECIMAL(5,4)  NOT NULL                COMMENT '居间服务费率',
    `donate_rate`              DECIMAL(5,4)  NOT NULL                COMMENT '募集比率',
    `tax_rate`                 DECIMAL(5,4)  NOT NULL                COMMENT '税率',
    `p1f1_rate_keeper`         DECIMAL(5,4)  DEFAULT NULL            COMMENT '默认管家激励分成比例',
    `p1f1_rate`                DECIMAL(5,4)  NOT NULL DEFAULT '0.4000' COMMENT '大区管家激励比率',
    `p2f2_rate`                DECIMAL(5,4)  NOT NULL DEFAULT '0.0000' COMMENT '总部管家激励比率',
    `p2f2_rate_keeper`         DECIMAL(5,4)  DEFAULT NULL            COMMENT '总部管家激励默认分成比例',
    `profit_headquarter_rate`  DECIMAL(5,4)  NOT NULL DEFAULT '0.5000' COMMENT '总部分成比率(总部+大区=1)',
    `new_user_incentive_rate`  DECIMAL(10,2) DEFAULT '0.00'          COMMENT '大区拉新激励比例',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
