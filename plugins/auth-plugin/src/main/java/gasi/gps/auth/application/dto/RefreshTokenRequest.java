package gasi.gps.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for refreshing an access token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank
    private String refreshToken;

    // Populated by Controller
    private String ipAddress;

    // Populated by Controller
    private String userAgent;
}
