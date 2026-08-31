ALTER TABLE `category_change_event`
    DROP INDEX `idx_processing_token`,
    DROP INDEX `idx_status_processing_lease`,
    DROP COLUMN `processing_lease_until`,
    DROP COLUMN `processing_token`;
