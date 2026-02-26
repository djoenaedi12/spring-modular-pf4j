package gasi.gps.api.shared.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import gasi.gps.api.shared.application.exception.EntityNotFoundException;
import gasi.gps.api.shared.application.mapper.BaseDtoMapper;
import gasi.gps.api.shared.domain.model.BaseModel;
import gasi.gps.api.shared.domain.model.GenericFilter;
import gasi.gps.api.shared.domain.model.PageResult;
import gasi.gps.api.shared.domain.model.SortOrder;
import gasi.gps.api.shared.domain.port.BaseRepositoryPort;

/**
 * Generic base service with CRUD, filtering, sorting, and pagination.
 * Record rules (horizontal access control) are enforced at the repository
 * layer.
 *
 * @param <D>   domain model type
 * @param <ID>  identifier type
 * @param <CRQ> create request DTO
 * @param <URQ> update request DTO
 * @param <SUM> summary response DTO (for lists)
 * @param <DET> detail response DTO (for single entity)
 */
@Transactional
public abstract class BaseService<D extends BaseModel<ID>, ID, CRQ, URQ, SUM, DET> {

    protected final BaseRepositoryPort<D, ID> repository;
    protected final BaseDtoMapper<D, CRQ, URQ, SUM, DET> mapper;
    protected final MessageHelper msg;

    protected BaseService(BaseRepositoryPort<D, ID> repository,
            BaseDtoMapper<D, CRQ, URQ, SUM, DET> mapper,
            MessageHelper msg) {
        this.repository = repository;
        this.mapper = mapper;
        this.msg = msg;
    }

    public DET create(CRQ request) {
        validateCreate(request);
        D domain = mapper.toCreateDomain(request);
        D saved = repository.save(domain);
        return mapper.toDetail(saved);
    }

    @Transactional(readOnly = true)
    public DET findById(ID id) {
        D domain = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        msg.get("error.entity.notFound", entityName(), id)));
        return mapper.toDetail(domain);
    }

    @Transactional(readOnly = true)
    public DET findBy(GenericFilter filter) {
        D domain = repository.findBy(filter)
                .orElseThrow(() -> new EntityNotFoundException(
                        msg.get("error.entity.notFound", entityName(), "filter")));
        return mapper.toDetail(domain);
    }

    @Transactional(readOnly = true)
    public List<SUM> findAll(GenericFilter filter, List<SortOrder> orders) {
        List<D> result = repository.findAll(filter, orders);
        return result.stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResult<SUM> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders) {
        PageResult<D> result = repository.findAll(page, size, filter, orders);
        List<SUM> content = result.getContent().stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());
        return PageResult.<SUM>builder()
                .content(content)
                .page(result.getPage())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    public DET update(ID id, URQ request) {
        D existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        msg.get("error.entity.notFound", entityName(), id)));
        validateUpdate(id, request);
        mapper.updateDomain(request, existing);
        D saved = repository.save(existing);
        return mapper.toDetail(saved);
    }

    public void delete(ID id) {
        repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        msg.get("error.entity.notFound", entityName(), id)));
        validateDelete(id);
        repository.delete(id);
    }

    protected void validateCreate(CRQ request) {
    }

    protected void validateUpdate(ID id, URQ request) {
    }

    protected void validateDelete(ID id) {
    }

    protected String entityName() {
        return "Entity";
    }
}
