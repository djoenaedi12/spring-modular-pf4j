package gasi.gps.auth.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import gasi.gps.auth.application.dto.MenuCreateRequest;
import gasi.gps.auth.application.dto.MenuDetailResponse;
import gasi.gps.auth.application.dto.MenuSummaryResponse;
import gasi.gps.auth.application.dto.MenuUpdateRequest;
import gasi.gps.auth.domain.model.Menu;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.application.mapper.IgnoreAuditFields;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public abstract class MenuDtoMapper implements
        BaseDtoMapper<Menu, MenuCreateRequest, MenuUpdateRequest, MenuSummaryResponse, MenuDetailResponse> {

    @Autowired
    protected IdEncoder idEncoder;

    @Override
    @IgnoreAuditFields
    @Mapping(target = "parent", ignore = true)
    public abstract Menu toCreateDomain(MenuCreateRequest createRequest);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "parent", ignore = true)
    public abstract Menu toUpdateDomain(MenuUpdateRequest updateRequest);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    @Mapping(target = "parentId", source = "parent", qualifiedByName = "menuToParentId")
    public abstract MenuSummaryResponse toSummary(Menu domain);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    @Mapping(target = "parentId", source = "parent", qualifiedByName = "menuToParentId")
    public abstract MenuDetailResponse toDetail(Menu domain);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "parent", ignore = true)
    public abstract void updateDomain(MenuUpdateRequest updateRequest, @MappingTarget Menu domain);

    @Named("menuToParentId")
    protected String menuToParentId(Menu parent) {
        if (parent == null || parent.getId() == null) {
            return null;
        }
        return idEncoder.encode(parent.getId());
    }
}
