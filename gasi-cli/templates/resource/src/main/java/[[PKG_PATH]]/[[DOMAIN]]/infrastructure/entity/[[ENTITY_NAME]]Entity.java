package {{FULL_PACKAGE}}.infrastructure.entity;

{{ENTITY_IMPORTS}}
import gasi.gps.core.starter.infrastructure.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Entity(name = "{{ENTITY_NAME}}")
@Table(name = "{{TABLE_NAME}}")
public class {{ENTITY_NAME}}Entity extends BaseEntity {

{{ENTITY_FIELDS}}
}
