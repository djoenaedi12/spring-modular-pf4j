package gasi.gps.core.starter.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.port.inbound.BaseService;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;
import gasi.gps.core.starter.application.mapper.BaseDtoMapper;
import gasi.gps.core.starter.infrastructure.i18n.MessageUtil;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Generic transactional implementation of {@link BaseService}.
 *
 * <p>Subclasses supply concrete repository and mapper implementations, then
 * override validation hooks for resource-specific business rules.</p>
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
        implements BaseService<D, CRQ, URQ, SRS, DRS> {

    /** Repository port used by the service implementation. */
    protected final BaseRepositoryPort<D> repositoryPort;

    /** Mapper used to convert between DTOs and domain models. */
    protected final BaseDtoMapper<D, CRQ, URQ, SRS, DRS> mapper;

    /** Localized message helper for user-facing errors. */
    protected final MessageUtil messageUtil;

    /** Public ID encoder used in responses and error messages. */
    protected final IdEncoder idEncoder;

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
        this.repositoryPort = repositoryPort;
        this.mapper = mapper;
        this.messageUtil = messageUtil;
        this.idEncoder = idEncoder;
    }

    public DRS create(CRQ request) {
        validateCreate(request);
        D domain = mapper.toCreateDomain(request);
        D saved = repositoryPort.save(domain);
        return mapper.toDetail(saved);
    }

    @Transactional(readOnly = true)
    public DRS findById(Long id) {
        D domain = repositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
        return mapper.toDetail(domain);
    }

    @Transactional(readOnly = true)
    public DRS findBy(GenericFilter filter) {
        D domain = repositoryPort.findBy(filter)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), "filter")));
        return mapper.toDetail(domain);
    }

    @Transactional(readOnly = true)
    public List<SRS> findAll(GenericFilter filter, List<SortOrder> orders) {
        List<D> result = repositoryPort.findAll(filter, orders);
        return result.stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResult<SRS> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders) {
        PageResult<D> result = repositoryPort.findAll(page, size, filter, orders);
        List<SRS> content = result.getContent().stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());
        return PageResult.<SRS>builder()
                .content(content)
                .page(result.getPage())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    public DRS update(Long id, URQ request) {
        D existing = repositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
        validateUpdate(id, request);
        mapper.updateDomain(request, existing);
        D saved = repositoryPort.save(existing);
        return mapper.toDetail(saved);
    }

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

    /**
     * Returns a human-readable resource type for error messages.
     *
     * @return resource type name
     */
    protected String resourceType() {
        return "Entity";
    }
}
