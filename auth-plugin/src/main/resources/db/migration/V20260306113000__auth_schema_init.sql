CREATE TABLE IF NOT EXISTS actions (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uq_actions_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uq_roles_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS menus (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    title VARCHAR(150),
    parent_id BIGINT,
    path VARCHAR(255),
    icon VARCHAR(50),
    sort_order INTEGER NOT NULL DEFAULT 0,
    type VARCHAR(20) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    module VARCHAR(50),
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    help_url VARCHAR(255),
    CONSTRAINT uq_menus_code UNIQUE (code),
    CONSTRAINT fk_menus_parent_id FOREIGN KEY (parent_id) REFERENCES menus (id)
);

CREATE TABLE IF NOT EXISTS resources (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    is_approval_required BOOLEAN NOT NULL DEFAULT FALSE,
    menu_id BIGINT NOT NULL,
    CONSTRAINT uq_resources_code UNIQUE (code),
    CONSTRAINT fk_resources_menu_id FOREIGN KEY (menu_id) REFERENCES menus (id)
);

CREATE TABLE IF NOT EXISTS record_rules (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    resource_id BIGINT NOT NULL,
    condition_expression TEXT NOT NULL,
    uri VARCHAR(255) NOT NULL,
    CONSTRAINT uq_record_rules_code UNIQUE (code),
    CONSTRAINT fk_record_rules_resource_id FOREIGN KEY (resource_id) REFERENCES resources (id)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255),
    full_name VARCHAR(150),
    email VARCHAR(150),
    phone VARCHAR(20),
    avatar_path VARCHAR(255),
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP NULL,
    failed_login_count INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    authorized_until TIMESTAMP NULL,
    password_changed_at TIMESTAMP NULL,
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS password_resets (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    user_id BIGINT NOT NULL,
    reset_token_hash VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    CONSTRAINT fk_password_resets_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS password_histories (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255),
    CONSTRAINT fk_password_histories_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS app_clients (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    client_id VARCHAR(50) NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    grant_types VARCHAR(255) NOT NULL,
    redirect_uris VARCHAR(255),
    access_token_validity INTEGER NOT NULL,
    refresh_token_validity INTEGER NOT NULL,
    web_idle_timeout INTEGER,
    is_single_session BOOLEAN NOT NULL DEFAULT TRUE,
    is_single_device BOOLEAN NOT NULL DEFAULT FALSE,
    scopes VARCHAR(255) NOT NULL,
    CONSTRAINT uq_app_clients_client_id UNIQUE (client_id)
);

CREATE TABLE IF NOT EXISTS user_devices (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    user_id BIGINT NOT NULL,
    app_client_id BIGINT NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    device_model VARCHAR(150),
    os_version VARCHAR(50),
    app_version VARCHAR(50),
    trusted_expires_at TIMESTAMP NULL,
    CONSTRAINT uq_user_devices_device_id UNIQUE (device_id),
    CONSTRAINT fk_user_devices_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_devices_app_client_id FOREIGN KEY (app_client_id) REFERENCES app_clients (id)
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    user_id BIGINT NOT NULL,
    app_client_id BIGINT NOT NULL,
    user_device_id BIGINT NOT NULL,
    access_token_jti VARCHAR(100) NOT NULL,
    refresh_token_jti VARCHAR(100) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    last_activity_at TIMESTAMP NULL,
    ip_address VARCHAR(50),
    user_agent VARCHAR(512),
    CONSTRAINT uq_user_sessions_user_client UNIQUE (user_id, app_client_id),
    CONSTRAINT uq_user_sessions_access_jti UNIQUE (access_token_jti),
    CONSTRAINT uq_user_sessions_refresh_jti UNIQUE (refresh_token_jti),
    CONSTRAINT fk_user_sessions_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_sessions_app_client_id FOREIGN KEY (app_client_id) REFERENCES app_clients (id),
    CONSTRAINT fk_user_sessions_user_device_id FOREIGN KEY (user_device_id) REFERENCES user_devices (id)
);

CREATE TABLE IF NOT EXISTS user_api_tokens (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT,
    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NULL,
    CONSTRAINT uq_user_api_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_user_api_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS permissions (
    role_id BIGINT NOT NULL,
    action_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, action_id, resource_id),
    CONSTRAINT fk_permissions_role_id FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_permissions_action_id FOREIGN KEY (action_id) REFERENCES actions (id),
    CONSTRAINT fk_permissions_resource_id FOREIGN KEY (resource_id) REFERENCES resources (id)
);

CREATE TABLE IF NOT EXISTS role_record_rules (
    role_id BIGINT NOT NULL,
    record_rule_id BIGINT NOT NULL,
    is_negated BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (role_id, record_rule_id),
    CONSTRAINT fk_role_record_rules_role_id FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_record_rules_record_rule_id FOREIGN KEY (record_rule_id) REFERENCES record_rules (id)
);

CREATE TABLE IF NOT EXISTS role_menus (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_role_menus_role_id FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_menus_menu_id FOREIGN KEY (menu_id) REFERENCES menus (id)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE INDEX IF NOT EXISTS idx_menus_parent_id ON menus (parent_id);
CREATE INDEX IF NOT EXISTS idx_resources_menu_id ON resources (menu_id);
CREATE INDEX IF NOT EXISTS idx_record_rules_resource_id ON record_rules (resource_id);
CREATE INDEX IF NOT EXISTS idx_password_resets_user_id ON password_resets (user_id);
CREATE INDEX IF NOT EXISTS idx_password_resets_expires_at ON password_resets (expires_at);
CREATE INDEX IF NOT EXISTS idx_password_histories_user_id ON password_histories (user_id);
CREATE INDEX IF NOT EXISTS idx_user_devices_user_id ON user_devices (user_id);
CREATE INDEX IF NOT EXISTS idx_user_devices_app_client_id ON user_devices (app_client_id);
CREATE INDEX IF NOT EXISTS idx_user_devices_trusted_expires_at ON user_devices (trusted_expires_at);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions (expires_at);
CREATE INDEX IF NOT EXISTS idx_user_sessions_last_activity_at ON user_sessions (last_activity_at);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_device_id ON user_sessions (user_device_id);
CREATE INDEX IF NOT EXISTS idx_user_api_tokens_user_id ON user_api_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_user_api_tokens_expires_at ON user_api_tokens (expires_at);
CREATE INDEX IF NOT EXISTS idx_permissions_role_id ON permissions (role_id);
CREATE INDEX IF NOT EXISTS idx_permissions_action_id ON permissions (action_id);
CREATE INDEX IF NOT EXISTS idx_permissions_resource_id ON permissions (resource_id);
CREATE INDEX IF NOT EXISTS idx_role_record_rules_role_id ON role_record_rules (role_id);
CREATE INDEX IF NOT EXISTS idx_role_record_rules_record_rule_id ON role_record_rules (record_rule_id);
CREATE INDEX IF NOT EXISTS idx_role_menus_role_id ON role_menus (role_id);
CREATE INDEX IF NOT EXISTS idx_role_menus_menu_id ON role_menus (menu_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles (role_id);
