package gasi.gps.core.starter.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.port.inbound.BaseReadService;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;
import gasi.gps.core.starter.application.mapper.BaseReadDtoMapper;
import gasi.gps.core.starter.infrastructure.i18n.MessageUtil;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Generic transactional implementation of {@link BaseReadService}.
 *
 * <p>
 * Subclasses supply concrete repository and read mapper implementations,
 * then override {@link #resourceType()} for resource-specific error
 * messages.
 * </p>
 *
 * @param <D>   domain model type
 * @param <SRS> summary response DTO type
 * @param <DRS> detail response DTO type
 * @since 1.0.0
 */
@Transactional(readOnly = true)
public abstract class BaseReadServiceImpl<D extends BaseModel, SRS, DRS>
        implements BaseReadService<SRS, DRS> {

    /** Repository port used by the service implementation. */
    protected final BaseRepositoryPort<D> repositoryPort;

    /** Mapper used to convert domain models into response DTOs. */
    protected final BaseReadDtoMapper<D, SRS, DRS> mapper;

    /** Localized message helper for user-facing errors. */
    protected final MessageUtil messageUtil;

    /** Public ID encoder used in responses and error messages. */
    protected final IdEncoder idEncoder;

    /**
     * Creates a base read service implementation.
     *
     * @param repositoryPort repository port for domain persistence
     * @param mapper         mapper from domain models to response DTOs
     * @param messageUtil    localized message helper
     * @param idEncoder      public ID encoder
     */
    protected BaseReadServiceImpl(BaseRepositoryPort<D> repositoryPort,
            BaseReadDtoMapper<D, SRS, DRS> mapper,
            MessageUtil messageUtil,
            IdEncoder idEncoder) {
        this.repositoryPort = repositoryPort;
        this.mapper = mapper;
        this.messageUtil = messageUtil;
        this.idEncoder = idEncoder;
    }

    @Override
    public DRS findById(Long id) {
        D domain = repositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
        return enrichDetail(mapper.toDetail(domain));
    }

    @Override
    public DRS findBy(GenericFilter filter) {
        D domain = repositoryPort.findBy(filter)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), "filter")));
        return enrichDetail(mapper.toDetail(domain));
    }

    @Override
    public List<SRS> findAll(GenericFilter filter, List<SortOrder> orders) {
        List<D> result = repositoryPort.findAll(filter, orders);
        return result.stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
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

    /**
     * Returns a human-readable resource type for error messages.
     *
     * @return resource type name
     */
    protected String resourceType() {
        return "Entity";
    }

    /**
     * Enrich a detail response with additional data (child lists, computed fields).
     * Called on create, update, and findById.
     *
     * @param response the detail response to enrich
     * @return the enriched response
     */
    protected DRS enrichDetail(DRS response) {
        return response;
    }

    /**
     * Convert saved domain to detail response, then enrich.
     *
     * @param saved the persisted domain object
     * @return the enriched detail response
     */
    protected DRS toDetailResponse(D saved) {
        DRS response = mapper.toDetail(saved);
        return enrichDetail(response);
    }
}
