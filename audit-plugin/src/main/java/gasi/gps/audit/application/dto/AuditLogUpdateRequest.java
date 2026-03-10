package gasi.gps.audit.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing audit log entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogUpdateRequest {

    private String traceId;

    private String actorId;

    @Size(max = 20)
    private String actorIp;

    @Size(max = 20)
    private String action;

    @Size(max = 50)
    private String module;

    @Size(max = 150)
    private String resourceType;

    @Size(max = 50)
    private String resourceId;

    private String description;

    @Size(max = 20)
    private String status;
}
