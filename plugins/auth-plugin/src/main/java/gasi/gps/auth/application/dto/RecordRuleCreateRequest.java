package gasi.gps.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new record rule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordRuleCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String description;

    @NotBlank
    private String resourceId;

    @NotBlank
    private String conditionExpression;

    @NotBlank
    @Size(max = 255)
    private String uri;
}
