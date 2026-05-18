package {{FULL_PACKAGE}}.application.mapper;

import org.mapstruct.Mapper;
{{DTO_MAPPER_EXTRA_IMPORTS}}
import org.mapstruct.MappingTarget;
{{DTO_MAPPER_AUTOWIRED_IMPORT}}

import gasi.gps.core.starter.application.mapper.BaseDtoMapper;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}CreateRequest;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}DetailResponse;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}SummaryResponse;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}UpdateRequest;
import {{FULL_PACKAGE}}.domain.model.{{ENTITY_NAME}};
{{DTO_MAPPER_CHILD_IMPORTS}}

@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public abstract class {{ENTITY_NAME}}DtoMapper implements BaseDtoMapper<{{ENTITY_NAME}}, {{ENTITY_NAME}}CreateRequest, {{ENTITY_NAME}}UpdateRequest, {{ENTITY_NAME}}SummaryResponse, {{ENTITY_NAME}}DetailResponse> {

{{DTO_MAPPER_ID_ENCODER_FIELD}}
{{DTO_MAPPER_TO_MODEL_MAPPINGS}}
    @Override
    public abstract {{ENTITY_NAME}} toCreateDomain({{ENTITY_NAME}}CreateRequest request);

{{DTO_MAPPER_UPDATE_MODEL_MAPPINGS}}
    @Override
    public abstract void updateDomain({{ENTITY_NAME}}UpdateRequest request, @MappingTarget {{ENTITY_NAME}} model);

{{DTO_MAPPER_SUMMARY_MAPPINGS}}
    @Override
    public abstract {{ENTITY_NAME}}SummaryResponse toSummary({{ENTITY_NAME}} model);

{{DTO_MAPPER_DETAIL_MAPPINGS}}
    @Override
    public abstract {{ENTITY_NAME}}DetailResponse toDetail({{ENTITY_NAME}} model);

{{DTO_MAPPER_CHILD_METHODS}}
}
