package gasi.gps.core.starter.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.domain.model.DataRowUpl;
import gasi.gps.core.api.domain.port.outbound.DataRowUplRepositoryPort;
import gasi.gps.core.starter.infrastructure.entity.DataRowUplEntity;
import gasi.gps.core.starter.infrastructure.mapper.DataRowUplMapper;
import gasi.gps.core.starter.infrastructure.persistance.DataRowUplEntityRepository;

@Component
public class DataRowUplRepositoryAdapter
        extends BaseRepositoryAdapter<DataRowUpl, DataRowUplEntity>
        implements DataRowUplRepositoryPort {

    public DataRowUplRepositoryAdapter(DataRowUplEntityRepository repository,
            DataRowUplMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "DataRowUpl";
    }
}
