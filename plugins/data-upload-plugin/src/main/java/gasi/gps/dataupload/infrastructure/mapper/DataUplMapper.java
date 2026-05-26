package gasi.gps.dataupload.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;
import gasi.gps.dataupload.domain.model.DataUpl;
import gasi.gps.dataupload.infrastructure.entity.DataUplEntity;

@Mapper
public interface DataUplMapper extends BaseMapper<DataUpl, DataUplEntity> {
}
