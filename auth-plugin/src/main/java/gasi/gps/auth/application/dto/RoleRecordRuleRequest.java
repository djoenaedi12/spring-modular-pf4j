package gasi.gps.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested request DTO for creating/updating a role record rule within a role.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRecordRuleRequest {

    @NotBlank
    private String recordRuleId;
    @Builder.Default
    private Boolean isNegated = false;
}
