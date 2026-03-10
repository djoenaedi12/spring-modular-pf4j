package gasi.gps.auth.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.UserSession;
import gasi.gps.auth.domain.port.outbound.UserSessionRepositoryPort;
import gasi.gps.auth.infrastructure.entity.UserSessionEntity;
import gasi.gps.auth.infrastructure.mapper.UserSessionMapper;
import gasi.gps.auth.infrastructure.persistence.UserSessionEntityRepository;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

/**
 * Repository adapter for user login session persistence.
 */
@Component
public class UserSessionRepositoryAdapter
        extends BaseRepositoryAdapter<UserSession, UserSessionEntity>
        implements UserSessionRepositoryPort {

    public UserSessionRepositoryAdapter(UserSessionEntityRepository repository,
            UserSessionMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "UserSession";
    }
}
