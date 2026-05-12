ALTER TABLE auth_actions MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_roles MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_menus MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_resources MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_record_rules MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_users MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_password_resets MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_password_histories MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_app_clients MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_providers MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_user_devices MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_user_sessions MODIFY lifecycle_status VARCHAR(20);
ALTER TABLE auth_user_sessions MODIFY refresh_token_jti VARCHAR(100) NULL;
ALTER TABLE auth_user_api_tokens MODIFY lifecycle_status VARCHAR(20);

ALTER TABLE auth_user_api_tokens ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP NULL;
ALTER TABLE auth_user_api_tokens ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_auth_user_api_tokens_revoked_at ON auth_user_api_tokens (revoked_at);

ALTER TABLE auth_app_clients ADD COLUMN IF NOT EXISTS max_failed_login_attempts INTEGER NOT NULL DEFAULT 5;
ALTER TABLE auth_app_clients ADD COLUMN IF NOT EXISTS lockout_seconds INTEGER NOT NULL DEFAULT 900;
