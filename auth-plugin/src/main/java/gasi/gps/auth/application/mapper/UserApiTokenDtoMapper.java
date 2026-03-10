package gasi.gps.auth.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import gasi.gps.auth.application.dto.UserApiTokenCreateRequest;
import gasi.gps.auth.application.dto.UserApiTokenDetailResponse;
import gasi.gps.auth.application.dto.UserApiTokenSummaryResponse;
import gasi.gps.auth.domain.model.UserApiToken;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.application.mapper.IgnoreAuditFields;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

/**
 * Mapper for user API token DTO responses.
 */
@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public interface UserApiTokenDtoMapper extends
        BaseDtoMapper<UserApiToken, UserApiTokenCreateRequest, UserApiTokenCreateRequest, UserApiTokenSummaryResponse, UserApiTokenDetailResponse> {

    @Override
    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tokenHash", ignore = true)
    public abstract UserApiToken toCreateDomain(UserApiTokenCreateRequest createRequest);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tokenHash", ignore = true)
    public abstract UserApiToken toUpdateDomain(UserApiTokenCreateRequest updateRequest);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    @Mapping(target = "userId", source = "user.id", qualifiedByName = "encodeId")
    public abstract UserApiTokenSummaryResponse toSummary(UserApiToken domain);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    @Mapping(target = "userId", source = "user.id", qualifiedByName = "encodeId")
    public abstract UserApiTokenDetailResponse toDetail(UserApiToken domain);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tokenHash", ignore = true)
    public abstract void updateDomain(UserApiTokenCreateRequest updateRequest, @MappingTarget UserApiToken domain);
}
