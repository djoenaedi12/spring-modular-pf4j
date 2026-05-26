package gasi.gps.dataupload.infrastructure.entity;

import gasi.gps.core.starter.infrastructure.entity.BaseEntity;
import gasi.gps.core.starter.infrastructure.filter.Filterable;
import gasi.gps.dataupload.domain.model.UploadRowStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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
public class DataRowUplEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_upl_id", nullable = false)
    @Filterable
    private DataUplEntity dataUpl;
    @Filterable
    @Column(name = "row_number", nullable = false)
    private int rowNumber;
    @Lob
    @Column(name = "row_data")
    private String rowData;
    @Column(name = "row_status", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @Filterable
    private UploadRowStatus rowStatus;
    @Filterable
    @Column(name = "identifier", length = 255)
    private String identifier;
    @Filterable
    @Column(name = "lookup_value1", length = 255)
    private String lookupValue1;
    @Filterable
    @Column(name = "lookup_value2", length = 255)
    private String lookupValue2;
    @Filterable
    @Column(name = "lookup_value3", length = 255)
    private String lookupValue3;
    @Lob
    @Column(name = "error_message")
    private String errorMessage;
}
