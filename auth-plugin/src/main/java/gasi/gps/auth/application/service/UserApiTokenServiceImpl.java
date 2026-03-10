package gasi.gps.auth.application.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.auth.application.dto.UserApiTokenCreateRequest;
import gasi.gps.auth.application.dto.UserApiTokenDetailResponse;
import gasi.gps.auth.application.dto.UserApiTokenSummaryResponse;
import gasi.gps.auth.application.mapper.UserApiTokenDtoMapper;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.domain.model.User;
import gasi.gps.auth.domain.model.UserApiToken;
import gasi.gps.auth.domain.port.inbound.UserApiTokenService;
import gasi.gps.auth.domain.port.outbound.UserApiTokenRepositoryPort;
import gasi.gps.auth.domain.port.outbound.UserRepositoryPort;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SimpleFilter.FilterOperator;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;
import gasi.gps.core.api.infrastructure.security.AuthenticatedPrincipal;
import gasi.gps.core.api.infrastructure.util.HashUtil;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

/**
 * Application service for user API token lifecycle.
 */
@Service
@Transactional
public class UserApiTokenServiceImpl extends
        BaseServiceImpl<UserApiToken, UserApiTokenCreateRequest, UserApiTokenCreateRequest, UserApiTokenSummaryResponse, UserApiTokenDetailResponse>
        implements UserApiTokenService {

    private final UserApiTokenRepositoryPort repositoryPort;
    private final UserApiTokenDtoMapper mapper;
    private final UserRepositoryPort userRepositoryPort;

    public UserApiTokenServiceImpl(UserApiTokenRepositoryPort repositoryPort,
            UserApiTokenDtoMapper mapper, MessageUtil messageUtil,
            IdEncoder idEncoder, UserRepositoryPort userRepositoryPort) {
        super(repositoryPort, mapper, messageUtil, idEncoder);
        this.userRepositoryPort = userRepositoryPort;
        this.repositoryPort = repositoryPort;
        this.mapper = mapper;
    }

    @Override
    @Cacheable(value = "userApiTokens", key = "#rawToken", unless = "#result == null || !#result.isPresent()")
    @Transactional(readOnly = true)
    public Optional<AuthenticatedPrincipal> authenticate(String rawToken) {
        String tokenHash = HashUtil.sha256Base64(rawToken);
        GenericFilter tokenFilter = SimpleFilter.builder().field("tokenHash").operator(FilterOperator.EQUALS)
                .value(tokenHash).build();
        return repositoryPort.findBy(tokenFilter)
                .map(token -> new AuthenticatedPrincipal(
                        "local",
                        "",
                        token.getUser().getUsername(),
                        token.getUser().getRoles().stream().map(Role::getCode).collect(Collectors.toSet()),
                        Map.of()));
    }

    @Override
    public UserApiTokenDetailResponse create(UserApiTokenCreateRequest request) {
        Long userId = idEncoder.decode(request.getUserId());
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException("Invalid userId: " + request.getUserId()));

        String token = UUID.randomUUID().toString();
        String tokenHash = HashUtil.sha256Base64(token);

        UserApiToken domain = mapper.toCreateDomain(request);
        domain.setUser(user);
        domain.setTokenHash(tokenHash);
        UserApiToken saved = repositoryPort.save(domain);
        UserApiTokenDetailResponse response = mapper.toDetail(saved);
        response.setToken(token);
        return response;
    }
}
