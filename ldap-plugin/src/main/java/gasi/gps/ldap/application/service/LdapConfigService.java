package gasi.gps.ldap.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.ldap.application.dto.LdapConfigRequest;
import gasi.gps.ldap.application.dto.LdapConfigResponse;
import gasi.gps.ldap.infrastructure.entity.AuthProviderEntity;
import gasi.gps.ldap.infrastructure.persistence.AuthProviderRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Service for user-managed LDAP runtime configuration.
 */
@Service
@Transactional
public class LdapConfigService {

    public static final String LDAP_PROVIDER_ID = "ldap";
    public static final String LDAP_PROVIDER_TYPE = "LDAP";

    private final AuthProviderRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Constructs LdapConfigService.
     *
     * @param repository   generic auth provider repository
     * @param objectMapper JSON mapper for settings payload
     */
    public LdapConfigService(AuthProviderRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves current LDAP settings, creating default row when missing.
     *
     * @return LDAP config response
     */
    @Transactional(readOnly = true)
    public LdapConfigResponse get() {
        AuthProviderEntity provider = getOrCreateProvider();
        LdapProviderSettings settings = readSettings(provider);
        return toResponse(provider, settings);
    }

    /**
     * Updates LDAP settings from user input.
     *
     * @param request update request
     * @return updated LDAP config response
     */
    public LdapConfigResponse update(LdapConfigRequest request) {
        AuthProviderEntity provider = getOrCreateProvider();
        LdapProviderSettings settings = LdapProviderSettings.builder()
                .ldapUrl(request.getLdapUrl())
                .baseDn(request.getBaseDn())
                .bindDn(request.getBindDn())
                .bindPassword(request.getBindPassword())
                .userSearchFilter(request.getUserSearchFilter())
                .connectTimeoutMs(request.getConnectTimeoutMs())
                .readTimeoutMs(request.getReadTimeoutMs())
                .build();

        provider.setEnabled(request.isEnabled());
        provider.setSettingsJson(writeSettings(settings));
        AuthProviderEntity saved = repository.save(provider);
        return toResponse(saved, settings);
    }

    /**
     * Returns active LDAP config for authentication flow.
     *
     * @return active LDAP runtime config
     */
    @Transactional(readOnly = true)
    public ActiveLdapConfig getActiveConfig() {
        AuthProviderEntity provider = getOrCreateProvider();
        LdapProviderSettings settings = readSettings(provider);

        return new ActiveLdapConfig(
                Boolean.TRUE.equals(provider.getEnabled()),
                settings.getLdapUrl(),
                settings.getBaseDn(),
                settings.getBindDn(),
                settings.getBindPassword(),
                settings.getUserSearchFilter(),
                settings.getConnectTimeoutMs(),
                settings.getReadTimeoutMs());
    }

    private AuthProviderEntity getOrCreateProvider() {
        return repository.findByProviderId(LDAP_PROVIDER_ID)
                .orElseGet(() -> repository.save(defaultProvider()));
    }

    private AuthProviderEntity defaultProvider() {
        return AuthProviderEntity.builder()
                .providerId(LDAP_PROVIDER_ID)
                .providerType(LDAP_PROVIDER_TYPE)
                .enabled(false)
                .settingsJson(writeSettings(defaultSettings()))
                .build();
    }

    private LdapProviderSettings defaultSettings() {
        return LdapProviderSettings.builder()
                .ldapUrl("ldap://localhost:389")
                .baseDn("dc=example,dc=com")
                .bindDn("cn=admin,dc=example,dc=com")
                .bindPassword("change-me")
                .userSearchFilter("(uid={0})")
                .connectTimeoutMs(5000)
                .readTimeoutMs(5000)
                .build();
    }

    private LdapProviderSettings readSettings(AuthProviderEntity provider) {
        try {
            return objectMapper.readValue(provider.getSettingsJson(), LdapProviderSettings.class);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Invalid LDAP provider settings JSON");
        }
    }

    private String writeSettings(LdapProviderSettings settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize LDAP provider settings");
        }
    }

    private LdapConfigResponse toResponse(AuthProviderEntity provider, LdapProviderSettings settings) {
        return LdapConfigResponse.builder()
                .enabled(Boolean.TRUE.equals(provider.getEnabled()))
                .ldapUrl(settings.getLdapUrl())
                .baseDn(settings.getBaseDn())
                .bindDn(settings.getBindDn())
                .bindPasswordMasked(maskPassword(settings.getBindPassword()))
                .userSearchFilter(settings.getUserSearchFilter())
                .connectTimeoutMs(settings.getConnectTimeoutMs())
                .readTimeoutMs(settings.getReadTimeoutMs())
                .build();
    }

    private String maskPassword(String password) {
        if (password == null || password.isBlank()) {
            return "";
        }
        return "********";
    }

    /**
     * Runtime LDAP configuration used by provider auth flow.
     */
    public record ActiveLdapConfig(
            boolean enabled,
            String ldapUrl,
            String baseDn,
            String bindDn,
            String bindPassword,
            String userSearchFilter,
            Integer connectTimeoutMs,
            Integer readTimeoutMs) {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LdapProviderSettings {
        private String ldapUrl;
        private String baseDn;
        private String bindDn;
        private String bindPassword;
        private String userSearchFilter;
        private Integer connectTimeoutMs;
        private Integer readTimeoutMs;
    }
}
