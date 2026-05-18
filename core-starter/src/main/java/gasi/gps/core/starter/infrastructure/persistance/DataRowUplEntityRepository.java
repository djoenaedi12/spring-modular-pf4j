package gasi.gps.core.starter.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.core.starter.infrastructure.entity.DataRowUplEntity;

/**
 * Spring Data JPA repository for DataRowUplEntity.
 */
@Repository
public interface DataRowUplEntityRepository extends
        JpaRepository<DataRowUplEntity, Long>,
        JpaSpecificationExecutor<DataRowUplEntity> {
}
