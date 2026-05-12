package {{FULL_PACKAGE}}.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.core.starter.infrastructure.adapter.BaseRepositoryAdapter;
import {{FULL_PACKAGE}}.domain.model.{{ENTITY_NAME}};
import {{FULL_PACKAGE}}.domain.port.outbound.{{ENTITY_NAME}}RepositoryPort;
import {{FULL_PACKAGE}}.infrastructure.entity.{{ENTITY_NAME}}Entity;
import {{FULL_PACKAGE}}.infrastructure.mapper.{{ENTITY_NAME}}Mapper;
import {{FULL_PACKAGE}}.infrastructure.persistence.{{ENTITY_NAME}}EntityRepository;

@Component
public class {{ENTITY_NAME}}RepositoryAdapter
        extends BaseRepositoryAdapter<{{ENTITY_NAME}}, {{ENTITY_NAME}}Entity>
        implements {{ENTITY_NAME}}RepositoryPort {

    public {{ENTITY_NAME}}RepositoryAdapter({{ENTITY_NAME}}EntityRepository repository, {{ENTITY_NAME}}Mapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "{{ENTITY_NAME}}";
    }
}
