CREATE TABLE IF NOT EXISTS auth_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    origin_id BIGINT,
    status VARCHAR(255),
    version BIGINT,
    provider_id VARCHAR(100) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL,
    settings_json TEXT NOT NULL,
    CONSTRAINT uq_auth_providers_provider_id UNIQUE (provider_id)
);

INSERT INTO auth_providers (
    provider_id,
    provider_type,
    enabled,
    settings_json
)
SELECT
    'ldap',
    'LDAP',
    c.enabled,
    JSON_OBJECT(
        'ldapUrl', c.ldap_url,
        'baseDn', c.base_dn,
        'bindDn', c.bind_dn,
        'bindPassword', c.bind_password,
        'userSearchFilter', c.user_search_filter,
        'connectTimeoutMs', c.connect_timeout_ms,
        'readTimeoutMs', c.read_timeout_ms
    )
FROM ldap_auth_config c
WHERE c.config_key = 'default'
  AND NOT EXISTS (SELECT 1 FROM auth_providers p WHERE p.provider_id = 'ldap');

INSERT INTO auth_providers (
    provider_id,
    provider_type,
    enabled,
    settings_json
)
SELECT
    'ldap',
    'LDAP',
    FALSE,
    '{"ldapUrl":"ldap://localhost:389","baseDn":"dc=example,dc=com","bindDn":"cn=admin,dc=example,dc=com","bindPassword":"change-me","userSearchFilter":"(uid={0})","connectTimeoutMs":5000,"readTimeoutMs":5000}'
WHERE NOT EXISTS (SELECT 1 FROM auth_providers p WHERE p.provider_id = 'ldap');
