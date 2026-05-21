-- Vocabulary learning & review migration for existing databases
USE `english_learning`;

CREATE TABLE IF NOT EXISTS `biz_user_vocab` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `vocab_id` BIGINT NOT NULL COMMENT '词汇ID',
    `mastery_level` INT DEFAULT 0 COMMENT '掌握度 0-100',
    `review_stage` INT DEFAULT 0 COMMENT '艾宾浩斯复习阶段 0-6',
    `status` VARCHAR(20) DEFAULT 'NEW' COMMENT 'NEW/LEARNING/REVIEWING/MASTERED',
    `next_review_time` DATETIME DEFAULT NULL COMMENT '下次复习时间',
    `last_review_time` DATETIME DEFAULT NULL COMMENT '上次复习时间',
    `review_count` INT DEFAULT 0 COMMENT '累计复习次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_vocab` (`user_id`, `vocab_id`)
) ENGINE=InnoDB COMMENT='用户生词本';
