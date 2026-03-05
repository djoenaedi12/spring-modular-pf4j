package gasi.gps.auth.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import gasi.gps.auth.application.dto.RecordRuleCreateRequest;
import gasi.gps.auth.application.dto.RecordRuleDetailResponse;
import gasi.gps.auth.application.dto.RecordRuleSummaryResponse;
import gasi.gps.auth.application.dto.RecordRuleUpdateRequest;
import gasi.gps.auth.domain.model.RecordRule;
import gasi.gps.auth.domain.model.Resource;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.application.mapper.IgnoreAuditFields;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public abstract class RecordRuleDtoMapper implements
        BaseDtoMapper<RecordRule, RecordRuleCreateRequest, RecordRuleUpdateRequest, RecordRuleSummaryResponse, RecordRuleDetailResponse> {

    @Autowired
    protected IdEncoder idEncoder;

    @Override
    @IgnoreAuditFields
    @Mapping(target = "resource", ignore = true)
    public abstract RecordRule toCreateDomain(RecordRuleCreateRequest createRequest);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "resource", ignore = true)
    public abstract RecordRule toUpdateDomain(RecordRuleUpdateRequest updateRequest);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    @Mapping(target = "resourceId", source = "resource", qualifiedByName = "resourceToResourceId")
    public abstract RecordRuleSummaryResponse toSummary(RecordRule domain);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    @Mapping(target = "resourceId", source = "resource", qualifiedByName = "resourceToResourceId")
    public abstract RecordRuleDetailResponse toDetail(RecordRule domain);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "resource", ignore = true)
    public abstract void updateDomain(RecordRuleUpdateRequest updateRequest, @MappingTarget RecordRule domain);

    @Named("resourceToResourceId")
    protected String resourceToResourceId(Resource resource) {
        if (resource == null || resource.getId() == null) {
            return null;
        }
        return idEncoder.encode(resource.getId());
    }
}
