package gasi.gps.ldap.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for updating LDAP settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LdapConfigRequest {

    @Builder.Default
    private boolean enabled = false;

    @NotBlank
    private String ldapUrl;

    @NotBlank
    private String baseDn;

    @NotBlank
    private String bindDn;

    @NotBlank
    private String bindPassword;

    @NotBlank
    private String userSearchFilter;

    @Min(100)
    @Max(60000)
    @Builder.Default
    private int connectTimeoutMs = 5000;

    @Min(100)
    @Max(60000)
    @Builder.Default
    private int readTimeoutMs = 5000;
}
