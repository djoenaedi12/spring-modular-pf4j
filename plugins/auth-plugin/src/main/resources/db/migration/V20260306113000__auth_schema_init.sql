CREATE TABLE IF NOT EXISTS auth_actions (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS auth_roles (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS auth_menus (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

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

    CONSTRAINT fk_auth_menus_parent_id FOREIGN KEY (parent_id) REFERENCES auth_menus (id)
);

CREATE TABLE IF NOT EXISTS auth_resources (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    is_approval_required BOOLEAN NOT NULL DEFAULT FALSE,
    menu_id BIGINT NOT NULL,
    CONSTRAINT fk_auth_resources_menu_id FOREIGN KEY (menu_id) REFERENCES auth_menus (id)
);

CREATE TABLE IF NOT EXISTS auth_record_rules (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    resource_id BIGINT NOT NULL,
    condition_expression TEXT NOT NULL,
    uri VARCHAR(255) NOT NULL,

    CONSTRAINT fk_auth_record_rules_resource_id FOREIGN KEY (resource_id) REFERENCES auth_resources (id)
);

CREATE TABLE IF NOT EXISTS auth_users (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,

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
    password_changed_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS auth_password_resets (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,

    user_id BIGINT NOT NULL,
    reset_token_hash VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,

    CONSTRAINT fk_auth_password_resets_user_id FOREIGN KEY (user_id) REFERENCES auth_users (id)
);

CREATE TABLE IF NOT EXISTS auth_password_histories (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,

    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255),

    CONSTRAINT fk_auth_password_histories_user_id FOREIGN KEY (user_id) REFERENCES auth_users (id)
);

CREATE TABLE IF NOT EXISTS auth_app_clients (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,

    client_id VARCHAR(50) NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    grant_types VARCHAR(255) NOT NULL,
    redirect_uris VARCHAR(255),
    access_token_validity INTEGER NOT NULL,
    refresh_token_validity INTEGER NOT NULL,
    web_idle_timeout INTEGER,
    is_single_session BOOLEAN NOT NULL DEFAULT TRUE,
    is_single_device BOOLEAN NOT NULL DEFAULT FALSE,
    scopes VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS auth_providers (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status VARCHAR(20),

    provider_id VARCHAR(100) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    settings_json TEXT NOT NULL,

    CONSTRAINT uq_auth_providers_provider_id UNIQUE (provider_id)
);

CREATE TABLE IF NOT EXISTS auth_user_devices (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,

    user_id BIGINT NOT NULL,
    app_client_id BIGINT,
    device_id VARCHAR(100) NOT NULL,
    device_model VARCHAR(150),
    trusted_expires_at TIMESTAMP NULL,

    CONSTRAINT fk_auth_user_devices_user_id FOREIGN KEY (user_id) REFERENCES auth_users (id),
    CONSTRAINT fk_auth_user_devices_app_client_id FOREIGN KEY (app_client_id) REFERENCES auth_app_clients (id)
);

CREATE TABLE IF NOT EXISTS auth_user_sessions (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,

    user_id BIGINT NOT NULL,
    app_client_id BIGINT,
    user_device_id BIGINT,
    access_token_jti VARCHAR(100) NOT NULL,
    refresh_token_jti VARCHAR(100),
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    last_activity_at TIMESTAMP NULL,
    ip_address VARCHAR(50),
    user_agent VARCHAR(512),

    CONSTRAINT fk_auth_user_sessions_user_id FOREIGN KEY (user_id) REFERENCES auth_users (id),
    CONSTRAINT fk_auth_user_sessions_app_client_id FOREIGN KEY (app_client_id) REFERENCES auth_app_clients (id),
    CONSTRAINT fk_auth_user_sessions_user_device_id FOREIGN KEY (user_device_id) REFERENCES auth_user_devices (id)
);

CREATE TABLE IF NOT EXISTS auth_user_api_tokens (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status INT,

    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NULL,

    CONSTRAINT fk_auth_user_api_tokens_user_id FOREIGN KEY (user_id) REFERENCES auth_users (id)
);

CREATE TABLE IF NOT EXISTS auth_permissions (
    role_id BIGINT NOT NULL,
    action_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,

    PRIMARY KEY (role_id, action_id, resource_id),
    CONSTRAINT fk_auth_permissions_role_id FOREIGN KEY (role_id) REFERENCES auth_roles (id),
    CONSTRAINT fk_auth_permissions_action_id FOREIGN KEY (action_id) REFERENCES auth_actions (id),
    CONSTRAINT fk_auth_permissions_resource_id FOREIGN KEY (resource_id) REFERENCES auth_resources (id)
);

CREATE TABLE IF NOT EXISTS auth_role_record_rules (
    role_id BIGINT NOT NULL,
    record_rule_id BIGINT NOT NULL,
    is_negated BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (role_id, record_rule_id),
    CONSTRAINT fk_auth_role_record_rules_role_id FOREIGN KEY (role_id) REFERENCES auth_roles (id),
    CONSTRAINT fk_auth_role_record_rules_record_rule_id FOREIGN KEY (record_rule_id) REFERENCES auth_record_rules (id)
);

CREATE TABLE IF NOT EXISTS auth_role_menus (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,

    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_auth_role_menus_role_id FOREIGN KEY (role_id) REFERENCES auth_roles (id),
    CONSTRAINT fk_auth_role_menus_menu_id FOREIGN KEY (menu_id) REFERENCES auth_menus (id)
);

CREATE TABLE IF NOT EXISTS auth_user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_auth_user_roles_user_id FOREIGN KEY (user_id) REFERENCES auth_users (id),
    CONSTRAINT fk_auth_user_roles_role_id FOREIGN KEY (role_id) REFERENCES auth_roles (id)
);

CREATE INDEX IF NOT EXISTS idx_auth_menus_parent_id ON auth_menus (parent_id);
CREATE INDEX IF NOT EXISTS idx_auth_resources_menu_id ON auth_resources (menu_id);
CREATE INDEX IF NOT EXISTS idx_auth_record_rules_resource_id ON auth_record_rules (resource_id);
CREATE INDEX IF NOT EXISTS idx_auth_password_resets_user_id ON auth_password_resets (user_id);
CREATE INDEX IF NOT EXISTS idx_auth_password_resets_expires_at ON auth_password_resets (expires_at);
CREATE INDEX IF NOT EXISTS idx_auth_password_histories_user_id ON auth_password_histories (user_id);
CREATE INDEX IF NOT EXISTS idx_auth_providers_provider_type ON auth_providers (provider_type);
CREATE INDEX IF NOT EXISTS idx_auth_providers_enabled ON auth_providers (enabled);
CREATE INDEX IF NOT EXISTS idx_auth_user_devices_user_id ON auth_user_devices (user_id);
CREATE INDEX IF NOT EXISTS idx_auth_user_devices_app_client_id ON auth_user_devices (app_client_id);
CREATE INDEX IF NOT EXISTS idx_auth_user_devices_trusted_expires_at ON auth_user_devices (trusted_expires_at);
CREATE INDEX IF NOT EXISTS idx_auth_user_sessions_expires_at ON auth_user_sessions (expires_at);
CREATE INDEX IF NOT EXISTS idx_auth_user_sessions_last_activity_at ON auth_user_sessions (last_activity_at);
CREATE INDEX IF NOT EXISTS idx_auth_user_sessions_user_device_id ON auth_user_sessions (user_device_id);
CREATE INDEX IF NOT EXISTS idx_auth_user_api_tokens_user_id ON auth_user_api_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_auth_user_api_tokens_expires_at ON auth_user_api_tokens (expires_at);
CREATE INDEX IF NOT EXISTS idx_auth_permissions_role_id ON auth_permissions (role_id);
CREATE INDEX IF NOT EXISTS idx_auth_permissions_action_id ON auth_permissions (action_id);
CREATE INDEX IF NOT EXISTS idx_auth_permissions_resource_id ON auth_permissions (resource_id);
CREATE INDEX IF NOT EXISTS idx_auth_role_record_rules_role_id ON auth_role_record_rules (role_id);
CREATE INDEX IF NOT EXISTS idx_auth_role_record_rules_record_rule_id ON auth_role_record_rules (record_rule_id);
CREATE INDEX IF NOT EXISTS idx_auth_role_menus_role_id ON auth_role_menus (role_id);
CREATE INDEX IF NOT EXISTS idx_auth_role_menus_menu_id ON auth_role_menus (menu_id);
CREATE INDEX IF NOT EXISTS idx_auth_user_roles_user_id ON auth_user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_auth_user_roles_role_id ON auth_user_roles (role_id);
