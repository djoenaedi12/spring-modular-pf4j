package gasi.gps.auth.infrastructure.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import gasi.gps.auth.domain.model.PasswordReset;
import gasi.gps.auth.domain.port.outbound.PasswordResetRepositoryPort;
import gasi.gps.auth.infrastructure.entity.PasswordResetEntity;
import gasi.gps.auth.infrastructure.mapper.PasswordResetMapper;
import gasi.gps.auth.infrastructure.persistence.PasswordResetEntityRepository;
import gasi.gps.core.api.domain.model.AndFilter;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Repository adapter for password reset tokens.
 */
@Component
@Slf4j
public class PasswordResetRepositoryAdapter
        extends BaseRepositoryAdapter<PasswordReset, PasswordResetEntity>
        implements PasswordResetRepositoryPort {

    public PasswordResetRepositoryAdapter(PasswordResetEntityRepository repository,
            PasswordResetMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public Optional<PasswordReset> findActiveByTokenHash(String tokenHash, Instant now) {
        GenericFilter activeTokenFilter = AndFilter.builder()
                .filters(List.of(
                        SimpleFilter.builder()
                                .field("resetTokenHash")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(tokenHash)
                                .build(),
                        SimpleFilter.builder()
                                .field("usedAt")
                                .operator(SimpleFilter.FilterOperator.IS_NULL)
                                .build(),
                        SimpleFilter.builder()
                                .field("expiresAt")
                                .operator(SimpleFilter.FilterOperator.GREATER_THAN)
                                .value(now)
                                .build()))
                .build();

        List<PasswordReset> results = findAll(activeTokenFilter, List.of(SortOrder.desc("createdAt")), false);
        return results.stream().findFirst();
    }

    @Override
    public List<PasswordReset> findActiveByUserId(Long userId, Instant now) {
        GenericFilter activeUserFilter = AndFilter.builder()
                .filters(List.of(
                        SimpleFilter.builder()
                                .field("user.id")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(userId)
                                .build(),
                        SimpleFilter.builder()
                                .field("usedAt")
                                .operator(SimpleFilter.FilterOperator.IS_NULL)
                                .build(),
                        SimpleFilter.builder()
                                .field("expiresAt")
                                .operator(SimpleFilter.FilterOperator.GREATER_THAN)
                                .value(now)
                                .build()))
                .build();
        return findAll(activeUserFilter, List.of(), false);
    }

    @Override
    public void sendPasswordReset(String recipient, String resetToken, Instant expiresAt) {
        log.info("Password reset token generated for recipient={} expiresAt={} token={}",
                recipient,
                expiresAt,
                resetToken);
    }

    @Override
    protected String resourceType() {
        return "PasswordReset";
    }
}
