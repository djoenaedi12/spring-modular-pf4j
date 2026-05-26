package gasi.gps.dataupload.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.core.starter.infrastructure.adapter.BaseRepositoryAdapter;
import gasi.gps.dataupload.domain.model.DataUpl;
import gasi.gps.dataupload.domain.port.outbound.DataUplRepositoryPort;
import gasi.gps.dataupload.infrastructure.entity.DataUplEntity;
import gasi.gps.dataupload.infrastructure.mapper.DataUplMapper;
import gasi.gps.dataupload.infrastructure.persistance.DataUplEntityRepository;

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
