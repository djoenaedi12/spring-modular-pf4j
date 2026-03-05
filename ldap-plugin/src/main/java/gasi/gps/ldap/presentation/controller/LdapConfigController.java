package gasi.gps.ldap.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.core.api.presentation.dto.ApiResponse;
import gasi.gps.ldap.application.dto.LdapConfigRequest;
import gasi.gps.ldap.application.dto.LdapConfigResponse;
import gasi.gps.ldap.application.service.LdapConfigService;
import jakarta.validation.Valid;

/**
 * REST controller for LDAP runtime configuration.
 */
@RestController
@RequestMapping("/api/ldap-config")
public class LdapConfigController {

    private final LdapConfigService ldapConfigService;

    /**
     * Constructs LdapConfigController.
     *
     * @param ldapConfigService LDAP config service
     */
    public LdapConfigController(LdapConfigService ldapConfigService) {
        this.ldapConfigService = ldapConfigService;
    }

    /**
     * Returns current LDAP configuration.
     *
     * @return current config
     */
    @GetMapping
    public ApiResponse<LdapConfigResponse> get() {
        return ApiResponse.ok(ldapConfigService.get());
    }

    /**
     * Updates LDAP configuration.
     *
     * @param request config payload
     * @return updated config
     */
    @PutMapping
    public ApiResponse<LdapConfigResponse> update(@Valid @RequestBody LdapConfigRequest request) {
        return ApiResponse.ok(ldapConfigService.update(request));
    }
}
