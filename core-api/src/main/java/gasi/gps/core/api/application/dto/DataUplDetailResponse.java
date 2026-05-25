package gasi.gps.core.api.application.dto;

import gasi.gps.core.api.domain.model.UploadStatus;
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
public class DataUplDetailResponse extends BaseDetailResponse {
    private String instructionNo;
    private String fileName;
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private UploadStatus uploadStatus;
}
