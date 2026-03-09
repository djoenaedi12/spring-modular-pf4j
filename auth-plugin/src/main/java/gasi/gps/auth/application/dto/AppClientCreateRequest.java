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
 * DTO for creating a new app client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppClientCreateRequest {

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

    private Integer webIdleTimeout;

    @Builder.Default
    private Boolean isSingleSession = true;

    @Builder.Default
    private Boolean isSingleDevice = false;

    @NotEmpty
    private String[] scopes;
}
