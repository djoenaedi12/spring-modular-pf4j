package gasi.gps.auth.application.service;

import org.springframework.stereotype.Service;

import gasi.gps.auth.application.dto.MenuCreateRequest;
import gasi.gps.auth.application.dto.MenuDetailResponse;
import gasi.gps.auth.application.dto.MenuSummaryResponse;
import gasi.gps.auth.application.dto.MenuUpdateRequest;
import gasi.gps.auth.application.mapper.MenuDtoMapper;
import gasi.gps.auth.domain.model.Menu;
import gasi.gps.auth.domain.port.inbound.MenuService;
import gasi.gps.auth.domain.port.outbound.MenuRepositoryPort;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Service
public class MenuServiceImpl extends
        BaseServiceImpl<Menu, MenuCreateRequest, MenuUpdateRequest, MenuSummaryResponse, MenuDetailResponse>
        implements MenuService {

    private final MenuRepositoryPort menuRepositoryPort;
    private final MenuDtoMapper menuMapper;

    public MenuServiceImpl(MenuRepositoryPort repositoryPort,
            MenuDtoMapper mapper,
            MessageUtil messageUtil,
            IdEncoder idEncoder) {
        super(repositoryPort, mapper, messageUtil, idEncoder);
        this.menuRepositoryPort = repositoryPort;
        this.menuMapper = mapper;
    }

    @Override
    public MenuDetailResponse create(MenuCreateRequest request) {
        validateCreate(request);
        Menu domain = menuMapper.toCreateDomain(request);
        domain.setParent(resolveParent(request.getParentId()));
        Menu saved = menuRepositoryPort.save(domain);
        return menuMapper.toDetail(saved);
    }

    @Override
    public MenuDetailResponse update(Long id, MenuUpdateRequest request) {
        Menu existing = menuRepositoryPort.findById(id)
                .orElseThrow(() -> new gasi.gps.core.api.application.exception.EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
        validateUpdate(id, request);
        menuMapper.updateDomain(request, existing);
        existing.setParent(resolveParent(request.getParentId()));
        Menu saved = menuRepositoryPort.save(existing);
        return menuMapper.toDetail(saved);
    }

    private Menu resolveParent(String parentId) {
        if (parentId == null) {
            return null;
        }
        Long decodedId = idEncoder.decode(parentId);
        return menuRepositoryPort.findById(decodedId)
                .orElseThrow(() -> new BusinessException("Invalid parentId: " + parentId));
    }

    @Override
    protected String resourceType() {
        return "Menu";
    }
}
