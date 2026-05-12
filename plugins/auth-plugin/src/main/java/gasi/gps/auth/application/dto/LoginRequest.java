package gasi.gps.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank
    private String grantType;

    private String provider;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    // Populated by Controller
    private String deviceId;

    // Populated by Controller
    private String deviceModel;

    // Populated by Controller
    private String ipAddress;

    // Populated by Controller
    private String userAgent;
}
