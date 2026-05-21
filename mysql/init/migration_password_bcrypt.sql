-- 密码字段扩容以存储 BCrypt 哈希（60 字符，预留余量）
ALTER TABLE `sys_user` MODIFY COLUMN `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt）';

-- 已有明文密码的用户在下次登录时会自动迁移为 BCrypt
-- 如需立即迁移，可手动将 password 重置为 BCrypt 哈希后再执行 UPDATE
