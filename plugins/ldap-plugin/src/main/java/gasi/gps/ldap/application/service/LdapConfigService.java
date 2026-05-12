package gasi.gps.ldap.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.auth.application.service.AuthProviderRegistryService;
import gasi.gps.auth.application.service.AuthProviderRegistryService.AuthProviderConfig;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.ldap.application.dto.LdapConfigRequest;
import gasi.gps.ldap.application.dto.LdapConfigResponse;
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

    private final AuthProviderRegistryService authProviderRegistryService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs LdapConfigService.
     *
     * @param authProviderRegistryService generic auth provider registry
     * @param objectMapper                JSON mapper for settings payload
     */
    public LdapConfigService(AuthProviderRegistryService authProviderRegistryService, ObjectMapper objectMapper) {
        this.authProviderRegistryService = authProviderRegistryService;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves current LDAP settings, creating default row when missing.
     *
     * @return LDAP config response
     */
    public LdapConfigResponse get() {
        AuthProviderConfig provider = getOrCreateProvider();
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
        AuthProviderConfig provider = getOrCreateProvider();
        LdapProviderSettings existingSettings = readSettings(provider);
        LdapProviderSettings settings = LdapProviderSettings.builder()
                .ldapUrl(request.getLdapUrl())
                .baseDn(request.getBaseDn())
                .bindDn(request.getBindDn())
                .bindPassword(resolveBindPassword(request.getBindPassword(), existingSettings))
                .userSearchFilter(request.getUserSearchFilter())
                .connectTimeoutMs(request.getConnectTimeoutMs())
                .readTimeoutMs(request.getReadTimeoutMs())
                .build();

        AuthProviderConfig saved = authProviderRegistryService.save(
                LDAP_PROVIDER_ID,
                LDAP_PROVIDER_TYPE,
                request.isEnabled(),
                writeSettings(settings));
        return toResponse(saved, settings);
    }

    /**
     * Returns active LDAP config for authentication flow.
     *
     * @return active LDAP runtime config
     */
    public ActiveLdapConfig getActiveConfig() {
        AuthProviderConfig provider = getOrCreateProvider();
        LdapProviderSettings settings = readSettings(provider);

        return new ActiveLdapConfig(
                provider.enabled(),
                settings.getLdapUrl(),
                settings.getBaseDn(),
                settings.getBindDn(),
                settings.getBindPassword(),
                settings.getUserSearchFilter(),
                settings.getConnectTimeoutMs(),
                settings.getReadTimeoutMs());
    }

    private String resolveBindPassword(String requestedPassword, LdapProviderSettings existingSettings) {
        if (requestedPassword != null && !requestedPassword.isBlank()) {
            return requestedPassword;
        }
        return existingSettings.getBindPassword();
    }

    private AuthProviderConfig getOrCreateProvider() {
        return authProviderRegistryService.getOrCreate(
                LDAP_PROVIDER_ID,
                LDAP_PROVIDER_TYPE,
                false,
                writeSettings(defaultSettings()));
    }

    private LdapProviderSettings defaultSettings() {
        return LdapProviderSettings.builder()
                .ldapUrl("ldap://localhost:389")
                .baseDn("dc=example,dc=com")
                .bindDn("cn=admin,dc=example,dc=com")
                .bindPassword("change-me")
                .userSearchFilter("(uid={0})")
                .connectTimeoutMs(LdapConfigRequest.DEFAULT_CONNECT_TIMEOUT_MS)
                .readTimeoutMs(LdapConfigRequest.DEFAULT_READ_TIMEOUT_MS)
                .build();
    }

    private LdapProviderSettings readSettings(AuthProviderConfig provider) {
        try {
            return normalizeSettings(objectMapper.readValue(provider.settingsJson(), LdapProviderSettings.class));
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Invalid LDAP provider settings JSON");
        }
    }

    private LdapProviderSettings normalizeSettings(LdapProviderSettings settings) {
        LdapProviderSettings defaults = defaultSettings();
        if (settings == null) {
            return defaults;
        }
        if (settings.getConnectTimeoutMs() == null) {
            settings.setConnectTimeoutMs(defaults.getConnectTimeoutMs());
        }
        if (settings.getReadTimeoutMs() == null) {
            settings.setReadTimeoutMs(defaults.getReadTimeoutMs());
        }
        return settings;
    }

    private String writeSettings(LdapProviderSettings settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize LDAP provider settings");
        }
    }

    private LdapConfigResponse toResponse(AuthProviderConfig provider, LdapProviderSettings settings) {
        return LdapConfigResponse.builder()
                .enabled(provider.enabled())
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
     *
     * @param enabled provider enabled state
     * @param ldapUrl LDAP server URL
     * @param baseDn LDAP search base DN
     * @param bindDn service account bind DN
     * @param bindPassword service account bind password
     * @param userSearchFilter LDAP user search filter
     * @param connectTimeoutMs connect timeout in milliseconds
     * @param readTimeoutMs read timeout in milliseconds
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
