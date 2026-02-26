package gasi.gps.api.shared.domain.port;

import java.util.List;
import java.util.Optional;

import gasi.gps.api.shared.domain.model.BaseModel;
import gasi.gps.api.shared.domain.model.GenericFilter;
import gasi.gps.api.shared.domain.model.PageResult;
import gasi.gps.api.shared.domain.model.SortOrder;

/**
 * Generic base repository port for domain models.
 *
 * @param <T>  the domain model type
 * @param <ID> the identifier type
 */
public interface BaseRepositoryPort<T extends BaseModel<ID>, ID> {
    T save(T model);

    Optional<T> findById(ID id);

    Optional<T> findBy(GenericFilter filter);

    List<T> findAll(GenericFilter filter, List<SortOrder> orders);

    PageResult<T> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders);

    Optional<T> findById(ID id, boolean useRecordRule);

    Optional<T> findBy(GenericFilter filter, boolean useRecordRule);

    List<T> findAll(GenericFilter filter, List<SortOrder> orders, boolean useRecordRule);

    PageResult<T> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders, boolean useRecordRule);

    void delete(ID id);
}
