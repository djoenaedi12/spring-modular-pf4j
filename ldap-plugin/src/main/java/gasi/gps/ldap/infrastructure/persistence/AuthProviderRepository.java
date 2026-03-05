package gasi.gps.ldap.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import gasi.gps.ldap.infrastructure.entity.AuthProviderEntity;

/**
 * Repository for generic authentication provider configuration.
 */
public interface AuthProviderRepository extends JpaRepository<AuthProviderEntity, Long> {

    /**
     * Finds provider config by provider ID.
     *
     * @param providerId provider identifier
     * @return matching provider row if exists
     */
    Optional<AuthProviderEntity> findByProviderId(String providerId);
}
