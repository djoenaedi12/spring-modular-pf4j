package gasi.gps.auth.application.dto;

import gasi.gps.core.api.application.dto.BaseSummaryResponse;
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
public class ActionSummaryResponse extends BaseSummaryResponse {

    private String code;
    private String name;
    private String description;
}
