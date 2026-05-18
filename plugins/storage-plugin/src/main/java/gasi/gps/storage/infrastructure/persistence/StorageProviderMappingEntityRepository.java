package gasi.gps.storage.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import gasi.gps.storage.infrastructure.entity.StorageProviderMappingEntity;

/**
 * Spring Data repository for {@link StorageProviderMappingEntity}.
 *
 * @since 1.0.0
 */
public interface StorageProviderMappingEntityRepository
        extends JpaRepository<StorageProviderMappingEntity, Long>,
        JpaSpecificationExecutor<StorageProviderMappingEntity> {

    /**
     * Finds a mapping by resource type.
     *
     * @param resource resource type
     * @return matching mapping
     */
    Optional<StorageProviderMappingEntity> findByResource(String resource);
}
