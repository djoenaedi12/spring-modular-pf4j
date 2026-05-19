package gasi.gps.core.api.application.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base response DTO for single-resource detail views.
 *
 * <p>Extends {@link BaseSummaryResponse} with audit metadata and optimistic
 * locking version. The public {@code id} remains encoded in the superclass.</p>
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class BaseDetailResponse extends BaseSummaryResponse {

    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Integer version;
}
