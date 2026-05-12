package gasi.gps.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.AuthProviderEntity;

/**
 * Repository for generic authentication provider configuration.
 */
@Repository
public interface AuthProviderEntityRepository extends JpaRepository<AuthProviderEntity, Long> {

    /**
     * Finds provider config by provider ID.
     *
     * @param providerId provider identifier
     * @return matching provider row if exists
     */
    Optional<AuthProviderEntity> findByProviderId(String providerId);
}
