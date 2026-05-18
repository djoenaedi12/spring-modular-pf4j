package gasi.gps.storage.infrastructure.entity;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import gasi.gps.core.starter.infrastructure.entity.BaseEntity;
import gasi.gps.core.starter.infrastructure.filter.Filterable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * JPA entity for the {@code storage_providers} table.
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "storage_providers")
@SequenceGenerator(name = "global_seq", sequenceName = "storage_provider_seq", allocationSize = 50)
public class StorageProviderEntity extends BaseEntity {

    @Filterable
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Filterable
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Filterable
    @Column(name = "provider_type", nullable = false, length = 20)
    private String providerType;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> config = new HashMap<>();

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Returns the config map directly for factory usage.
     *
     * @return configuration map
     */
    public Map<String, Object> getConfigAsMap() {
        return config != null ? config : new HashMap<>();
    }
}
