package gasi.gps.core.api.application.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base detail response DTO for single-entity views.
 * Contains all audit fields matching
 * {@link gasi.gps.core.api.domain.model.BaseModel}.
 *
 * @param <ID> the type of the entity identifier
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
    private Long version;
}
