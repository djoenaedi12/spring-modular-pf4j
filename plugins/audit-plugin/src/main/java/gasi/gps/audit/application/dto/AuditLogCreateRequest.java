package gasi.gps.audit.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new audit log entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogCreateRequest {

    private String traceId;

    @NotBlank
    private String actorId;

    @Size(max = 20)
    private String actorIp;

    @NotBlank
    @Size(max = 20)
    private String action;

    @Size(max = 50)
    private String module;

    @Size(max = 150)
    private String resourceType;

    @Size(max = 50)
    private String resourceId;

    private String description;

    @Builder.Default
    private String status = "SUCCESS";
}
