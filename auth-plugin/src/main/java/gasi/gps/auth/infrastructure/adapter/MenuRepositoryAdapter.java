package gasi.gps.auth.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.Menu;
import gasi.gps.auth.domain.port.outbound.MenuRepositoryPort;
import gasi.gps.auth.infrastructure.entity.MenuEntity;
import gasi.gps.auth.infrastructure.mapper.MenuMapper;
import gasi.gps.auth.infrastructure.persistence.MenuEntityRepository;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

@Component
public class MenuRepositoryAdapter
        extends BaseRepositoryAdapter<Menu, MenuEntity>
        implements MenuRepositoryPort {

    public MenuRepositoryAdapter(MenuEntityRepository repository,
            MenuMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "Menu";
    }
}
