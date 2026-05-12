package {{FULL_PACKAGE}}.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;
import {{FULL_PACKAGE}}.domain.model.{{ENTITY_NAME}};
import {{FULL_PACKAGE}}.infrastructure.entity.{{ENTITY_NAME}}Entity;

@Mapper
public interface {{ENTITY_NAME}}Mapper extends BaseMapper<{{ENTITY_NAME}}, {{ENTITY_NAME}}Entity> {
}
