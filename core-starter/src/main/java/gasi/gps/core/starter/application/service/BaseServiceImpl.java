package gasi.gps.core.starter.application.service;

import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.port.inbound.BaseService;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;
import gasi.gps.core.starter.application.mapper.BaseDtoMapper;
import gasi.gps.core.starter.infrastructure.i18n.MessageUtil;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Generic transactional implementation of {@link BaseService}.
 *
 * <p>
 * This implementation builds on {@link BaseReadServiceImpl} for query
 * operations, then adds create, update, and delete behavior for full CRUD
 * resources. Subclasses supply concrete repository and mapper implementations,
 * then override validation hooks for resource-specific business rules.
 * </p>
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO
 * @param <URQ> update request DTO
 * @param <SRS> summary response DTO (for lists)
 * @param <DRS> detail response DTO (for single entity)
 * @since 1.0.0
 */
@Transactional
public abstract class BaseServiceImpl<D extends BaseModel, CRQ, URQ, SRS, DRS>
        extends BaseReadServiceImpl<D, SRS, DRS>
        implements BaseService<D, CRQ, URQ, SRS, DRS> {

    /** Mapper used for write-side DTO conversion and inherited read mappings. */
    protected final BaseDtoMapper<D, CRQ, URQ, SRS, DRS> mapper;

    /**
     * Creates a base service implementation.
     *
     * @param repositoryPort repository port for domain persistence
     * @param mapper         mapper between request/response DTOs and domain models
     * @param messageUtil    localized message helper
     * @param idEncoder      public ID encoder
     */
    protected BaseServiceImpl(BaseRepositoryPort<D> repositoryPort,
            BaseDtoMapper<D, CRQ, URQ, SRS, DRS> mapper,
            MessageUtil messageUtil,
            IdEncoder idEncoder) {
        super(repositoryPort, mapper, messageUtil, idEncoder);
        this.mapper = mapper;
    }

    @Override
    public DRS create(CRQ request) {
        D domain = mapper.toCreateDomain(request);
        beforeCreate(domain, request);
        D saved = repositoryPort.save(domain);
        afterCreate(saved, request);
        return toDetailResponse(saved);
    }

    @Override
    public DRS update(Long id, URQ request) {
        D existing = repositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
        mapper.updateDomain(request, existing);
        beforeUpdate(existing, request);
        D saved = repositoryPort.save(existing);
        afterUpdate(saved, request);
        return toDetailResponse(saved);
    }

    @Override
    public void delete(Long id) {
        repositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
        beforeDelete(id);
        repositoryPort.delete(id);
    }

    /**
     * Called after create DTO is mapped to domain, before save.
     * Use this to resolve and validate references, then apply them.
     *
     * @param domain  the newly mapped domain object
     * @param request the original create request
     */
    protected void beforeCreate(D domain, CRQ request) {
    }

    /**
     * Called after the entity is saved on create.
     * Use this to save child collections.
     *
     * @param saved   the persisted domain object
     * @param request the original create request
     */
    protected void afterCreate(D saved, CRQ request) {
    }

    /**
     * Called after update DTO is merged into existing domain, before save.
     * Use this to resolve and validate references, then apply them.
     *
     * @param domain  the existing domain object with updates applied
     * @param request the original update request
     */
    protected void beforeUpdate(D domain, URQ request) {
    }

    /**
     * Called after the entity is saved on update.
     * Use this to delete and re-save child collections.
     *
     * @param saved   the persisted domain object
     * @param request the original update request
     */
    protected void afterUpdate(D saved, URQ request) {
    }

    /**
     * Called before the entity is deleted.
     * Use this to cascade-delete child entities.
     *
     * @param id internal database identifier
     */
    protected void beforeDelete(Long id) {
    }
}
