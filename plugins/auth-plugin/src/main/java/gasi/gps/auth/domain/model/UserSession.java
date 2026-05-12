package gasi.gps.auth.domain.model;

import java.time.Instant;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model for user session records.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSession extends BaseModel {

    private User user;
    private AppClient appClient;
    private UserDevice userDevice;
    private String accessTokenJti;
    private String refreshTokenJti;
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant lastActivityAt;
    private String ipAddress;
    private String userAgent;
}
