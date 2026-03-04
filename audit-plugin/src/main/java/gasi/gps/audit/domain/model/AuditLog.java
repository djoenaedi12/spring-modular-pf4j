package gasi.gps.audit.domain.model;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseModel {

    private String traceId;
    private String actorId;
    private String actorIp;
    private String action;
    private String category;
    private String module;
    private String resourceType;
    private String resourceId;
    private String[] fieldsChanged;
    private String description;
    @lombok.Builder.Default
    private String status = "SUCCESS";
}
