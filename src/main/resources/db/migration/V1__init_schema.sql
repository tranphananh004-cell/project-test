-- ============================================================
-- V1__init_schema.sql
-- Tạo schema ban đầu cho module User/Role/Permission
-- ============================================================

-- 1. Bảng permissions
CREATE TABLE permissions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE   COMMENT 'Ví dụ: USER_READ, ROLE_CREATE',
    description VARCHAR(255)                   COMMENT 'Mô tả quyền',
    resource    VARCHAR(100) NOT NULL           COMMENT 'Tài nguyên: user, role, permission',
    action      VARCHAR(50)  NOT NULL           COMMENT 'Hành động: READ, CREATE, UPDATE, DELETE',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Bảng roles
CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE   COMMENT 'Ví dụ: ADMIN, MANAGER, USER',
    description VARCHAR(255)                   COMMENT 'Mô tả role',
    is_system   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '1: Role hệ thống, không được xoá/đổi tên',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Bảng users
CREATE TABLE users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(100) NOT NULL UNIQUE,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL          COMMENT 'Bcrypt hashed password',
    full_name    VARCHAR(255),
    enabled      TINYINT(1)   NOT NULL DEFAULT 1,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. Bảng nối user <-> role (many-to-many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 5. Bảng nối role <-> permission (many-to-many)
CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- ============================================================
-- Seed data: Permissions
-- ============================================================
INSERT INTO permissions (name, description, resource, action) VALUES
-- User permissions
('USER_READ',        'Xem danh sách và chi tiết user',  'user', 'READ'),
('USER_CREATE',      'Tạo mới user',                    'user', 'CREATE'),
('USER_UPDATE',      'Cập nhật thông tin user',         'user', 'UPDATE'),
('USER_DELETE',      'Xoá user',                        'user', 'DELETE'),
('USER_ASSIGN_ROLE', 'Gán role cho user',               'user', 'ASSIGN_ROLE'),

-- Role permissions
('ROLE_READ',              'Xem danh sách và chi tiết role', 'role', 'READ'),
('ROLE_CREATE',            'Tạo mới role',                   'role', 'CREATE'),
('ROLE_UPDATE',            'Cập nhật role',                  'role', 'UPDATE'),
('ROLE_DELETE',            'Xoá role',                       'role', 'DELETE'),
('ROLE_ASSIGN_PERMISSION', 'Gán permission cho role',        'role', 'ASSIGN_PERMISSION'),

-- Permission permissions
('PERMISSION_READ',   'Xem danh sách permissions', 'permission', 'READ'),
('PERMISSION_CREATE', 'Tạo mới permission',         'permission', 'CREATE'),
('PERMISSION_UPDATE', 'Cập nhật permission',        'permission', 'UPDATE'),
('PERMISSION_DELETE', 'Xoá permission',             'permission', 'DELETE');

-- ============================================================
-- Seed data: Roles
-- ============================================================
INSERT INTO roles (name, description, is_system) VALUES
('ADMIN',   'Quản trị viên — toàn quyền', 1),
('MANAGER', 'Quản lý — quyền đọc và cập nhật', 0),
('USER',    'Người dùng thường — quyền cơ bản', 0);

-- ============================================================
-- Seed data: Role <-> Permission
-- ADMIN có tất cả quyền
-- ============================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- MANAGER: chỉ read user/role, không xoá, không quản lý permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('USER_READ', 'USER_UPDATE', 'ROLE_READ', 'PERMISSION_READ')
WHERE r.name = 'MANAGER';

-- USER: chỉ read
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('USER_READ')
WHERE r.name = 'USER';

-- ============================================================
-- Seed data: Admin user
-- Password: Admin@123 (bcrypt encoded)
-- ============================================================
INSERT INTO users (username, email, password, full_name, enabled) VALUES
('admin', 'admin@example.com', '$2a$12$rXQWgIFbLB0mpiMbcYEJGOoQ7VOl/OAGEoHl.HkEFVxhSFoijmLmm', 'System Administrator', 1);

-- Gán role ADMIN cho user admin
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';
