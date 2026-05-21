-- Learning record stats migration for existing databases
USE `english_learning`;

ALTER TABLE `biz_learning_record`
    ADD COLUMN `score` INT DEFAULT NULL COMMENT '成绩得分' AFTER `duration`;
