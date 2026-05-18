package {{FULL_PACKAGE}}.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.core.starter.presentation.controller.BaseController;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}CreateRequest;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}DetailResponse;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}SummaryResponse;
import {{FULL_PACKAGE}}.application.dto.{{ENTITY_NAME}}UpdateRequest;
import {{FULL_PACKAGE}}.domain.model.{{ENTITY_NAME}};
import {{FULL_PACKAGE}}.domain.port.inbound.{{ENTITY_NAME}}Service;

@RestController
@RequestMapping("/api/v1/{{API_PATH}}")
public class {{ENTITY_NAME}}Controller
        extends BaseController<{{ENTITY_NAME}}, {{ENTITY_NAME}}CreateRequest, {{ENTITY_NAME}}UpdateRequest, {{ENTITY_NAME}}SummaryResponse, {{ENTITY_NAME}}DetailResponse> {

    public {{ENTITY_NAME}}Controller({{ENTITY_NAME}}Service {{ENTITY_VAR}}Service, IdEncoder idEncoder) {
        super({{ENTITY_VAR}}Service, idEncoder);
    }

    @Override
    public String getResourceName() {
        return "{{ENTITY_NAME}}";
    }
}
