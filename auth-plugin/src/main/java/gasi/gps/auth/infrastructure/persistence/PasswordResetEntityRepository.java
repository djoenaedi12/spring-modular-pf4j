package gasi.gps.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.PasswordResetEntity;

/**
 * Spring Data JPA repository for PasswordResetEntity.
 */
@Repository
public interface PasswordResetEntityRepository extends JpaRepository<PasswordResetEntity, Long>,
        JpaSpecificationExecutor<PasswordResetEntity> {
}
