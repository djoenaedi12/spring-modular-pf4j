package gasi.gps.auth.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.Action;
import gasi.gps.auth.domain.port.outbound.ActionRepositoryPort;
import gasi.gps.auth.infrastructure.entity.ActionEntity;
import gasi.gps.auth.infrastructure.mapper.ActionMapper;
import gasi.gps.auth.infrastructure.persistence.ActionEntityRepository;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

@Component
public class ActionRepositoryAdapter
        extends BaseRepositoryAdapter<Action, ActionEntity>
        implements ActionRepositoryPort {

    public ActionRepositoryAdapter(ActionEntityRepository repository,
            ActionMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "Action";
    }
}
