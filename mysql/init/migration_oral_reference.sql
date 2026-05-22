-- Add English reference sentence for pronunciation scoring (Whisper+WER)
ALTER TABLE `biz_oral`
    ADD COLUMN `reference_text` VARCHAR(512) DEFAULT NULL COMMENT 'English reading prompt' AFTER `topic`;
