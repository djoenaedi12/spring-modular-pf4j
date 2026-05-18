package gasi.gps.storage.domain.port.outbound;

import java.util.List;
import java.util.Optional;

import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;
import gasi.gps.storage.domain.model.Media;

/**
 * Repository port for {@link Media} persistence.
 *
 * @since 1.0.0
 */
public interface MediaRepositoryPort extends BaseRepositoryPort<Media> {

    /**
     * Finds a media record by its unique file key.
     *
     * @param fileKey unique file key
     * @return matching media
     */
    Optional<Media> findByFileKey(String fileKey);

    /**
     * Finds all media records for a given resource owner.
     *
     * @param resource   resource type
     * @param resourceId resource identifier
     * @return list of media records
     */
    List<Media> findByResourceAndResourceId(String resource, Long resourceId);
}
