package gasi.gps.core.api.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.port.inbound.BaseService;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

/**
 * Generic base service with CRUD, filtering, sorting, and pagination.
 * Record rules (horizontal access control) are enforced at the repository
 * layer.
 *
 * @param <D>   domain model type
 * @param <ID>  identifier type
 * @param <CRQ> create request DTO
 * @param <URQ> update request DTO
 * @param <SRS> summary response DTO (for lists)
 * @param <DRS> detail response DTO (for single entity)
 */
@Transactional
public abstract class BaseServiceImpl<D extends BaseModel, CRQ, URQ, SRS, DRS>
        implements BaseService<D, CRQ, URQ, SRS, DRS> {

    protected final BaseRepositoryPort<D> repositoryPort;
    protected final BaseDtoMapper<D, CRQ, URQ, SRS, DRS> mapper;
    protected final MessageUtil messageUtil;
    protected final IdEncoder idEncoder;

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

    protected void validateCreate(CRQ request) {
    }

    protected void validateUpdate(Long id, URQ request) {
    }

    protected void validateDelete(Long id) {
    }

    protected String resourceType() {
        return "Entity";
    }
}
