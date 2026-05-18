package gasi.gps.storage.infrastructure.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.storage.FileStorageProvider;
import gasi.gps.core.api.storage.FileStorageProviderFactory;
import gasi.gps.storage.infrastructure.entity.StorageProviderEntity;
import gasi.gps.storage.infrastructure.persistence.StorageProviderEntityRepository;

/**
 * Registry that creates, caches, and manages {@link FileStorageProvider}
 * instances from database configuration.
 *
 * <p>Provider factories are discovered via PF4J from all installed
 * storage plugins. Provider instances are lazily created on first
 * access and cached for reuse.</p>
 *
 * @since 1.0.0
 */
@Component
public class StorageProviderRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(StorageProviderRegistry.class);

    private final ConcurrentHashMap<Long, FileStorageProvider> cache = new ConcurrentHashMap<>();
    private final StorageProviderEntityRepository providerRepo;
    private final PluginManager pluginManager;

    /**
     * Creates the registry.
     *
     * @param providerRepo  storage provider repository
     * @param pluginManager PF4J plugin manager for factory discovery
     */
    public StorageProviderRegistry(StorageProviderEntityRepository providerRepo,
            PluginManager pluginManager) {
        this.providerRepo = providerRepo;
        this.pluginManager = pluginManager;
    }

    /**
     * Returns a cached or newly created provider instance for the given ID.
     *
     * @param providerId database ID of the storage provider
     * @return ready-to-use provider instance
     * @throws BusinessException if the provider is not found, disabled,
     *                           or no factory is installed for its type
     */
    public FileStorageProvider getProvider(Long providerId) {
        return cache.computeIfAbsent(providerId, this::createProvider);
    }

    /**
     * Returns metadata about all installed provider types (from plugins).
     *
     * @return list of available provider type descriptors
     */
    public List<AvailableProviderType> getAvailableTypes() {
        return discoverFactories().values().stream()
                .map(f -> new AvailableProviderType(
                        f.getProviderType(), f.getConfigFields()))
                .toList();
    }

    /**
     * Returns the set of provider type strings currently installed.
     *
     * @return set of type identifiers
     */
    public Set<String> getInstalledTypes() {
        return discoverFactories().keySet();
    }

    /**
     * Validates configuration for a given provider type.
     *
     * @param providerType type identifier
     * @param config       configuration map to validate
     * @throws BusinessException if no factory is installed or config is invalid
     */
    public void validateConfig(String providerType, Map<String, Object> config) {
        FileStorageProviderFactory factory = discoverFactories().get(providerType);
        if (factory == null) {
            throw new BusinessException(
                    "No plugin installed for provider type: " + providerType);
        }
        factory.validate(config);
    }

    /**
     * Evicts a single provider from the cache, forcing re-creation on next access.
     *
     * @param providerId database ID to evict
     */
    public void evict(Long providerId) {
        cache.remove(providerId);
        LOG.debug("Evicted provider cache for id={}", providerId);
    }

    /**
     * Evicts all providers from the cache.
     */
    public void evictAll() {
        cache.clear();
        LOG.debug("Evicted all provider caches");
    }

    private FileStorageProvider createProvider(Long providerId) {
        StorageProviderEntity entity = providerRepo.findById(providerId)
                .orElseThrow(() -> new BusinessException(
                        "Storage provider not found: " + providerId));

        if (!entity.isEnabled()) {
            throw new BusinessException(
                    "Storage provider is disabled: " + entity.getCode());
        }

        Map<String, FileStorageProviderFactory> factories = discoverFactories();
        FileStorageProviderFactory factory = factories.get(entity.getProviderType());
        if (factory == null) {
            throw new BusinessException(
                    "No plugin installed for provider type: " + entity.getProviderType()
                            + ". Available types: " + factories.keySet());
        }

        LOG.info("Creating storage provider instance: code={}, type={}",
                entity.getCode(), entity.getProviderType());
        return factory.create(entity.getConfigAsMap());
    }

    private Map<String, FileStorageProviderFactory> discoverFactories() {
        List<FileStorageProviderFactory> factories =
                pluginManager.getExtensions(FileStorageProviderFactory.class);
        return factories.stream()
                .collect(Collectors.toMap(
                        FileStorageProviderFactory::getProviderType,
                        Function.identity(),
                        (a, b) -> a));
    }

    /**
     * Descriptor for an available provider type discovered from plugins.
     *
     * @param providerType type identifier
     * @param configFields required configuration fields
     */
    public record AvailableProviderType(
            String providerType,
            List<FileStorageProviderFactory.ConfigField> configFields) { }
}
