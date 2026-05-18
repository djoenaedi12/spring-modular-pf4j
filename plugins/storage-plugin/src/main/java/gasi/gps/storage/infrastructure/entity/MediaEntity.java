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
 * JPA entity for the {@code storage_medias} table.
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "storage_medias")
@SequenceGenerator(name = "global_seq", sequenceName = "storage_media_seq", allocationSize = 50)
public class MediaEntity extends BaseEntity {

    @Filterable
    @Column(name = "file_key", unique = true, nullable = false, length = 64)
    private String fileKey;

    @Filterable
    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Filterable
    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private long fileSize;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Filterable
    @Column(name = "resource", nullable = false, length = 50)
    private String resource;

    @Filterable
    @Column(name = "resource_id")
    private Long resourceId;
}
