-- Question bank migration for existing databases
USE `english_learning`;

CREATE TABLE IF NOT EXISTS `biz_question_bank` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `module_type` VARCHAR(20) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT,
    `difficulty` VARCHAR(20),
    `category` VARCHAR(50),
    `duration` VARCHAR(20),
    `audio_url` VARCHAR(255),
    `questions_json` JSON NOT NULL,
    `answers_json` JSON NOT NULL,
    `status` VARCHAR(20) DEFAULT 'Active',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

ALTER TABLE `biz_reading`
    ADD COLUMN `bank_id` BIGINT DEFAULT NULL AFTER `id`,
    ADD COLUMN `questions_json` JSON NULL AFTER `difficulty`,
    ADD COLUMN `answers_json` JSON NULL AFTER `questions_json`;

ALTER TABLE `biz_listening`
    ADD COLUMN `bank_id` BIGINT DEFAULT NULL AFTER `id`,
    ADD COLUMN `content` TEXT NULL AFTER `audio_url`,
    ADD COLUMN `questions_json` JSON NULL AFTER `content`,
    ADD COLUMN `answers_json` JSON NULL AFTER `questions_json`;

ALTER TABLE `biz_cloze`
    ADD COLUMN `bank_id` BIGINT DEFAULT NULL AFTER `id`,
    ADD COLUMN `questions_json` JSON NULL AFTER `blanks_count`,
    ADD COLUMN `answers_json` JSON NULL AFTER `questions_json`;
