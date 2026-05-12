package gasi.gps.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.ResourceEntity;

/**
 * Spring Data JPA repository for ResourceEntity.
 */
@Repository
public interface ResourceEntityRepository extends
        JpaRepository<ResourceEntity, Long>,
        JpaSpecificationExecutor<ResourceEntity> {
}
