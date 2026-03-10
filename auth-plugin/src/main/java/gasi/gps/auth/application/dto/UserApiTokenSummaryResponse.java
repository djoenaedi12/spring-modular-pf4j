package gasi.gps.auth.application.dto;

import java.time.Instant;

import gasi.gps.core.api.application.dto.BaseSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Summary response DTO for user API token listing.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserApiTokenSummaryResponse extends BaseSummaryResponse {

    private String userId;
    private String name;
    private Instant expiresAt;
}
