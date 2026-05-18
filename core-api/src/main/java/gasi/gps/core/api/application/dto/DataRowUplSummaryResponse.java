package gasi.gps.core.api.application.dto;

import gasi.gps.core.api.domain.model.UploadRowStatus;
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
public class DataRowUplSummaryResponse extends BaseSummaryResponse {
    private int rowNumber;
    private UploadRowStatus rowStatus;
}
