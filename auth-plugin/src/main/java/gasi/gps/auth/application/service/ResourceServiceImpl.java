package gasi.gps.auth.application.service;

import org.springframework.stereotype.Service;

import gasi.gps.auth.application.dto.ResourceCreateRequest;
import gasi.gps.auth.application.dto.ResourceDetailResponse;
import gasi.gps.auth.application.dto.ResourceSummaryResponse;
import gasi.gps.auth.application.dto.ResourceUpdateRequest;
import gasi.gps.auth.application.mapper.ResourceDtoMapper;
import gasi.gps.auth.domain.model.Menu;
import gasi.gps.auth.domain.model.Resource;
import gasi.gps.auth.domain.port.inbound.ResourceService;
import gasi.gps.auth.domain.port.outbound.MenuRepositoryPort;
import gasi.gps.auth.domain.port.outbound.ResourceRepositoryPort;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Service
public class ResourceServiceImpl extends
        BaseServiceImpl<Resource, ResourceCreateRequest, ResourceUpdateRequest, ResourceSummaryResponse, ResourceDetailResponse>
        implements ResourceService {

    private final ResourceRepositoryPort resourceRepositoryPort;
    private final ResourceDtoMapper resourceMapper;
    private final MenuRepositoryPort menuRepositoryPort;

    public ResourceServiceImpl(ResourceRepositoryPort repositoryPort,
            ResourceDtoMapper mapper,
            MessageUtil messageUtil,
            MenuRepositoryPort menuRepositoryPort,
            IdEncoder idEncoder) {
        super(repositoryPort, mapper, messageUtil, idEncoder);
        this.resourceRepositoryPort = repositoryPort;
        this.resourceMapper = mapper;
        this.menuRepositoryPort = menuRepositoryPort;
    }

    @Override
    public ResourceDetailResponse create(ResourceCreateRequest request) {
        validateCreate(request);
        Resource domain = resourceMapper.toCreateDomain(request);
        domain.setMenu(resolveMenu(request.getMenuId()));
        Resource saved = resourceRepositoryPort.save(domain);
        return resourceMapper.toDetail(saved);
    }

    @Override
    public ResourceDetailResponse update(Long id, ResourceUpdateRequest request) {
        Resource existing = resourceRepositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
        validateUpdate(id, request);
        resourceMapper.updateDomain(request, existing);
        existing.setMenu(resolveMenu(request.getMenuId()));
        Resource saved = resourceRepositoryPort.save(existing);
        return resourceMapper.toDetail(saved);
    }

    private Menu resolveMenu(String menuId) {
        if (menuId == null) {
            return null;
        }
        Long decodedId = idEncoder.decode(menuId);
        return menuRepositoryPort.findById(decodedId)
                .orElseThrow(() -> new BusinessException("Invalid menuId: " + menuId));
    }

    @Override
    protected String resourceType() {
        return "Resource";
    }
}
