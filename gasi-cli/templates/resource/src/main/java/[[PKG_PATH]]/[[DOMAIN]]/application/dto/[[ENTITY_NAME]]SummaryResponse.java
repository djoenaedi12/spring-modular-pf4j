package {{FULL_PACKAGE}}.application.dto;

{{SUMMARY_RESPONSE_IMPORTS}}
import gasi.gps.core.api.application.dto.BaseSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class {{ENTITY_NAME}}SummaryResponse extends BaseSummaryResponse {

{{SUMMARY_RESPONSE_FIELDS}}
}
