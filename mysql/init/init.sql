/* 数据库初始化脚本 */
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS `english_learning` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `english_learning`;

-- 1. 权限管理相关表
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt）',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role_id` BIGINT COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='用户表';

CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    `description` VARCHAR(200) COMMENT '描述'
) ENGINE=InnoDB COMMENT='角色表';

CREATE TABLE `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(50) NOT NULL COMMENT '权限编码',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID'
) ENGINE=InnoDB COMMENT='权限表';

CREATE TABLE `sys_role_permission` (
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB COMMENT='角色权限关联表';

-- 2. 核心业务表
-- 词汇
CREATE TABLE `biz_vocab` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `word` VARCHAR(100) NOT NULL COMMENT '单词',
    `phonetic` VARCHAR(100) COMMENT '音标',
    `translation` TEXT NOT NULL COMMENT '翻译',
    `example` TEXT COMMENT '例句',
    `level` VARCHAR(20) COMMENT '难度等级',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='词汇表';

-- 用户生词本（艾宾浩斯复习）
CREATE TABLE `biz_user_vocab` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `vocab_id` BIGINT NOT NULL COMMENT '词汇ID',
    `mastery_level` INT DEFAULT 0 COMMENT '掌握度 0-100',
    `review_stage` INT DEFAULT 0 COMMENT '艾宾浩斯复习阶段 0-6',
    `status` VARCHAR(20) DEFAULT 'NEW' COMMENT 'NEW/LEARNING/REVIEWING/MASTERED',
    `next_review_time` DATETIME DEFAULT NULL COMMENT '下次复习时间',
    `last_review_time` DATETIME DEFAULT NULL COMMENT '上次复习时间',
    `review_count` INT DEFAULT 0 COMMENT '累计复习次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_vocab` (`user_id`, `vocab_id`)
) ENGINE=InnoDB COMMENT='用户生词本';

-- 语法
CREATE TABLE `biz_grammar` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `category` VARCHAR(50) COMMENT '分类',
    `content` TEXT NOT NULL COMMENT '内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='语法表';

-- 文献阅读
CREATE TABLE `biz_literature` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `translation` TEXT COMMENT '译文',
    `author` VARCHAR(100) COMMENT '作者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='文献表';

-- 讨论区
CREATE TABLE `biz_discuss` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型 (VOCAB, GRAMMAR, LIT)',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='讨论表';

-- 学习记录
CREATE TABLE `biz_learning_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '学习类型',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `duration` INT COMMENT '学习时长(秒)',
    `score` INT DEFAULT NULL COMMENT '成绩得分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='学习记录表';

-- 题库模板（教师录入，/generate 随机抽题来源）
CREATE TABLE `biz_question_bank` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `module_type` VARCHAR(20) NOT NULL COMMENT 'READING/LISTENING/CLOZE',
    `title` VARCHAR(255) NOT NULL COMMENT '题库标题',
    `content` TEXT COMMENT '阅读/填空 passage 或听力 transcript',
    `difficulty` VARCHAR(20) COMMENT '阅读难度',
    `category` VARCHAR(50) COMMENT '听力分类',
    `duration` VARCHAR(20) COMMENT '听力时长',
    `audio_url` VARCHAR(255) COMMENT '听力音频地址',
    `questions_json` JSON NOT NULL COMMENT '题目与选项 JSON 数组',
    `answers_json` JSON NOT NULL COMMENT '标准答案与解析 JSON 数组',
    `status` VARCHAR(20) DEFAULT 'Active' COMMENT 'Active/Inactive',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='题库模板表';

-- 阅读理解
CREATE TABLE `biz_reading` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `bank_id` BIGINT DEFAULT NULL COMMENT '来源题库ID',
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT,
    `difficulty` VARCHAR(20),
    `questions_json` JSON COMMENT '题目与选项',
    `answers_json` JSON COMMENT '标准答案与解析',
    `score` INT DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='阅读理解表';

-- 听力训练
CREATE TABLE `biz_listening` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `bank_id` BIGINT DEFAULT NULL COMMENT '来源题库ID',
    `title` VARCHAR(255) NOT NULL,
    `category` VARCHAR(50),
    `duration` VARCHAR(20),
    `audio_url` VARCHAR(255),
    `content` TEXT COMMENT '听力原文/说明',
    `questions_json` JSON COMMENT '题目与选项',
    `answers_json` JSON COMMENT '标准答案与解析',
    `score` INT DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='听力训练表';

-- 选词填空
CREATE TABLE `biz_cloze` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `bank_id` BIGINT DEFAULT NULL COMMENT '来源题库ID',
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT,
    `blanks_count` INT,
    `questions_json` JSON COMMENT '各空选项',
    `answers_json` JSON COMMENT '标准答案与解析',
    `completion_status` VARCHAR(50),
    `score` INT DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='选词填空表';

-- 口语练习
CREATE TABLE `biz_oral` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `topic` VARCHAR(255) NOT NULL,
    `reference_text` VARCHAR(512) DEFAULT NULL COMMENT 'English reading prompt',
    `score` INT DEFAULT NULL,
    `attempts` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='口语练习表';

-- 系统配置（AI / 语音 API 集成）
CREATE TABLE `sys_config` (
    `config_key` VARCHAR(100) NOT NULL PRIMARY KEY COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统配置表';

-- 3. 基础数据
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES 
('管理员', 'ADMIN', '系统最高权限'),
('教师', 'TEACHER', '内容维护权限'),
('普通用户', 'USER', '基础学习权限');

-- 初始账号 admin/teacher/user 默认密码均为 123456（BCrypt）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role_id`) VALUES 
('admin', '$2a$10$lNX8Qs8nlvMBUvGf5mkdQu3am0.UrZOS5HBAkHzkkZpdtLeohqylu', '超级管理员', 1),
('teacher', '$2a$10$lNX8Qs8nlvMBUvGf5mkdQu3am0.UrZOS5HBAkHzkkZpdtLeohqylu', '王老师', 2),
('user', '$2a$10$lNX8Qs8nlvMBUvGf5mkdQu3am0.UrZOS5HBAkHzkkZpdtLeohqylu', '小明', 3);

-- 演示词汇数据
INSERT INTO `biz_vocab` (`word`, `phonetic`, `translation`, `example`, `level`) VALUES 
('procrastinate', '/prəʊˈkræstɪneɪt/', '拖延', 'He tends to procrastinate on his homework.', 'CET-6'),
('resilient', '/rɪˈzɪliənt/', '有韧性的', 'She is a resilient woman who can handle any challenge.', 'IELTS');
