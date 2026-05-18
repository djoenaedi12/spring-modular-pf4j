package gasi.gps.core.starter.infrastructure.entity;

import gasi.gps.core.api.domain.model.UploadStatus;
import gasi.gps.core.starter.infrastructure.filter.Filterable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "data_upls")
public class DataUplEntity extends BaseEntity {

    @Column(name = "resource", nullable = false, length = 255)
    @Filterable
    private String resource;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Column(name = "valid_rows", nullable = false)
    private int validRows;

    @Column(name = "invalid_rows", nullable = false)
    private int invalidRows;

    @Column(name = "committed_rows", nullable = false)
    private int committedRows;

    @Column(name = "upload_status")
    @Enumerated(EnumType.ORDINAL)
    private UploadStatus uploadStatus;

    @Column(name = "error_message", length = 255)
    private String errorMessage;
}
