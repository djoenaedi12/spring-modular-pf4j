package gasi.gps.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing app client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppClientUpdateRequest {

    @NotBlank
    @Size(max = 50)
    private String clientId;

    @NotBlank
    @Size(max = 255)
    private String clientSecret;

    @NotEmpty
    private String[] grantTypes;

    @Size(max = 255)
    private String redirectUris;

    @NotNull
    private Integer accessTokenValidity;

    @NotNull
    private Integer refreshTokenValidity;

    @NotEmpty
    private String[] scopes;
}
