package gasi.gps.dataupload.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.dataupload.infrastructure.entity.DataUplEntity;

/**
 * Spring Data JPA repository for DataUplEntity.
 */
@Repository
public interface DataUplEntityRepository extends
        JpaRepository<DataUplEntity, Long>,
        JpaSpecificationExecutor<DataUplEntity> {
}
