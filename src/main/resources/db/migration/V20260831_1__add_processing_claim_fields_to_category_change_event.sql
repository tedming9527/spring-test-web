ALTER TABLE `category_change_event`
    ADD COLUMN `processing_token` VARCHAR(64) DEFAULT NULL
        COMMENT '本次处理领取令牌'
        AFTER `next_retry_at`,
    ADD COLUMN `processing_lease_until` DATETIME DEFAULT NULL
        COMMENT '处理租约截止时间'
        AFTER `processing_token`,
    ADD KEY `idx_status_processing_lease`
        (`status`, `processing_lease_until`),
    ADD KEY `idx_processing_token`
        (`processing_token`);
