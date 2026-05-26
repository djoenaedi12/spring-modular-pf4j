package gasi.gps.dataupload.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.core.starter.infrastructure.adapter.BaseRepositoryAdapter;
import gasi.gps.dataupload.domain.model.DataRowUpl;
import gasi.gps.dataupload.domain.port.outbound.DataRowUplRepositoryPort;
import gasi.gps.dataupload.infrastructure.entity.DataRowUplEntity;
import gasi.gps.dataupload.infrastructure.mapper.DataRowUplMapper;
import gasi.gps.dataupload.infrastructure.persistance.DataRowUplEntityRepository;

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
