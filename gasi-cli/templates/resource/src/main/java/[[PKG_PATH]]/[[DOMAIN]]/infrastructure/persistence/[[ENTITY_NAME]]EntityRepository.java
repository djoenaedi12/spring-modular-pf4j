package {{FULL_PACKAGE}}.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import {{FULL_PACKAGE}}.infrastructure.entity.{{ENTITY_NAME}}Entity;

@Repository
public interface {{ENTITY_NAME}}EntityRepository extends
        JpaRepository<{{ENTITY_NAME}}Entity, Long>,
        JpaSpecificationExecutor<{{ENTITY_NAME}}Entity> {
}
