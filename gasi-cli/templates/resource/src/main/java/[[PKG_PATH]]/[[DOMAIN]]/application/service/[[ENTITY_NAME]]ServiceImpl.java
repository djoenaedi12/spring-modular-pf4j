package {{FULL_PACKAGE}}.application.service;

{{SERVICE_IMPORTS}}
import gasi.gps.core.starter.application.service.BaseServiceImpl;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}CreateRequest;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}DetailResponse;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}SummaryResponse;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}UpdateRequest;
import {{FULL_PACKAGE}}.application.mapper.{{ENTITY_NAME}}DtoMapper;
import {{FULL_PACKAGE}}.domain.model.{{ENTITY_NAME}};
import {{FULL_PACKAGE}}.domain.port.inbound.{{ENTITY_NAME}}Service;
import {{FULL_PACKAGE}}.domain.port.outbound.{{ENTITY_NAME}}RepositoryPort;

@Service
public class {{ENTITY_NAME}}ServiceImpl
        extends BaseServiceImpl<{{ENTITY_NAME}}, {{ENTITY_NAME}}CreateRequest, {{ENTITY_NAME}}UpdateRequest, {{ENTITY_NAME}}SummaryResponse, {{ENTITY_NAME}}DetailResponse>
        implements {{ENTITY_NAME}}Service {

{{SERVICE_FIELDS}}
    public {{ENTITY_NAME}}ServiceImpl({{SERVICE_CONSTRUCTOR_PARAMS}}) {
        super(repositoryPort, dtoMapper, messageUtil, idEncoder);
{{SERVICE_CONSTRUCTOR_ASSIGNMENTS}}
    }

    @Override
    protected String resourceType() {
        return "{{ENTITY_NAME}}";
    }

{{SERVICE_REFERENCE_METHODS}}
}
