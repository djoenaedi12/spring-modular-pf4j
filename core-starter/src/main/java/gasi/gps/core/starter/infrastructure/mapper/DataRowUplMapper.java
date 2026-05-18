package gasi.gps.core.starter.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.core.api.domain.model.DataRowUpl;
import gasi.gps.core.starter.infrastructure.entity.DataRowUplEntity;

@Mapper
public interface DataRowUplMapper extends BaseMapper<DataRowUpl, DataRowUplEntity> {
}
