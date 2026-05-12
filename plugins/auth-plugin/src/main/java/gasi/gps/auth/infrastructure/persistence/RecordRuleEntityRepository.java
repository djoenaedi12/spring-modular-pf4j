package gasi.gps.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.RecordRuleEntity;

/**
 * Spring Data JPA repository for RecordRuleEntity.
 */
@Repository
public interface RecordRuleEntityRepository extends
        JpaRepository<RecordRuleEntity, Long>,
        JpaSpecificationExecutor<RecordRuleEntity> {
}
