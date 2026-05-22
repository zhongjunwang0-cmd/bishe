-- 个性化周计划表（已有数据库增量迁移）
USE `english_learning`;

CREATE TABLE IF NOT EXISTS `biz_learning_plan_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `week_start` DATE NOT NULL COMMENT '周起始日（周一）',
    `plan_date` DATE NOT NULL COMMENT '任务日期',
    `day_of_week` TINYINT NOT NULL COMMENT '1=周一 … 7=周日',
    `module` VARCHAR(20) NOT NULL COMMENT 'VOCAB/GRAMMAR/READING/LISTENING/ORAL',
    `source` VARCHAR(20) DEFAULT 'template' COMMENT 'template=档案模板 ai_recommend=AI推荐',
    `task_text` VARCHAR(255) NOT NULL COMMENT '任务描述',
    `target_minutes` INT DEFAULT 0 COMMENT '预计用时（分钟）',
    `completed` TINYINT DEFAULT 0 COMMENT '是否完成 0/1',
    `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_user_week` (`user_id`, `week_start`),
    KEY `idx_user_date` (`user_id`, `plan_date`)
) ENGINE=InnoDB COMMENT='个性化周计划任务项';
