package gasi.gps.auth.infrastructure.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.UserDevice;
import gasi.gps.auth.domain.port.outbound.UserDeviceRepositoryPort;
import gasi.gps.auth.infrastructure.entity.AppClientEntity;
import gasi.gps.auth.infrastructure.entity.UserDeviceEntity;
import gasi.gps.auth.infrastructure.entity.UserEntity;
import gasi.gps.auth.infrastructure.mapper.UserDeviceMapper;
import gasi.gps.auth.infrastructure.persistence.AppClientEntityRepository;
import gasi.gps.auth.infrastructure.persistence.UserDeviceEntityRepository;
import gasi.gps.auth.infrastructure.persistence.UserEntityRepository;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.domain.model.AndFilter;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;
import gasi.gps.core.api.infrastructure.specification.GenericSpecification;

/**
 * Repository adapter for user device persistence.
 */
@Component
public class UserDeviceRepositoryAdapter
        extends BaseRepositoryAdapter<UserDevice, UserDeviceEntity>
        implements UserDeviceRepositoryPort {

    private final UserDeviceMapper userDeviceMapper;
    private final UserEntityRepository userEntityRepository;
    private final AppClientEntityRepository appClientEntityRepository;
    private final UserDeviceEntityRepository userDeviceEntityRepository;

    public UserDeviceRepositoryAdapter(UserEntityRepository repository,
            UserDeviceMapper mapper,
            AppClientEntityRepository appClientEntityRepository,
            UserDeviceEntityRepository userDeviceEntityRepository) {
        super(userDeviceEntityRepository, mapper);
        this.userDeviceMapper = mapper;
        this.userEntityRepository = repository;
        this.appClientEntityRepository = appClientEntityRepository;
        this.userDeviceEntityRepository = userDeviceEntityRepository;
    }

    @Override
    public UserDevice save(UserDevice model) {
        Long userId = model.getUser() != null ? model.getUser().getId() : null;
        Long appClientId = model.getAppClient() != null ? model.getAppClient().getId() : null;

        if (userId == null || appClientId == null) {
            throw new BusinessException("User and app client are required");
        }

        UserEntity user = userEntityRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        AppClientEntity appClient = appClientEntityRepository.findById(appClientId)
                .orElseThrow(() -> new BusinessException("App client not found"));

        String resolvedDeviceId = (model.getDeviceId() == null || model.getDeviceId().isBlank())
                ? UUID.randomUUID().toString()
                : model.getDeviceId();

        GenericFilter deviceFilter = AndFilter.builder()
                .filters(List.of(
                        SimpleFilter.builder()
                                .field("user.id")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(userId)
                                .build(),
                        SimpleFilter.builder()
                                .field("appClient.id")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(appClientId)
                                .build(),
                        SimpleFilter.builder()
                                .field("deviceId")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(resolvedDeviceId)
                                .build()))
                .build();

        UserDeviceEntity userDevice = userDeviceEntityRepository.findOne(GenericSpecification.from(deviceFilter))
                .orElseGet(() -> UserDeviceEntity.builder().build());

        userDevice.setUser(user);
        userDevice.setAppClient(appClient);
        userDeviceMapper.updateEntity(model, userDevice);
        userDevice.setDeviceId(resolvedDeviceId);

        UserDeviceEntity saved = userDeviceEntityRepository.save(userDevice);
        return userDeviceMapper.toDomain(saved);
    }

    @Override
    protected String resourceType() {
        return "UserDevice";
    }
}
