package {{FULL_PACKAGE}}.application.dto;

{{CREATE_REQUEST_IMPORTS}}
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {{ENTITY_NAME}}CreateRequest {

{{CREATE_REQUEST_FIELDS}}
}
