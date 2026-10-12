-- ============================================================
-- V2__add_refresh_tokens.sql
-- Bảng lưu refresh token để có thể revoke (logout, đổi token)
-- ============================================================

CREATE TABLE refresh_tokens (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    token      VARCHAR(500) NOT NULL UNIQUE              COMMENT 'JWT refresh token string',
    user_id    BIGINT       NOT NULL,
    expires_at DATETIME     NOT NULL,
    revoked    TINYINT(1)   NOT NULL DEFAULT 0           COMMENT '0=valid, 1=revoked',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
