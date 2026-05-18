package gasi.gps.core.starter.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.domain.model.DataUpl;
import gasi.gps.core.api.domain.port.outbound.DataUplRepositoryPort;
import gasi.gps.core.starter.infrastructure.entity.DataUplEntity;
import gasi.gps.core.starter.infrastructure.mapper.DataUplMapper;
import gasi.gps.core.starter.infrastructure.persistance.DataUplEntityRepository;

@Component
public class DataUplRepositoryAdapter
        extends BaseRepositoryAdapter<DataUpl, DataUplEntity>
        implements DataUplRepositoryPort {

    public DataUplRepositoryAdapter(DataUplEntityRepository repository,
            DataUplMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "DataUpl";
    }
}
