package gasi.gps.auth.infrastructure.persistence;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.AppClientEntity;

/**
 * Spring Data JPA repository for AppClientEntity.
 */
@Repository
public interface AppClientEntityRepository extends
        JpaRepository<AppClientEntity, Long>,
        JpaSpecificationExecutor<AppClientEntity> {
}
