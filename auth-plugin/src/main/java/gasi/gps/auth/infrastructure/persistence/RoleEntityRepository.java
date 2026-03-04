package gasi.gps.auth.infrastructure.persistence;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.RoleEntity;

/**
 * Spring Data JPA repository for RoleEntity.
 */
@Repository
public interface RoleEntityRepository extends
        JpaRepository<RoleEntity, Long>,
        JpaSpecificationExecutor<RoleEntity> {
}
