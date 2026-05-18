package {{FULL_PACKAGE}}.domain.model;

{{DOMAIN_MODEL_IMPORTS}}
import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class {{ENTITY_NAME}} extends BaseModel {

{{DOMAIN_MODEL_FIELDS}}
}
