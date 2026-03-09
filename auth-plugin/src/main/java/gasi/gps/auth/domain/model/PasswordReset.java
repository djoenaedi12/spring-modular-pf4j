package gasi.gps.auth.domain.model;

import java.time.Instant;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model representing a password reset token lifecycle.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PasswordReset extends BaseModel {

    private Long userId;
    private String resetTokenHash;
    private Instant expiresAt;
    private Instant usedAt;
}
