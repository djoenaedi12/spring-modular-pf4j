package gasi.gps.auth.application.dto;

import gasi.gps.core.api.application.dto.BaseDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Detail response DTO for a single app client.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppClientDetailResponse extends BaseDetailResponse {

    private String clientId;
    private String[] grantTypes;
    private String redirectUris;
    private Integer accessTokenValidity;
    private Integer refreshTokenValidity;
    private Integer webIdleTimeout;
    private Boolean isSingleSession;
    private Boolean isSingleDevice;
    private String[] scopes;
}
