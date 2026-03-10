package gasi.gps.auth.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.auth.domain.model.UserSession;
import gasi.gps.auth.domain.port.inbound.UserSessionService;
import gasi.gps.auth.domain.port.outbound.UserSessionRepositoryPort;
import gasi.gps.core.api.domain.model.AndFilter;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;

/**
 * Validates access tokens against persisted sessions with Caffeine caching.
 */
@Service
public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionRepositoryPort userSessionRepositoryPort;

    public UserSessionServiceImpl(UserSessionRepositoryPort userSessionRepositoryPort) {
        this.userSessionRepositoryPort = userSessionRepositoryPort;
    }

    @Override
    @Cacheable(value = "activeTokens", key = "#jti", unless = "#result == false")
    @Transactional(readOnly = true)
    public boolean validateAccessToken(String jti) {
        GenericFilter jtiFilter = AndFilter.builder()
                .filters(List.of(
                        SimpleFilter.builder()
                                .field("accessTokenJti")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(jti)
                                .build(),
                        SimpleFilter.builder()
                                .field("expiresAt")
                                .operator(SimpleFilter.FilterOperator.GREATER_THAN)
                                .value(Instant.now())
                                .build()))
                .build();
        return userSessionRepositoryPort.findBy(jtiFilter)
                .isPresent();
    }

    @Override
    @CacheEvict(value = "activeTokens", key = "#jti")
    @Transactional
    public void revokeAccessToken(String jti) {
        SimpleFilter jtiFilter = SimpleFilter.builder()
                .field("accessTokenJti")
                .operator(SimpleFilter.FilterOperator.EQUALS)
                .value(jti)
                .build();
        userSessionRepositoryPort.deleteAllBy(jtiFilter);
    }

    @Override
    @CachePut(value = "activeTokens", key = "#jti")
    @Transactional
    public boolean save(UserSession userSession, String jti) {
        userSessionRepositoryPort.save(userSession);
        return true;
    }
}
