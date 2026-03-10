package gasi.gps.auth.infrastructure.entity;

import java.time.Instant;

import gasi.gps.core.api.infrastructure.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * JPA entity for the user_sessions table.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_sessions")
public class UserSessionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_client_id")
    private AppClientEntity appClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_device_id")
    private UserDeviceEntity userDevice;

    @Column(name = "access_token_jti", nullable = false, length = 100)
    private String accessTokenJti;

    @Column(name = "refresh_token_jti", nullable = false, length = 100)
    private String refreshTokenJti;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;
}
