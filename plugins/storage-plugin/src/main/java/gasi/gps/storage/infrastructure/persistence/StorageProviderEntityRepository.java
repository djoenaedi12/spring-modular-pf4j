package gasi.gps.storage.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import gasi.gps.storage.infrastructure.entity.StorageProviderEntity;

/**
 * Spring Data repository for {@link StorageProviderEntity}.
 *
 * @since 1.0.0
 */
public interface StorageProviderEntityRepository
        extends JpaRepository<StorageProviderEntity, Long>,
        JpaSpecificationExecutor<StorageProviderEntity> {

    /**
     * Finds the provider marked as default.
     *
     * @return default provider entity
     */
    Optional<StorageProviderEntity> findByIsDefaultTrue();

    /**
     * Finds a provider by its unique code.
     *
     * @param code provider code
     * @return matching entity
     */
    Optional<StorageProviderEntity> findByCode(String code);
}
