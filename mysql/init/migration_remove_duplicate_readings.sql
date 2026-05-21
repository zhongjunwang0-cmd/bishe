-- Remove duplicate generated reading tests (English titles from repeated generation)
USE `english_learning`;

DELETE FROM `biz_reading`
WHERE `id` IN (19, 20, 21, 22, 23, 24, 25);
