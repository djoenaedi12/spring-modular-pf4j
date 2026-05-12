package gasi.gps.auth.application.service;

import org.springframework.stereotype.Service;

import gasi.gps.auth.application.dto.ActionCreateRequest;
import gasi.gps.auth.application.dto.ActionDetailResponse;
import gasi.gps.auth.application.dto.ActionSummaryResponse;
import gasi.gps.auth.application.dto.ActionUpdateRequest;
import gasi.gps.auth.application.mapper.ActionDtoMapper;
import gasi.gps.auth.domain.model.Action;
import gasi.gps.auth.domain.port.inbound.ActionService;
import gasi.gps.auth.domain.port.outbound.ActionRepositoryPort;
import gasi.gps.core.starter.application.service.BaseServiceImpl;
import gasi.gps.core.api.audit.AuditableEntity;
import gasi.gps.core.starter.infrastructure.i18n.MessageUtil;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

@Service
@AuditableEntity(module = "auth", resourceType = "Action")
public class ActionServiceImpl extends
        BaseServiceImpl<Action, ActionCreateRequest, ActionUpdateRequest, ActionSummaryResponse, ActionDetailResponse>
        implements ActionService {

    private final PermissionCacheService permissionCacheService;

    public ActionServiceImpl(ActionRepositoryPort repositoryPort,
            ActionDtoMapper mapper,
            MessageUtil messageUtil, IdEncoder idEncoder,
            PermissionCacheService permissionCacheService) {
        super(repositoryPort, mapper, messageUtil, idEncoder);
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    protected String resourceType() {
        return "Action";
    }

    @Override
    public ActionDetailResponse create(ActionCreateRequest request) {
        ActionDetailResponse response = super.create(request);
        permissionCacheService.evictAllRolePermissions();
        return response;
    }

    @Override
    public ActionDetailResponse update(Long id, ActionUpdateRequest request) {
        ActionDetailResponse response = super.update(id, request);
        permissionCacheService.evictAllRolePermissions();
        return response;
    }

    @Override
    public void delete(Long id) {
        super.delete(id);
        permissionCacheService.evictAllRolePermissions();
    }
}
