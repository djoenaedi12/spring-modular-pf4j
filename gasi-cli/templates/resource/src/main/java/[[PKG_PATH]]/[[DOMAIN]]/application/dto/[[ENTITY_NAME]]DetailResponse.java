package {{FULL_PACKAGE}}.application.dto;

{{DETAIL_RESPONSE_IMPORTS}}
import gasi.gps.core.api.application.dto.BaseDetailResponse;
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
public class {{ENTITY_NAME}}DetailResponse extends BaseDetailResponse {

{{DETAIL_RESPONSE_FIELDS}}
}
