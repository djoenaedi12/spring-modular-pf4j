package gasi.gps.core.api.application.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base response DTO for list and pagination views.
 *
 * <p>Summary responses intentionally expose only the public encoded identifier
 * and creation timestamp shared by all resources.</p>
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseSummaryResponse {

    private String id;
    private Instant createdAt;
}
