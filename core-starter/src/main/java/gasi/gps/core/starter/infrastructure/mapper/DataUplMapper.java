package gasi.gps.core.starter.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.core.api.domain.model.DataUpl;
import gasi.gps.core.starter.infrastructure.entity.DataUplEntity;

@Mapper
public interface DataUplMapper extends BaseMapper<DataUpl, DataUplEntity> {
}
