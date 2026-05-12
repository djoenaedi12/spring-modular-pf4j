package {{FULL_PACKAGE}}.application.mapper;

import org.mapstruct.Mapper;
{{DTO_MAPPER_EXTRA_IMPORTS}}
import org.mapstruct.MappingTarget;

import gasi.gps.core.starter.application.mapper.BaseDtoMapper;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}CreateRequest;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}DetailResponse;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}SummaryResponse;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}UpdateRequest;
import {{FULL_PACKAGE}}.domain.model.{{ENTITY_NAME}};

@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public interface {{ENTITY_NAME}}DtoMapper extends BaseDtoMapper<{{ENTITY_NAME}}, {{ENTITY_NAME}}CreateRequest, {{ENTITY_NAME}}UpdateRequest, {{ENTITY_NAME}}SummaryResponse, {{ENTITY_NAME}}DetailResponse> {

{{DTO_MAPPER_TO_MODEL_MAPPINGS}}
    @Override
    {{ENTITY_NAME}} toModel({{ENTITY_NAME}}CreateRequest request);

{{DTO_MAPPER_UPDATE_MODEL_MAPPINGS}}
    @Override
    void updateModel({{ENTITY_NAME}}UpdateRequest request, @MappingTarget {{ENTITY_NAME}} model);

{{DTO_MAPPER_SUMMARY_MAPPINGS}}
    @Override
    {{ENTITY_NAME}}SummaryResponse toSummaryResponse({{ENTITY_NAME}} model);

{{DTO_MAPPER_DETAIL_MAPPINGS}}
    @Override
    {{ENTITY_NAME}}DetailResponse toDetailResponse({{ENTITY_NAME}} model);
}
