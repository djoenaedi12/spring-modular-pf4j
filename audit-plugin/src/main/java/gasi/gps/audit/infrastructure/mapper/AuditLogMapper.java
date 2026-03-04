package gasi.gps.audit.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.infrastructure.entity.AuditLogEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper extends BaseMapper<AuditLog, AuditLogEntity> {

    @Override
    @Mapping(target = "fieldsChanged", source = "fieldsChanged", qualifiedByName = "stringToArray")
    AuditLog toDomain(AuditLogEntity entity);

    @Override
    @Mapping(target = "fieldsChanged", source = "fieldsChanged", qualifiedByName = "arrayToString")
    AuditLogEntity toEntity(AuditLog domain);

    @Named("stringToArray")
    default String[] stringToArray(String value) {
        if (value == null || value.isEmpty()) {
            return new String[0];
        }
        return value.split(",");
    }

    @Named("arrayToString")
    default String arrayToString(String[] value) {
        if (value == null || value.length == 0) {
            return null;
        }
        return String.join(",", value);
    }
}
