package {{FULL_PACKAGE}}.domain.port.inbound;

import gasi.gps.core.api.domain.port.inbound.BaseService;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}CreateRequest;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}DetailResponse;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}SummaryResponse;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}UpdateRequest;
import {{FULL_PACKAGE}}.domain.model.{{ENTITY_NAME}};

public interface {{ENTITY_NAME}}Service extends
        BaseService<{{ENTITY_NAME}}, {{ENTITY_NAME}}CreateRequest, {{ENTITY_NAME}}UpdateRequest, {{ENTITY_NAME}}SummaryResponse, {{ENTITY_NAME}}DetailResponse> {
}
