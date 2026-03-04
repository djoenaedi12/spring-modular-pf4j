package gasi.gps.core.api.domain.port.inbound;

import java.util.List;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;

public interface BaseService<D extends BaseModel, CRQ, URQ, SRS, DRS> {

    public DRS create(CRQ request);

    public DRS findById(Long id);

    public DRS findBy(GenericFilter filter);

    public List<SRS> findAll(GenericFilter filter, List<SortOrder> orders);

    public PageResult<SRS> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders);

    public DRS update(Long id, URQ request);

    public void delete(Long id);
}
