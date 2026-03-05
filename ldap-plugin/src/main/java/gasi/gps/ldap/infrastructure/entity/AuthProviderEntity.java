package gasi.gps.ldap.infrastructure.entity;

import gasi.gps.core.api.infrastructure.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Generic authentication provider configuration entity.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "auth_providers")
public class AuthProviderEntity extends BaseEntity {

    @Column(name = "provider_id", nullable = false, unique = true, length = 100)
    private String providerId;

    @Column(name = "provider_type", nullable = false, length = 50)
    private String providerType;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "settings_json", nullable = false, columnDefinition = "TEXT")
    private String settingsJson;
}
