package gasi.gps.auth.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.Resource;
import gasi.gps.auth.domain.port.outbound.ResourceRepositoryPort;
import gasi.gps.auth.infrastructure.entity.ResourceEntity;
import gasi.gps.auth.infrastructure.mapper.ResourceMapper;
import gasi.gps.auth.infrastructure.persistence.ResourceEntityRepository;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

@Component
public class ResourceRepositoryAdapter
        extends BaseRepositoryAdapter<Resource, ResourceEntity>
        implements ResourceRepositoryPort {

    public ResourceRepositoryAdapter(ResourceEntityRepository repository,
            ResourceMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "Resource";
    }
}
