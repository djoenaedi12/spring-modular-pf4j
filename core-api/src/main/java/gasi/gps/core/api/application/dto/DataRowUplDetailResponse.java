package gasi.gps.core.api.application.dto;

import gasi.gps.core.api.domain.model.UploadRowStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Detail response DTO for a single action.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataRowUplDetailResponse extends BaseDetailResponse {
    private int rowNumber;
    private String rowData;
    private String lookupValue1;
    private String lookupValue2;
    private String lookupValue3;
    private UploadRowStatus rowStatus;
    private String errorMessage;
}
