package gasi.gps.auth.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.AppClient;
import gasi.gps.auth.domain.port.outbound.AppClientRepositoryPort;
import gasi.gps.auth.infrastructure.entity.AppClientEntity;
import gasi.gps.auth.infrastructure.mapper.AppClientMapper;
import gasi.gps.auth.infrastructure.persistence.AppClientEntityRepository;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

@Component
public class AppClientRepositoryAdapter
        extends BaseRepositoryAdapter<AppClient, AppClientEntity>
        implements AppClientRepositoryPort {

    public AppClientRepositoryAdapter(AppClientEntityRepository repository,
            AppClientMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "AppClient";
    }
}
