package gasi.gps.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.ActionEntity;

/**
 * Spring Data JPA repository for ActionEntity.
 */
@Repository
public interface ActionEntityRepository extends
        JpaRepository<ActionEntity, Long>,
        JpaSpecificationExecutor<ActionEntity> {
}
