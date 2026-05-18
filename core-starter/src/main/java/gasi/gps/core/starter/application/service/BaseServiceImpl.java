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
 * <p>This implementation builds on {@link BaseReadServiceImpl} for query
 * operations, then adds create, update, and delete behavior for full CRUD
 * resources. Subclasses supply concrete repository and mapper implementations,
 * then override validation hooks for resource-specific business rules.</p>
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
        validateCreate(request);
        D domain = mapper.toCreateDomain(request);
        D saved = repositoryPort.save(domain);
        return mapper.toDetail(saved);
    }

    @Override
    public DRS update(Long id, URQ request) {
        D existing = repositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
        validateUpdate(id, request);
        mapper.updateDomain(request, existing);
        D saved = repositoryPort.save(existing);
        return mapper.toDetail(saved);
    }

    @Override
    public void delete(Long id) {
        repositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
        validateDelete(id);
        repositoryPort.delete(id);
    }

    /**
     * Hook for create request validation.
     *
     * @param request create request DTO
     */
    protected void validateCreate(CRQ request) {
    }

    /**
     * Hook for update request validation.
     *
     * @param id      internal database identifier
     * @param request update request DTO
     */
    protected void validateUpdate(Long id, URQ request) {
    }

    /**
     * Hook for delete validation.
     *
     * @param id internal database identifier
     */
    protected void validateDelete(Long id) {
    }
}
