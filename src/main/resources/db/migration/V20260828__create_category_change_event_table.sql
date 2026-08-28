CREATE TABLE IF NOT EXISTS `category_change_event`
(
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '同步任务ID',
    `category_id`      BIGINT        NOT NULL COMMENT '分类ID',
    `category_version` BIGINT        NOT NULL COMMENT '分类变更版本号',
    `event_type`       VARCHAR(64)   NOT NULL COMMENT '事件类型',
    `payload`          VARCHAR(1000) NOT NULL COMMENT '待同步内容',
    `status`           VARCHAR(32)   NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
    `retry_count`      INT           NOT NULL DEFAULT 0 COMMENT '任务重试次数',
    `last_error`       VARCHAR(500)  DEFAULT NULL COMMENT '最近失败原因',
    `next_retry_at`    DATETIME      DEFAULT NULL COMMENT '下次允许重试时间',
    `creator`          VARCHAR(64)   DEFAULT NULL COMMENT '创建人',
    `updater`          VARCHAR(64)   DEFAULT NULL COMMENT '更新人',
    `create_time`      DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_time`      DATETIME      DEFAULT NULL COMMENT '更新时间',

    PRIMARY KEY (`id`),
    KEY `idx_status_next_retry_at` (`status`, `next_retry_at`),
    KEY `idx_category_version` (`category_id`, `category_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类变更同步任务表';
