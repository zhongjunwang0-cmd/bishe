-- Backfill audio URLs for pre-seeded listening exercises
USE `english_learning`;

UPDATE `biz_listening`
SET `audio_url` = 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3'
WHERE `audio_url` IS NULL OR `audio_url` = '';
