-- System configuration for AI / voice API integration
USE `english_learning`;

CREATE TABLE IF NOT EXISTS `sys_config` (
    `config_key` VARCHAR(100) NOT NULL PRIMARY KEY COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统配置表';

INSERT INTO `sys_config` (`config_key`, `config_value`) VALUES
('aiEnabled', 'false'),
('aiEndpoint', 'https://api.deepseek.com/v1/chat/completions'),
('aiModel', 'deepseek-chat'),
('aiApiKey', ''),
('voiceEnabled', 'false'),
('voiceProvider', 'azure'),
('voiceToken', '')
ON DUPLICATE KEY UPDATE `config_key` = `config_key`;
