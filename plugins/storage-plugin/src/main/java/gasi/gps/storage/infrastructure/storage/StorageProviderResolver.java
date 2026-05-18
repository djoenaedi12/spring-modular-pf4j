package gasi.gps.storage.infrastructure.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.storage.infrastructure.persistence.StorageProviderEntityRepository;
import gasi.gps.storage.infrastructure.persistence.StorageProviderMappingEntityRepository;

/**
 * Resolves which storage provider to use for a given resource type.
 *
 * <p>Resolution order:</p>
 * <ol>
 *   <li>Check {@code storage_provider_mappings} for an exact resource match</li>
 *   <li>Fall back to the default provider ({@code is_default = true})</li>
 * </ol>
 *
 * @since 1.0.0
 */
@Component
public class StorageProviderResolver {

    private static final Logger LOG = LoggerFactory.getLogger(StorageProviderResolver.class);

    private final StorageProviderMappingEntityRepository mappingRepo;
    private final StorageProviderEntityRepository providerRepo;

    /**
     * Creates the resolver.
     *
     * @param mappingRepo  mapping repository
     * @param providerRepo provider repository
     */
    public StorageProviderResolver(StorageProviderMappingEntityRepository mappingRepo,
            StorageProviderEntityRepository providerRepo) {
        this.mappingRepo = mappingRepo;
        this.providerRepo = providerRepo;
    }

    /**
     * Resolves the provider ID for a given resource type.
     *
     * @param resource resource type, e.g. {@code "USER_AVATAR"}
     * @return database ID of the resolved storage provider
     * @throws BusinessException if no mapping and no default provider exist
     */
    public Long resolveProviderId(String resource) {
        // 1. Exact mapping
        var mapping = mappingRepo.findByResource(resource);
        if (mapping.isPresent()) {
            LOG.debug("Resolved provider via mapping: resource={} → providerId={}",
                    resource, mapping.get().getProviderId());
            return mapping.get().getProviderId();
        }

        // 2. Default provider
        return providerRepo.findByIsDefaultTrue()
                .orElseThrow(() -> new BusinessException(
                        "No storage provider mapping for resource '" + resource
                                + "' and no default provider configured"))
                .getId();
    }
}
