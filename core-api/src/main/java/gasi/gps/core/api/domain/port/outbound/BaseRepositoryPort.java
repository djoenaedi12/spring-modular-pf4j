package gasi.gps.core.api.domain.port.outbound;

import java.util.List;
import java.util.Optional;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;

/**
 * Generic base repository port for domain models.
 *
 * @param <T>  the domain model type
 * @param <ID> the identifier type
 */
public interface BaseRepositoryPort<T extends BaseModel> {
    T save(T model);

    List<T> saveAll(List<T> models);

    Optional<T> findById(Long id);

    Optional<T> findBy(GenericFilter filter);

    List<T> findAll(GenericFilter filter, List<SortOrder> orders);

    PageResult<T> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders);

    void delete(Long id);

    void deleteAllByIds(List<Long> ids);

    void deleteAllBy(GenericFilter filter);

    Optional<T> findById(Long id, boolean useRecordRule);

    Optional<T> findBy(GenericFilter filter, boolean useRecordRule);

    List<T> findAll(GenericFilter filter, List<SortOrder> orders, boolean useRecordRule);

    PageResult<T> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders, boolean useRecordRule);

    void deleteAllBy(GenericFilter filter, boolean useRecordRule);

}
