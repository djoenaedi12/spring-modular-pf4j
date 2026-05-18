package gasi.gps.core.api.domain.port.inbound;

import java.util.List;

import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;

/**
 * Inbound service contract for standard resource read operations.
 *
 * <p>
 * This interface is intentionally framework-neutral. Implementations may
 * live in plugin modules or in reusable starter support, while callers depend
 * only on this API-level contract. Use this contract when a resource exposes
 * query operations without requiring the standard create, update, and delete
 * commands.
 * </p>
 *
 * @param <SRS> summary response DTO type
 * @param <DRS> detail response DTO type
 * @since 1.0.0
 */
public interface BaseReadService<SRS, DRS> {

    /**
     * Finds a resource by internal numeric identifier.
     *
     * @param id internal database identifier
     * @return detail response for the resource
     */
    DRS findById(Long id);

    /**
     * Finds one resource matching a filter expression.
     *
     * @param filter filter expression, or {@code null} for no filtering
     * @return detail response for the matching resource
     */
    DRS findBy(GenericFilter filter);

    /**
     * Finds all resources matching a filter expression.
     *
     * @param filter filter expression, or {@code null} for no filtering
     * @param orders sort orders, or an empty list for natural ordering
     * @return summary responses for matching resources
     */
    List<SRS> findAll(GenericFilter filter, List<SortOrder> orders);

    /**
     * Finds a page of resources matching a filter expression.
     *
     * @param page   zero-based page index
     * @param size   requested page size
     * @param filter filter expression, or {@code null} for no filtering
     * @param orders sort orders, or an empty list for natural ordering
     * @return page of summary responses
     */
    PageResult<SRS> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders);
}
