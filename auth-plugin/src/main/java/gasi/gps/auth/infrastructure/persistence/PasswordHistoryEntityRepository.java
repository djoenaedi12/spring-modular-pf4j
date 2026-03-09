package gasi.gps.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.PasswordHistoryEntity;

/**
 * Spring Data JPA repository for PasswordHistoryEntity.
 */
@Repository
public interface PasswordHistoryEntityRepository extends JpaRepository<PasswordHistoryEntity, Long>,
        JpaSpecificationExecutor<PasswordHistoryEntity> {
}
