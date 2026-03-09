package gasi.gps.auth.infrastructure.entity;

import gasi.gps.core.api.infrastructure.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "app_clients")
public class AppClientEntity extends BaseEntity {

    @Column(name = "client_id", nullable = false, length = 50)
    private String clientId;

    @Column(name = "client_secret", nullable = false, length = 255)
    private String clientSecret;

    @Column(name = "grant_types", nullable = false, length = 255)
    private String grantTypes;

    @Column(name = "redirect_uris", length = 255)
    private String redirectUris;

    @Column(name = "access_token_validity", nullable = false)
    private Integer accessTokenValidity;

    @Column(name = "refresh_token_validity", nullable = false)
    private Integer refreshTokenValidity;

    @Column(name = "web_idle_timeout", nullable = true)
    private Integer webIdleTimeout;

    @Column(name = "is_single_session", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isSingleSession;

    @Column(name = "is_single_device", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSingleDevice;

    @Column(name = "scopes", nullable = false, length = 255)
    private String scopes;
}
