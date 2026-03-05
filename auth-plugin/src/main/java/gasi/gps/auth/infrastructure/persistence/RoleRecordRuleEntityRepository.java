package gasi.gps.auth.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.RoleRecordRuleEntity;
import gasi.gps.auth.infrastructure.entity.RoleRecordRuleId;

/**
 * Spring Data JPA repository for RoleRecordRuleEntity.
 */
@Repository
public interface RoleRecordRuleEntityRepository extends
        JpaRepository<RoleRecordRuleEntity, RoleRecordRuleId>,
        JpaSpecificationExecutor<RoleRecordRuleEntity> {

    List<RoleRecordRuleEntity> findByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);
}
