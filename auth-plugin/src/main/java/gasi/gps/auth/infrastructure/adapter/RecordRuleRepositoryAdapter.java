package gasi.gps.auth.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.RecordRule;
import gasi.gps.auth.domain.port.outbound.RecordRuleRepositoryPort;
import gasi.gps.auth.infrastructure.entity.RecordRuleEntity;
import gasi.gps.auth.infrastructure.mapper.RecordRuleMapper;
import gasi.gps.auth.infrastructure.persistence.RecordRuleEntityRepository;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

@Component
public class RecordRuleRepositoryAdapter
        extends BaseRepositoryAdapter<RecordRule, RecordRuleEntity>
        implements RecordRuleRepositoryPort {

    public RecordRuleRepositoryAdapter(RecordRuleEntityRepository repository,
            RecordRuleMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "RecordRule";
    }
}
