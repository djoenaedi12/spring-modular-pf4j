package gasi.gps.audit.infrastructure.entity;

import gasi.gps.core.api.infrastructure.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "audit_logs")
@SequenceGenerator(name = "global_seq", sequenceName = "audit_log_seq", allocationSize = 50)
public class AuditLogEntity extends BaseEntity {

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "actor_id", length = 50)
    private String actorId;

    @Column(name = "actor_ip", length = 20)
    private String actorIp;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "resource_type", length = 150)
    private String resourceType;

    @Column(name = "resource_id", length = 50)
    private String resourceId;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @lombok.Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "SUCCESS";
}
