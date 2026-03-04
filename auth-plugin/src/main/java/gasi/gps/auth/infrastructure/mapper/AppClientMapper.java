package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.AppClient;
import gasi.gps.auth.infrastructure.entity.AppClientEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;
import gasi.gps.core.api.infrastructure.mapper.StringArrayMapper;

@Mapper
public interface AppClientMapper extends BaseMapper<AppClient, AppClientEntity>, StringArrayMapper {
}
