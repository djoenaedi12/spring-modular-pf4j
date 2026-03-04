package gasi.gps.core.api.application.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base summary response DTO for list/pagination views.
 * Contains only essential identification and timestamp fields.
 *
 * @param <ID> the type of the entity identifier
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseSummaryResponse {

    private String id;
    private Instant createdAt;
}
