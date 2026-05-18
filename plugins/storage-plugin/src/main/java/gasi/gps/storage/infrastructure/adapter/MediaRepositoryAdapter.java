package gasi.gps.storage.infrastructure.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.model.AndFilter;
import gasi.gps.core.starter.infrastructure.adapter.BaseRepositoryAdapter;
import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;
import gasi.gps.storage.domain.model.Media;
import gasi.gps.storage.domain.port.outbound.MediaRepositoryPort;
import gasi.gps.storage.infrastructure.entity.MediaEntity;
import gasi.gps.storage.infrastructure.persistence.MediaEntityRepository;

/**
 * JPA adapter for {@link MediaRepositoryPort}.
 *
 * @since 1.0.0
 */
@Component
public class MediaRepositoryAdapter extends BaseRepositoryAdapter<Media, MediaEntity>
        implements MediaRepositoryPort {

    private final MediaEntityRepository repository;
    private final BaseMapper<Media, MediaEntity> mapper;

    /**
     * Creates the adapter.
     *
     * @param repository JPA repository
     * @param mapper     entity mapper
     */
    protected MediaRepositoryAdapter(MediaEntityRepository repository,
            BaseMapper<Media, MediaEntity> mapper) {
        super(repository, mapper);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    protected String resourceType() {
        return "Media";
    }

    @Override
    public Optional<Media> findByFileKey(String fileKey) {
        return repository.findByFileKey(fileKey).map(mapper::toDomain);
    }

    @Override
    public List<Media> findByResourceAndResourceId(String resource, Long resourceId) {
        AndFilter filter = AndFilter.builder()
                .filters(List.of(
                        SimpleFilter.builder()
                                .field("resource")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(resource)
                                .build(),
                        SimpleFilter.builder()
                                .field("resourceId")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(resourceId)
                                .build()))
                .build();
        return findAll(filter, Collections.emptyList(), false);
    }
}
