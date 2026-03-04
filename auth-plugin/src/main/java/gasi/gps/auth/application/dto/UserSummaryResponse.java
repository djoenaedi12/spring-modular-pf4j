package gasi.gps.auth.application.dto;

import gasi.gps.core.api.application.dto.BaseSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Summary response DTO for user listing.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSummaryResponse extends BaseSummaryResponse {

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isEnabled;
}
