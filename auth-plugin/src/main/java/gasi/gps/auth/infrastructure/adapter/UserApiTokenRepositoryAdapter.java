package gasi.gps.auth.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.UserApiToken;
import gasi.gps.auth.domain.port.outbound.UserApiTokenRepositoryPort;
import gasi.gps.auth.infrastructure.entity.UserApiTokenEntity;
import gasi.gps.auth.infrastructure.mapper.UserApiTokenMapper;
import gasi.gps.auth.infrastructure.persistence.UserApiTokenEntityRepository;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

/**
 * Repository adapter for user API tokens.
 */
@Component
public class UserApiTokenRepositoryAdapter extends BaseRepositoryAdapter<UserApiToken, UserApiTokenEntity>
        implements UserApiTokenRepositoryPort {

    public UserApiTokenRepositoryAdapter(UserApiTokenEntityRepository repository,
            UserApiTokenMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "UserApiToken";
    }
}
