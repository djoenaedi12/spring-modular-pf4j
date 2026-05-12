package gasi.gps.auth.domain.model;

import java.time.Instant;
import java.util.Set;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model representing a user.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseModel {

    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private String avatarPath;
    private Boolean isEnabled;
    private Instant lastLoginAt;
    private Integer failedLoginCount;
    private Instant lockedUntil;
    private Instant authorizedUntil;
    private Instant passwordChangedAt;
    private Set<Role> roles;
}
