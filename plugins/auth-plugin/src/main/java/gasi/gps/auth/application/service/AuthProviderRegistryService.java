package gasi.gps.auth.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.auth.infrastructure.entity.AuthProviderEntity;
import gasi.gps.auth.infrastructure.persistence.AuthProviderEntityRepository;

/**
 * Registry service for generic authentication provider configuration.
 */
@Service
@Transactional
public class AuthProviderRegistryService {

    private final AuthProviderEntityRepository repository;

    /**
     * Constructs AuthProviderRegistryService.
     *
     * @param repository auth provider repository
     */
    public AuthProviderRegistryService(AuthProviderEntityRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds a provider config, creating it with defaults when missing.
     *
     * @param providerId          provider identifier
     * @param providerType        provider type
     * @param defaultEnabled      default enabled state
     * @param defaultSettingsJson default settings payload
     * @return provider config
     */
    public AuthProviderConfig getOrCreate(String providerId, String providerType, boolean defaultEnabled,
            String defaultSettingsJson) {
        return repository.findByProviderId(providerId)
                .map(this::toConfig)
                .orElseGet(() -> toConfig(repository.save(AuthProviderEntity.builder()
                        .providerId(providerId)
                        .providerType(providerType)
                        .enabled(defaultEnabled)
                        .settingsJson(defaultSettingsJson)
                        .build())));
    }

    /**
     * Saves a provider config.
     *
     * @param providerId   provider identifier
     * @param providerType provider type
     * @param enabled      enabled state
     * @param settingsJson settings payload
     * @return saved provider config
     */
    public AuthProviderConfig save(String providerId, String providerType, boolean enabled, String settingsJson) {
        AuthProviderEntity provider = repository.findByProviderId(providerId)
                .orElseGet(() -> AuthProviderEntity.builder()
                        .providerId(providerId)
                        .providerType(providerType)
                        .build());

        provider.setProviderType(providerType);
        provider.setEnabled(enabled);
        provider.setSettingsJson(settingsJson);
        return toConfig(repository.save(provider));
    }

    private AuthProviderConfig toConfig(AuthProviderEntity entity) {
        return new AuthProviderConfig(
                entity.getProviderId(),
                entity.getProviderType(),
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getSettingsJson());
    }

    /**
     * Authentication provider configuration value.
     */
    public record AuthProviderConfig(
            String providerId,
            String providerType,
            boolean enabled,
            String settingsJson) {
    }
}
