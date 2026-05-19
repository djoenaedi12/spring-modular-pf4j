package gasi.gps.core.starter.infrastructure.entity;

import gasi.gps.core.api.domain.model.UploadRowStatus;
import gasi.gps.core.starter.infrastructure.filter.Filterable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
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
@Table(name = "data_row_upls")
@SequenceGenerator(name = "global_seq", sequenceName = "uploader_seq", allocationSize = 50)
public class DataRowUplEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_upl_id", nullable = false)
    @Filterable
    private DataUplEntity dataUpl;
    @Column(name = "row_number", nullable = false)
    private int rowNumber;
    @Lob
    @Column(name = "row_data")
    private String rowData;
    @Filterable
    @Column(name = "lookup_value1", length = 255)
    private String lookupValue1;
    @Filterable
    @Column(name = "lookup_value2", length = 255)
    private String lookupValue2;
    @Filterable
    @Column(name = "lookup_value3", length = 255)
    private String lookupValue3;
    @Column(name = "row_status", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private UploadRowStatus rowStatus;
    @Column(name = "error_message", length = 255)
    private String errorMessage;
}
