package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import gasi.gps.auth.domain.model.AppClient;
import gasi.gps.auth.infrastructure.entity.AppClientEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;
import gasi.gps.core.api.infrastructure.mapper.IgnoreAuditFields;
import gasi.gps.core.api.infrastructure.mapper.StringArrayMapper;

@Mapper(componentModel = "spring", uses = { StringArrayMapper.class })
public interface AppClientMapper extends BaseMapper<AppClient, AppClientEntity> {

    @Mapping(target = "grantTypes", source = "grantTypes", qualifiedByName = "stringToArray")
    @Mapping(target = "scopes", source = "scopes", qualifiedByName = "stringToArray")
    AppClient toDomain(AppClientEntity entity);

    @Mapping(target = "grantTypes", source = "grantTypes", qualifiedByName = "arrayToString")
    @Mapping(target = "scopes", source = "scopes", qualifiedByName = "arrayToString")
    AppClientEntity toEntity(AppClient domain);

    @IgnoreAuditFields
    @Mapping(target = "grantTypes", source = "grantTypes", qualifiedByName = "arrayToString")
    @Mapping(target = "scopes", source = "scopes", qualifiedByName = "arrayToString")
    void updateEntity(AppClient source, @MappingTarget AppClientEntity target);
}
