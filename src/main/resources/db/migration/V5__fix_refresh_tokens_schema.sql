-- ============================================================
-- V5__fix_refresh_tokens_schema.sql
-- Chuẩn hóa column expires_at trong refresh_tokens
-- (Trường hợp DB cũ đang dùng tên expiry_date)
-- ============================================================

-- Chỉ rename nếu column cũ tồn tại (MySQL 8+ hỗ trợ IF EXISTS với ALTER RENAME)
-- Nếu column 'expiry_date' tồn tại thì rename sang 'expires_at'
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'refresh_tokens'
      AND COLUMN_NAME  = 'expiry_date'
);

SET @sql = IF(@col_exists > 0,
    'ALTER TABLE refresh_tokens RENAME COLUMN expiry_date TO expires_at',
    'SELECT 1 -- column expires_at already correct'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
