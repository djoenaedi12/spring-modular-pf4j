package gasi.gps.auth.application.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a user API token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserApiTokenCreateRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String name;

    private Instant expiresAt;

    private Boolean noExpiry;
}
