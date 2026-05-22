-- 用户个性化学习档案字段（已有数据库增量迁移）
USE `english_learning`;

ALTER TABLE `sys_user`
    ADD COLUMN `level` VARCHAR(20) DEFAULT 'B1' COMMENT '英语水平' AFTER `role_id`,
    ADD COLUMN `target_exam` VARCHAR(20) DEFAULT 'GENERAL' COMMENT '目标考试' AFTER `level`,
    ADD COLUMN `daily_goal` INT DEFAULT 30 COMMENT '每日学习目标（分钟）' AFTER `target_exam`;
