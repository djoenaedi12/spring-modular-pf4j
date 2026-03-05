package gasi.gps.auth.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.RoleMenuEntity;
import gasi.gps.auth.infrastructure.entity.RoleMenuId;

/**
 * Spring Data JPA repository for RoleMenuEntity.
 */
@Repository
public interface RoleMenuEntityRepository extends
        JpaRepository<RoleMenuEntity, RoleMenuId>,
        JpaSpecificationExecutor<RoleMenuEntity> {

    List<RoleMenuEntity> findByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);
}
