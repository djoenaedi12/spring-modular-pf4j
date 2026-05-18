package gasi.gps.storage.infrastructure.entity;

import gasi.gps.core.starter.infrastructure.entity.BaseEntity;
import gasi.gps.core.starter.infrastructure.filter.Filterable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * JPA entity for the {@code storage_provider_mappings} table.
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "storage_provider_mappings")
@SequenceGenerator(name = "global_seq", sequenceName = "storage_provider_mapping_seq", allocationSize = 50)
public class StorageProviderMappingEntity extends BaseEntity {

    @Filterable
    @Column(name = "resource", nullable = false, unique = true, length = 50)
    private String resource;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;
}
