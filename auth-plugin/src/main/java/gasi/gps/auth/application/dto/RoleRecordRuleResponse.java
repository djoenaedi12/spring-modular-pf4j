package gasi.gps.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested response DTO for a role record rule within a role detail.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRecordRuleResponse {

    private String recordRuleId;
    private Boolean isNegated;
}
