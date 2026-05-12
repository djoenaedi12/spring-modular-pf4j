package gasi.gps.auth.application.dto;

import java.time.Instant;
import java.util.Set;

import gasi.gps.core.api.application.dto.BaseDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Detail response DTO for a single user.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDetailResponse extends BaseDetailResponse {

    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String avatarPath;
    private Boolean isEnabled;
    private Instant lastLoginAt;
    private Instant authorizedUntil;
    private Instant passwordChangedAt;
    private Set<String> roleIds;
}
