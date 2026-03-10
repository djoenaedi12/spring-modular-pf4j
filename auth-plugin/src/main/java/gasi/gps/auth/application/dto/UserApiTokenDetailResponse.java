package gasi.gps.auth.application.dto;

import java.time.Instant;

import gasi.gps.core.api.application.dto.BaseDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Response for newly-created user API token.
 * Raw token is returned only once at creation time.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserApiTokenDetailResponse extends BaseDetailResponse {

    private String userId;
    private String name;
    private String token;
    private Instant expiresAt;
}
