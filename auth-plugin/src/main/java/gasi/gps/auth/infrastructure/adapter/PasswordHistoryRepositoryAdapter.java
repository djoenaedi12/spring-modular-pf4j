package gasi.gps.auth.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.PasswordHistory;
import gasi.gps.auth.domain.port.outbound.PasswordHistoryRepositoryPort;
import gasi.gps.auth.infrastructure.entity.PasswordHistoryEntity;
import gasi.gps.auth.infrastructure.mapper.PasswordHistoryMapper;
import gasi.gps.auth.infrastructure.persistence.PasswordHistoryEntityRepository;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

/**
 * Repository adapter for password history records.
 */
@Component
public class PasswordHistoryRepositoryAdapter
        extends BaseRepositoryAdapter<PasswordHistory, PasswordHistoryEntity>
        implements PasswordHistoryRepositoryPort {

    public PasswordHistoryRepositoryAdapter(PasswordHistoryEntityRepository repository,
            PasswordHistoryMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(Long userId) {
        GenericFilter userFilter = SimpleFilter.builder()
                .field("user.id")
                .operator(SimpleFilter.FilterOperator.EQUALS)
                .value(userId)
                .build();
        return findAll(userFilter, List.of(SortOrder.desc("createdAt")), false);
    }

    @Override
    protected String resourceType() {
        return "PasswordHistory";
    }
}
