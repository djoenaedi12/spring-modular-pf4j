package gasi.gps.storage.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import gasi.gps.storage.infrastructure.entity.MediaEntity;

/**
 * Spring Data repository for {@link MediaEntity}.
 *
 * @since 1.0.0
 */
public interface MediaEntityRepository
        extends JpaRepository<MediaEntity, Long>, JpaSpecificationExecutor<MediaEntity> {

    /**
     * Finds a media entity by its unique file key.
     *
     * @param fileKey unique file key
     * @return matching entity
     */
    Optional<MediaEntity> findByFileKey(String fileKey);
}
