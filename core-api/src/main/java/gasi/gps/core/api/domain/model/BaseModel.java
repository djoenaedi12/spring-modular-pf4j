package gasi.gps.core.api.domain.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for domain models shared across modules.
 *
 * <p>
 * This class carries persistence-neutral metadata that mirrors the common
 * entity fields used by the starter module: database ID, audit timestamps,
 * actor metadata, approval source reference, lifecycle status, and optimistic
 * locking version.
 * </p>
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseModel {
    private Long id;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long sourceId;
    private LifecycleStatus lifecycleStatus;
    private Integer version;
}
