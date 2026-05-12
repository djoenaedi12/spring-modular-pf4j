package {{FULL_PACKAGE}}.application.dto;

{{UPDATE_REQUEST_IMPORTS}}
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {{ENTITY_NAME}}UpdateRequest {

{{UPDATE_REQUEST_FIELDS}}
}
