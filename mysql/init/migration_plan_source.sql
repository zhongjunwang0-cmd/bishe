-- 周计划任务来源字段（template / ai_recommend）
USE `english_learning`;

ALTER TABLE `biz_learning_plan_item`
    ADD COLUMN `source` VARCHAR(20) DEFAULT 'template' COMMENT 'template=档案模板 ai_recommend=AI推荐' AFTER `module`;
