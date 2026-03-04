package gasi.gps.auth.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.domain.port.outbound.RoleRepositoryPort;
import gasi.gps.auth.infrastructure.entity.RoleEntity;
import gasi.gps.auth.infrastructure.mapper.RoleMapper;
import gasi.gps.auth.infrastructure.persistence.RoleEntityRepository;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

@Component
public class RoleRepositoryAdapter
        extends BaseRepositoryAdapter<Role, RoleEntity>
        implements RoleRepositoryPort {

    public RoleRepositoryAdapter(RoleEntityRepository repository,
            RoleMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "Role";
    }

    @Override
    public Optional<Role> findByCode(String code) {
        GenericFilter codeFilter = SimpleFilter.builder()
                .field("code")
                .operator(SimpleFilter.FilterOperator.EQUALS)
                .value(code)
                .build();
        return findBy(codeFilter);
    }
}
