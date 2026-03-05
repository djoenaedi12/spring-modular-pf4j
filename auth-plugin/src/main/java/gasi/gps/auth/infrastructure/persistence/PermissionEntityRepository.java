package gasi.gps.auth.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.PermissionEntity;
import gasi.gps.auth.infrastructure.entity.PermissionId;

/**
 * Spring Data JPA repository for PermissionEntity.
 */
@Repository
public interface PermissionEntityRepository extends
        JpaRepository<PermissionEntity, PermissionId>,
        JpaSpecificationExecutor<PermissionEntity> {

    List<PermissionEntity> findByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);
}
