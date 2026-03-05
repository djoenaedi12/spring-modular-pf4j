package gasi.gps.auth.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import gasi.gps.auth.application.dto.ResourceCreateRequest;
import gasi.gps.auth.application.dto.ResourceDetailResponse;
import gasi.gps.auth.application.dto.ResourceSummaryResponse;
import gasi.gps.auth.application.dto.ResourceUpdateRequest;
import gasi.gps.auth.domain.model.Menu;
import gasi.gps.auth.domain.model.Resource;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.application.mapper.IgnoreAuditFields;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public abstract class ResourceDtoMapper implements
        BaseDtoMapper<Resource, ResourceCreateRequest, ResourceUpdateRequest, ResourceSummaryResponse, ResourceDetailResponse> {

    @Autowired
    protected IdEncoder idEncoder;

    @Override
    @IgnoreAuditFields
    @Mapping(target = "menu", ignore = true)
    public abstract Resource toCreateDomain(ResourceCreateRequest createRequest);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "menu", ignore = true)
    public abstract Resource toUpdateDomain(ResourceUpdateRequest updateRequest);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    @Mapping(target = "menuId", source = "menu", qualifiedByName = "menuToMenuId")
    public abstract ResourceSummaryResponse toSummary(Resource domain);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    @Mapping(target = "menuId", source = "menu", qualifiedByName = "menuToMenuId")
    public abstract ResourceDetailResponse toDetail(Resource domain);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "menu", ignore = true)
    public abstract void updateDomain(ResourceUpdateRequest updateRequest, @MappingTarget Resource domain);

    @Named("menuToMenuId")
    protected String menuToMenuId(Menu menu) {
        if (menu == null || menu.getId() == null) {
            return null;
        }
        return idEncoder.encode(menu.getId());
    }
}
