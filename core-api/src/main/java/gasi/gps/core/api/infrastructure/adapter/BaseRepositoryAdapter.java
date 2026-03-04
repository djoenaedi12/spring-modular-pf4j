package gasi.gps.core.api.infrastructure.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;
import gasi.gps.core.api.infrastructure.entity.BaseEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;
import gasi.gps.core.api.infrastructure.specification.GenericSpecification;

/**
 * Generic repository adapter with CRUD, filtering, sorting, pagination,
 * and transparent horizontal access control (record rules).
 *
 * @param <D>  domain model type
 * @param <E>  JPA entity type
 * @param <ID> identifier type
 */
public abstract class BaseRepositoryAdapter<D extends BaseModel, E extends BaseEntity>
        implements BaseRepositoryPort<D> {

    private final JpaRepository<E, Long> jpaRepository;
    private final JpaSpecificationExecutor<E> specExecutor;
    private final BaseMapper<D, E> mapper;

    protected <R extends JpaRepository<E, Long> & JpaSpecificationExecutor<E>> BaseRepositoryAdapter(R repository,
            BaseMapper<D, E> mapper) {
        this.jpaRepository = repository;
        this.specExecutor = repository;
        this.mapper = mapper;
    }

    /**
     * Returns the entity type name used for record rule resolution.
     * Must match the EntityType.name in the database.
     */
    protected abstract String resourceType();

    // ── Save ──────────────────────────────────────────────

    @Override
    public D save(D model) {
        E entity = mapper.toEntity(model);
        E saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    // ── Find single ───────────────────────────────────────

    @Override
    public Optional<D> findById(Long id) {
        return findById(id, true);
    }

    @Override
    public Optional<D> findById(Long id, boolean useRecordRule) {
        GenericFilter idFilter = SimpleFilter.builder()
                .field("id")
                .operator(SimpleFilter.FilterOperator.EQUALS)
                .value(id)
                .build();
        return findBy(idFilter, useRecordRule);
    }

    @Override
    public Optional<D> findBy(GenericFilter filter) {
        return findBy(filter, true);
    }

    @Override
    public Optional<D> findBy(GenericFilter filter, boolean useRecordRule) {
        GenericFilter effectiveFilter = useRecordRule ? applyRecordRules(filter) : filter;
        Specification<E> spec = GenericSpecification.from(effectiveFilter);
        return specExecutor.findOne(spec).map(mapper::toDomain);
    }

    // ── Find all ──────────────────────────────────────────

    @Override
    public List<D> findAll(GenericFilter filter, List<SortOrder> orders) {
        return findAll(filter, orders, true);
    }

    @Override
    public List<D> findAll(GenericFilter filter, List<SortOrder> orders, boolean useRecordRule) {
        GenericFilter effectiveFilter = useRecordRule ? applyRecordRules(filter) : filter;
        Specification<E> spec = toSpec(effectiveFilter);
        Sort sort = toSort(orders);
        return specExecutor.findAll(spec, sort).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    // ── Find all with pagination ──────────────────────────

    @Override
    public PageResult<D> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders) {
        return findAll(page, size, filter, orders, true);
    }

    @Override
    public PageResult<D> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders,
            boolean useRecordRule) {
        GenericFilter effectiveFilter = useRecordRule ? applyRecordRules(filter) : filter;
        Specification<E> spec = toSpec(effectiveFilter);
        PageRequest pageable = PageRequest.of(page, size, toSort(orders));
        Page<E> result = specExecutor.findAll(spec, pageable);

        List<D> content = result.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return PageResult.<D>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    // ── Delete ────────────────────────────────────────────

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    // ── Record Rules ─────────────────────────────────────

    private GenericFilter applyRecordRules(GenericFilter filter) {
        return filter;
        // if (!securityContext.isAuthenticated()) {
        // return userFilter;
        // }
        // GenericFilter ruleFilter = recordRuleService.resolveFilter(
        // securityContext.getRoleIds(),
        // resourceType(),
        // securityContext.getRequestUri());
        // return recordRuleService.combineWithUserFilter(userFilter, ruleFilter);
    }

    // ── Helpers ───────────────────────────────────────────

    private Specification<E> toSpec(GenericFilter filter) {
        return GenericSpecification.from(filter);
    }

    private Sort toSort(List<SortOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return Sort.unsorted();
        }
        return Sort.by(orders.stream()
                .filter(s -> s.getField() != null && !s.getField().isBlank())
                .map(order -> new Sort.Order(
                        Sort.Direction.fromString(order.getDirection().toString()),
                        order.getField()))
                .toList());
    }
}
