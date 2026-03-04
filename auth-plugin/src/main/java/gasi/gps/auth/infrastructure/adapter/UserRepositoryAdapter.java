package gasi.gps.auth.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.User;
import gasi.gps.auth.domain.port.outbound.UserRepositoryPort;
import gasi.gps.auth.infrastructure.entity.UserEntity;
import gasi.gps.auth.infrastructure.mapper.UserMapper;
import gasi.gps.auth.infrastructure.persistence.UserEntityRepository;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

@Component
public class UserRepositoryAdapter
        extends BaseRepositoryAdapter<User, UserEntity>
        implements UserRepositoryPort {

    public UserRepositoryAdapter(UserEntityRepository repository,
            UserMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "User";
    }

    @Override
    public Optional<User> findByUsername(String username) {
        GenericFilter usernameFilter = SimpleFilter.builder()
                .field("username")
                .operator(SimpleFilter.FilterOperator.EQUALS)
                .value(username)
                .build();
        return findBy(usernameFilter);
    }
}
