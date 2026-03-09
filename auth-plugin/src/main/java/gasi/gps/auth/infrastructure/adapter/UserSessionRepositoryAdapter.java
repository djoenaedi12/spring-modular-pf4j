package gasi.gps.auth.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.UserDevice;
import gasi.gps.auth.domain.model.UserSession;
import gasi.gps.auth.domain.port.outbound.UserDeviceRepositoryPort;
import gasi.gps.auth.domain.port.outbound.UserSessionRepositoryPort;
import gasi.gps.auth.infrastructure.entity.AppClientEntity;
import gasi.gps.auth.infrastructure.entity.UserDeviceEntity;
import gasi.gps.auth.infrastructure.entity.UserEntity;
import gasi.gps.auth.infrastructure.entity.UserSessionEntity;
import gasi.gps.auth.infrastructure.mapper.UserSessionMapper;
import gasi.gps.auth.infrastructure.persistence.AppClientEntityRepository;
import gasi.gps.auth.infrastructure.persistence.UserEntityRepository;
import gasi.gps.auth.infrastructure.persistence.UserSessionEntityRepository;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.domain.model.AndFilter;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;
import gasi.gps.core.api.infrastructure.specification.GenericSpecification;
import jakarta.persistence.EntityManager;

/**
 * Repository adapter for user login session persistence.
 */
@Component
public class UserSessionRepositoryAdapter
        extends BaseRepositoryAdapter<UserSession, UserSessionEntity>
        implements UserSessionRepositoryPort {

    private final UserSessionMapper userSessionMapper;
    private final UserEntityRepository userEntityRepository;
    private final AppClientEntityRepository appClientEntityRepository;
    private final UserDeviceRepositoryPort userDeviceRepositoryPort;
    private final UserSessionEntityRepository userSessionEntityRepository;
    private final EntityManager entityManager;

    public UserSessionRepositoryAdapter(UserEntityRepository userEntityRepository,
            AppClientEntityRepository appClientEntityRepository,
            UserDeviceRepositoryPort userDeviceRepositoryPort,
            UserSessionEntityRepository userSessionEntityRepository,
            UserSessionMapper userSessionMapper,
            EntityManager entityManager) {
        super(userSessionEntityRepository, userSessionMapper);
        this.userSessionMapper = userSessionMapper;
        this.userEntityRepository = userEntityRepository;
        this.appClientEntityRepository = appClientEntityRepository;
        this.userDeviceRepositoryPort = userDeviceRepositoryPort;
        this.userSessionEntityRepository = userSessionEntityRepository;
        this.entityManager = entityManager;
    }

    @Override
    public UserSession save(UserSession model) {
        Long userId = model.getUser() != null ? model.getUser().getId() : null;
        Long appClientId = model.getAppClient() != null ? model.getAppClient().getId() : null;

        if (userId == null || appClientId == null) {
            throw new BusinessException("User and app client are required");
        }

        UserEntity user = userEntityRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        AppClientEntity appClient = appClientEntityRepository.findById(appClientId)
                .orElseThrow(() -> new BusinessException("App client not found"));

        String deviceId = model.getUserDevice() != null ? model.getUserDevice().getDeviceId() : null;
        String deviceModel = model.getUserDevice() != null ? model.getUserDevice().getDeviceModel() : null;

        UserDevice savedUserDevice = userDeviceRepositoryPort.save(UserDevice.builder()
                .user(model.getUser())
                .appClient(model.getAppClient())
                .deviceId(deviceId)
                .deviceModel(deviceModel)
                .osVersion(model.getUserDevice() != null ? model.getUserDevice().getOsVersion() : null)
                .appVersion(model.getUserDevice() != null ? model.getUserDevice().getAppVersion() : null)
                .trustedExpiresAt(model.getUserDevice() != null ? model.getUserDevice().getTrustedExpiresAt() : null)
                .build());

        GenericFilter userSessionFilter = AndFilter.builder()
                .filters(java.util.List.of(
                        SimpleFilter.builder()
                                .field("user.id")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(userId)
                                .build(),
                        SimpleFilter.builder()
                                .field("appClient.id")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(appClientId)
                                .build()))
                .build();

        UserSessionEntity userSession = userSessionEntityRepository
                .findOne(GenericSpecification.from(userSessionFilter))
                .orElseGet(() -> UserSessionEntity.builder().build());
        userSession.setUser(user);
        userSession.setAppClient(appClient);
        userSession.setUserDevice(entityManager.getReference(UserDeviceEntity.class, savedUserDevice.getId()));
        userSessionMapper.updateEntity(model, userSession);
        userSession
                .setLastActivityAt(model.getLastActivityAt() != null ? model.getLastActivityAt() : model.getIssuedAt());

        UserSessionEntity saved = userSessionEntityRepository.save(userSession);
        return userSessionMapper.toDomain(saved);
    }

    @Override
    protected String resourceType() {
        return "UserSession";
    }
}
