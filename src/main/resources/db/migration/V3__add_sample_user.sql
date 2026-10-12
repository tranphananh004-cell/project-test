-- ============================================================
-- V3: Thêm tài khoản mẫu: user / User@123
-- ============================================================
INSERT INTO users (username, email, password, full_name, enabled)
SELECT 'user', 'user@example.com', '$2a$12$uslgiXfM4auNFccceu70.OXYAqQqbsOoXzp0W0NYlf//.ilS1XMWm', 'Standard User', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user');

-- Gán role USER cho tài khoản user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'user' AND r.name = 'USER';
