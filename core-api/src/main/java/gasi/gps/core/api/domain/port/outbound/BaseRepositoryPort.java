package gasi.gps.core.api.domain.port.outbound;

import java.util.List;
import java.util.Optional;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;

/**
 * Outbound repository contract for domain model persistence.
 *
 * <p>The port is independent from JPA or any other persistence mechanism.
 * Implementations decide how filters, sorting, pagination, and record-rule
 * enforcement are translated to storage queries.</p>
 *
 * @param <T> the domain model type
 * @since 1.0.0
 */
public interface BaseRepositoryPort<T extends BaseModel> {
    /**
     * Persists a domain model.
     *
     * @param model domain model to save
     * @return saved domain model
     */
    T save(T model);

    /**
     * Persists multiple domain models.
     *
     * @param models domain models to save
     * @return saved domain models
     */
    List<T> saveAll(List<T> models);

    /**
     * Finds a model by internal numeric identifier with record rules enabled.
     *
     * @param id internal database identifier
     * @return matching model, or {@link Optional#empty()} when absent
     */
    Optional<T> findById(Long id);

    /**
     * Finds one model matching a filter with record rules enabled.
     *
     * @param filter filter expression, or {@code null} for no filtering
     * @return matching model, or {@link Optional#empty()} when absent
     */
    Optional<T> findBy(GenericFilter filter);

    /**
     * Finds all models matching a filter with record rules enabled.
     *
     * @param filter filter expression, or {@code null} for no filtering
     * @param orders sort orders, or an empty list for natural ordering
     * @return matching models
     */
    List<T> findAll(GenericFilter filter, List<SortOrder> orders);

    /**
     * Finds a page of models with record rules enabled.
     *
     * @param page   zero-based page index
     * @param size   requested page size
     * @param filter filter expression, or {@code null} for no filtering
     * @param orders sort orders, or an empty list for natural ordering
     * @return page result
     */
    PageResult<T> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders);

    /**
     * Deletes a model by internal numeric identifier.
     *
     * @param id internal database identifier
     */
    void delete(Long id);

    /**
     * Deletes multiple models by internal numeric identifiers.
     *
     * @param ids internal database identifiers
     */
    void deleteAllByIds(List<Long> ids);

    /**
     * Deletes all models matching a filter with record rules enabled.
     *
     * @param filter filter expression; implementations should reject
     *               {@code null} to avoid accidental full deletes
     */
    void deleteAllBy(GenericFilter filter);

    /**
     * Finds a model by ID with explicit record-rule control.
     *
     * @param id            internal database identifier
     * @param useRecordRule whether record rules should be applied
     * @return matching model, or {@link Optional#empty()} when absent
     */
    Optional<T> findById(Long id, boolean useRecordRule);

    /**
     * Finds one model with explicit record-rule control.
     *
     * @param filter        filter expression, or {@code null} for no filtering
     * @param useRecordRule whether record rules should be applied
     * @return matching model, or {@link Optional#empty()} when absent
     */
    Optional<T> findBy(GenericFilter filter, boolean useRecordRule);

    /**
     * Finds all models with explicit record-rule control.
     *
     * @param filter        filter expression, or {@code null} for no filtering
     * @param orders        sort orders, or an empty list for natural ordering
     * @param useRecordRule whether record rules should be applied
     * @return matching models
     */
    List<T> findAll(GenericFilter filter, List<SortOrder> orders, boolean useRecordRule);

    /**
     * Finds a page of models with explicit record-rule control.
     *
     * @param page          zero-based page index
     * @param size          requested page size
     * @param filter        filter expression, or {@code null} for no filtering
     * @param orders        sort orders, or an empty list for natural ordering
     * @param useRecordRule whether record rules should be applied
     * @return page result
     */
    PageResult<T> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders, boolean useRecordRule);

    /**
     * Deletes models matching a filter with explicit record-rule control.
     *
     * @param filter        filter expression; implementations should reject
     *                      {@code null} to avoid accidental full deletes
     * @param useRecordRule whether record rules should be applied
     */
    void deleteAllBy(GenericFilter filter, boolean useRecordRule);

}
