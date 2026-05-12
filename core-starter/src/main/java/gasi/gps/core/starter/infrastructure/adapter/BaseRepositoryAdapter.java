package gasi.gps.core.starter.infrastructure.adapter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
import gasi.gps.core.starter.infrastructure.entity.BaseEntity;
import gasi.gps.core.starter.infrastructure.filter.FilterableFieldResolver;
import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;
import gasi.gps.core.starter.infrastructure.specification.GenericSpecification;

/**
 * Generic Spring Data JPA adapter for {@link BaseRepositoryPort}.
 *
 * <p>This adapter handles common CRUD, filtering, sorting, pagination, entity
 * mapping, and public filter-field validation. Record-rule support is exposed
 * through method parameters and can be added by extending the record-rule
 * hook.</p>
 *
 * @param <D>  domain model type
 * @param <E>  JPA entity type
 * @since 1.0.0
 */
public abstract class BaseRepositoryAdapter<D extends BaseModel, E extends BaseEntity>
        implements BaseRepositoryPort<D> {

    private final JpaRepository<E, Long> jpaRepository;
    private final JpaSpecificationExecutor<E> specExecutor;
    private final BaseMapper<D, E> mapper;
    private final Class<E> entityClass;

    /**
     * Creates an adapter and resolves the entity class from generic metadata.
     *
     * @param repository Spring Data repository implementing JPA and specification
     *                   contracts
     * @param mapper     mapper between domain model and JPA entity
     * @param <R>        repository type
     */
    protected <R extends JpaRepository<E, Long> & JpaSpecificationExecutor<E>> BaseRepositoryAdapter(R repository,
            BaseMapper<D, E> mapper) {
        this(repository, mapper, null);
    }

    /**
     * Creates an adapter with an explicit entity class.
     *
     * <p>Use this constructor when the entity class cannot be resolved from
     * generic metadata, for example through an intermediate abstract adapter.</p>
     *
     * @param repository  Spring Data repository implementing JPA and specification
     *                    contracts
     * @param mapper      mapper between domain model and JPA entity
     * @param entityClass JPA entity class
     * @param <R>         repository type
     */
    protected <R extends JpaRepository<E, Long> & JpaSpecificationExecutor<E>> BaseRepositoryAdapter(R repository,
            BaseMapper<D, E> mapper,
            Class<E> entityClass) {
        this.jpaRepository = repository;
        this.specExecutor = repository;
        this.mapper = mapper;
        this.entityClass = entityClass != null ? entityClass : resolveEntityClass();
    }

    /**
     * Returns the resource type used for record-rule resolution.
     *
     * @return resource type code expected by the record-rule implementation
     */
    protected abstract String resourceType();

    // ── Save ──────────────────────────────────────────────

    @Override
    public D save(D model) {
        E entity = mapper.toEntity(model);
        E saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<D> saveAll(List<D> models) {
        List<E> entities = models.stream()
                .map(mapper::toEntity)
                .toList();
        return jpaRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .toList();
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

    @Override
    public void deleteAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        jpaRepository.deleteAllById(ids);
    }

    @Override
    public void deleteAllBy(GenericFilter filter) {
        deleteAllBy(filter, true);
    }

    @Override
    public void deleteAllBy(GenericFilter filter, boolean useRecordRule) {
        if (filter == null) {
            throw new IllegalArgumentException("GenericFilter must not be null for bulk delete");
        }

        GenericFilter effectiveFilter = useRecordRule ? applyRecordRules(filter) : filter;
        Specification<E> spec = toSpec(effectiveFilter);
        List<E> entities = specExecutor.findAll(spec);

        if (entities.isEmpty()) {
            return;
        }

        jpaRepository.deleteAllInBatch(entities);
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
                        FilterableFieldResolver.resolve(entityClass, order.getField())))
                .toList());
    }

    @SuppressWarnings("unchecked")
    private Class<E> resolveEntityClass() {
        Type current = getClass().getGenericSuperclass();
        while (current != null) {
            if (current instanceof ParameterizedType parameterizedType
                    && parameterizedType.getRawType() instanceof Class<?> rawType
                    && BaseRepositoryAdapter.class.isAssignableFrom(rawType)) {
                Type entityType = parameterizedType.getActualTypeArguments()[1];
                if (entityType instanceof Class<?> entityClassType) {
                    return (Class<E>) entityClassType;
                }
            }

            if (current instanceof Class<?> currentClass) {
                current = currentClass.getGenericSuperclass();
            } else {
                break;
            }
        }
        throw new IllegalStateException("Unable to resolve entity class for " + getClass().getName()
                + ". Use the BaseRepositoryAdapter(repository, mapper, entityClass) constructor.");
    }
}
