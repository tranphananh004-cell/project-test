-- ============================================================
-- V4__add_is_system_to_roles.sql
-- Thêm column is_system vào bảng roles
-- ============================================================

ALTER TABLE roles
    ADD COLUMN is_system TINYINT(1) NOT NULL DEFAULT 0
        COMMENT '1: Role hệ thống, không được xoá/đổi tên';

-- Đánh dấu ADMIN là role hệ thống
UPDATE roles SET is_system = 1 WHERE name = 'ADMIN';

