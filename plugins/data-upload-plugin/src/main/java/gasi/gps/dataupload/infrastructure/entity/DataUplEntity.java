package gasi.gps.dataupload.infrastructure.entity;

import gasi.gps.core.starter.infrastructure.entity.BaseEntity;
import gasi.gps.core.starter.infrastructure.filter.Filterable;
import gasi.gps.dataupload.domain.model.UploadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
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

    @Column(name = "instruction_no", nullable = false, length = 255)
    @Filterable
    private String instructionNo;

    @Column(name = "file_name", nullable = false, length = 255)
    @Filterable
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
    @Filterable
    private UploadStatus uploadStatus;

    @Lob
    @Column(name = "error_message")
    private String errorMessage;
}
