INSERT INTO auth_providers (
    id,
    created_at,
    version,
    provider_id,
    provider_type,
    enabled,
    settings_json
)
SELECT
    NEXT VALUE FOR global_seq,
    CURRENT_TIMESTAMP,
    0,
    'ldap',
    'LDAP',
    FALSE,
    '{"ldapUrl":"ldap://localhost:389","baseDn":"dc=example,dc=com","bindDn":"cn=admin,dc=example,dc=com","bindPassword":"change-me","userSearchFilter":"(uid={0})","connectTimeoutMs":5000,"readTimeoutMs":5000}'
WHERE NOT EXISTS (
    SELECT 1
    FROM auth_providers
    WHERE provider_id = 'ldap'
);
