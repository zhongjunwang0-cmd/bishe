-- Remove random suffixes from generated oral topics and deduplicate by title
-- Compatible with MySQL 5.7+ (no REGEXP_REPLACE required)
USE `english_learning`;

UPDATE `biz_oral`
SET `topic` = LEFT(`topic`, LENGTH(`topic`) - 6)
WHERE `topic` REGEXP ' [0-9a-fA-F]{5}$';

DELETE o1 FROM `biz_oral` o1
INNER JOIN `biz_oral` o2 ON o1.`topic` = o2.`topic` AND o1.`id` > o2.`id`;

-- Remove placeholder titles left from old generation logic
DELETE FROM `biz_oral`
WHERE `topic` = 'AI Daily Conversation topic';
