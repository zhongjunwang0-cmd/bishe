-- Remove random suffixes (e.g. #d0629, #29208) from generated test titles
-- Compatible with MySQL 5.7+ (no REGEXP_REPLACE required)
USE `english_learning`;

UPDATE `biz_reading`
SET `title` = LEFT(`title`, LENGTH(`title`) - 7)
WHERE `title` REGEXP ' #[0-9a-fA-F]{5}$';

UPDATE `biz_listening`
SET `title` = LEFT(`title`, LENGTH(`title`) - 7)
WHERE `title` REGEXP ' #[0-9a-fA-F]{5}$';

UPDATE `biz_cloze`
SET `title` = LEFT(`title`, LENGTH(`title`) - 7)
WHERE `title` REGEXP ' #[0-9a-fA-F]{5}$';
