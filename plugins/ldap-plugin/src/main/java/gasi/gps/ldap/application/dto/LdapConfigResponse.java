package gasi.gps.ldap.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload for LDAP settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LdapConfigResponse {

    private boolean enabled;
    private String ldapUrl;
    private String baseDn;
    private String bindDn;
    private String bindPasswordMasked;
    private String userSearchFilter;
    private int connectTimeoutMs;
    private int readTimeoutMs;
}
