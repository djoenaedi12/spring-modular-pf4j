package gasi.gps.core.api.application.dto;

import gasi.gps.core.api.domain.model.UploadStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Summary response DTO for action listing.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataUplSummaryResponse extends BaseSummaryResponse {
    private String fileName;
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private UploadStatus uploadStatus;
}
