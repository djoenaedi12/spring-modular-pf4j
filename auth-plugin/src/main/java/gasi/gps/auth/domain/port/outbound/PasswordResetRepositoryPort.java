package gasi.gps.auth.domain.port.outbound;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import gasi.gps.auth.domain.model.PasswordReset;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;

/**
 * Outbound port for password reset token persistence.
 */
public interface PasswordResetRepositoryPort extends BaseRepositoryPort<PasswordReset> {

    Optional<PasswordReset> findActiveByTokenHash(String tokenHash, Instant now);

    List<PasswordReset> findActiveByUserId(Long userId, Instant now);

    void sendPasswordReset(String recipient, String resetToken, Instant expiresAt);
}
