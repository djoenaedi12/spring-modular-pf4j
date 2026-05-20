package gasi.gps.core.starter.infrastructure.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import gasi.gps.core.api.domain.model.LifecycleStatus;
import gasi.gps.core.starter.infrastructure.filter.Filterable;

/**
 * Base JPA entity with auto-generated Long ID, audit fields, and optimistic
 * locking.
 */
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SuppressWarnings("checkstyle:MemberName")
public abstract class BaseEntity {

    /**
     * Creates an empty base entity for JPA.
     */
    protected BaseEntity() {
    }

    @Id
    @Filterable
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "source_id")
    private Long sourceId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "lifecycle_status")
    private LifecycleStatus lifecycleStatus;

    @Version
    @Column(name = "version")
    private Integer version;
}
