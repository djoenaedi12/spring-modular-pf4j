package gasi.gps.core.starter.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.core.starter.infrastructure.entity.DataUplEntity;

/**
 * Spring Data JPA repository for DataUplEntity.
 */
@Repository
public interface DataUplEntityRepository extends
        JpaRepository<DataUplEntity, Long>,
        JpaSpecificationExecutor<DataUplEntity> {
}
