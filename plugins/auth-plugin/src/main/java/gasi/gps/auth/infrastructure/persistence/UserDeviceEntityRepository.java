package gasi.gps.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.auth.infrastructure.entity.UserDeviceEntity;

/**
 * Spring Data JPA repository for UserDeviceEntity.
 */
@Repository
public interface UserDeviceEntityRepository extends JpaRepository<UserDeviceEntity, Long>,
        JpaSpecificationExecutor<UserDeviceEntity> {
}
