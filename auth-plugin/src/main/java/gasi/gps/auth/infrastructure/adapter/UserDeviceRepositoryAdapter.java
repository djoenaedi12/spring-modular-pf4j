package gasi.gps.auth.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.UserDevice;
import gasi.gps.auth.domain.port.outbound.UserDeviceRepositoryPort;
import gasi.gps.auth.infrastructure.entity.UserDeviceEntity;
import gasi.gps.auth.infrastructure.mapper.UserDeviceMapper;
import gasi.gps.auth.infrastructure.persistence.UserDeviceEntityRepository;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

/**
 * Repository adapter for user device persistence.
 */
@Component
public class UserDeviceRepositoryAdapter extends BaseRepositoryAdapter<UserDevice, UserDeviceEntity>
        implements UserDeviceRepositoryPort {

    public UserDeviceRepositoryAdapter(UserDeviceEntityRepository repository,
            UserDeviceMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "UserDevice";
    }
}
