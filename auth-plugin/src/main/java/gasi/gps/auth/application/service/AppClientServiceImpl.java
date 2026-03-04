package gasi.gps.auth.application.service;

import org.springframework.stereotype.Service;

import gasi.gps.auth.application.dto.AppClientCreateRequest;
import gasi.gps.auth.application.dto.AppClientDetailResponse;
import gasi.gps.auth.application.dto.AppClientSummaryResponse;
import gasi.gps.auth.application.dto.AppClientUpdateRequest;
import gasi.gps.auth.application.mapper.AppClientDtoMapper;
import gasi.gps.auth.domain.model.AppClient;
import gasi.gps.auth.domain.port.inbound.AppClientService;
import gasi.gps.auth.domain.port.outbound.AppClientRepositoryPort;
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;

@Service
public class AppClientServiceImpl extends
        BaseServiceImpl<AppClient, AppClientCreateRequest, AppClientUpdateRequest, AppClientSummaryResponse, AppClientDetailResponse>
        implements AppClientService {

    public AppClientServiceImpl(AppClientRepositoryPort repository,
            AppClientDtoMapper mapper,
            MessageUtil messageUtil) {
        super(repository, mapper, messageUtil);
    }

    @Override
    protected String resourceType() {
        return "AppClient";
    }
}
