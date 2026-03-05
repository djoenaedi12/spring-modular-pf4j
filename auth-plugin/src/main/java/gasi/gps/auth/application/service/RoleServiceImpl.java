package gasi.gps.auth.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.auth.application.dto.PermissionRequest;
import gasi.gps.auth.application.dto.PermissionResponse;
import gasi.gps.auth.application.dto.RoleCreateRequest;
import gasi.gps.auth.application.dto.RoleDetailResponse;
import gasi.gps.auth.application.dto.RoleRecordRuleRequest;
import gasi.gps.auth.application.dto.RoleRecordRuleResponse;
import gasi.gps.auth.application.dto.RoleSummaryResponse;
import gasi.gps.auth.application.dto.RoleUpdateRequest;
import gasi.gps.auth.application.mapper.RoleDtoMapper;
import gasi.gps.auth.domain.model.Action;
import gasi.gps.auth.domain.model.Menu;
import gasi.gps.auth.domain.model.Permission;
import gasi.gps.auth.domain.model.RecordRule;
import gasi.gps.auth.domain.model.Resource;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.domain.model.RoleRecordRule;
import gasi.gps.auth.domain.port.inbound.RoleService;
import gasi.gps.auth.domain.port.outbound.ActionRepositoryPort;
import gasi.gps.auth.domain.port.outbound.MenuRepositoryPort;
import gasi.gps.auth.domain.port.outbound.RecordRuleRepositoryPort;
import gasi.gps.auth.domain.port.outbound.ResourceRepositoryPort;
import gasi.gps.auth.domain.port.outbound.RoleRepositoryPort;
import gasi.gps.auth.infrastructure.entity.MenuEntity;
import gasi.gps.auth.infrastructure.entity.PermissionEntity;
import gasi.gps.auth.infrastructure.entity.RoleEntity;
import gasi.gps.auth.infrastructure.entity.RoleMenuEntity;
import gasi.gps.auth.infrastructure.entity.RoleRecordRuleEntity;
import gasi.gps.auth.infrastructure.mapper.PermissionMapper;
import gasi.gps.auth.infrastructure.mapper.RoleRecordRuleMapper;
import gasi.gps.auth.infrastructure.persistence.PermissionEntityRepository;
import gasi.gps.auth.infrastructure.persistence.RoleMenuEntityRepository;
import gasi.gps.auth.infrastructure.persistence.RoleRecordRuleEntityRepository;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Service
public class RoleServiceImpl extends
        BaseServiceImpl<Role, RoleCreateRequest, RoleUpdateRequest, RoleSummaryResponse, RoleDetailResponse>
        implements RoleService {

    private final RoleDtoMapper roleDtoMapper;
    private final PermissionEntityRepository permissionEntityRepository;
    private final RoleRecordRuleEntityRepository roleRecordRuleEntityRepository;
    private final RoleMenuEntityRepository roleMenuEntityRepository;
    private final PermissionMapper permissionMapper;
    private final RoleRecordRuleMapper roleRecordRuleMapper;
    private final RoleRepositoryPort roleRepositoryPort;
    private final ActionRepositoryPort actionRepositoryPort;
    private final ResourceRepositoryPort resourceRepositoryPort;
    private final RecordRuleRepositoryPort recordRuleRepositoryPort;
    private final MenuRepositoryPort menuRepositoryPort;
    private final IdEncoder idEncoder;
    private final CacheManager cacheManager;

    public RoleServiceImpl(RoleRepositoryPort repositoryPort,
            RoleDtoMapper mapper,
            MessageUtil messageUtil,
            PermissionEntityRepository permissionEntityRepository,
            RoleRecordRuleEntityRepository roleRecordRuleEntityRepository,
            RoleMenuEntityRepository roleMenuEntityRepository,
            PermissionMapper permissionMapper,
            RoleRecordRuleMapper roleRecordRuleMapper,
            ActionRepositoryPort actionRepositoryPort,
            ResourceRepositoryPort resourceRepositoryPort,
            RecordRuleRepositoryPort recordRuleRepositoryPort,
            MenuRepositoryPort menuRepositoryPort,
            IdEncoder idEncoder,
            CacheManager cacheManager) {
        super(repositoryPort, mapper, messageUtil);
        this.roleDtoMapper = mapper;
        this.permissionEntityRepository = permissionEntityRepository;
        this.roleRecordRuleEntityRepository = roleRecordRuleEntityRepository;
        this.roleMenuEntityRepository = roleMenuEntityRepository;
        this.permissionMapper = permissionMapper;
        this.roleRepositoryPort = repositoryPort;
        this.roleRecordRuleMapper = roleRecordRuleMapper;
        this.actionRepositoryPort = actionRepositoryPort;
        this.resourceRepositoryPort = resourceRepositoryPort;
        this.recordRuleRepositoryPort = recordRuleRepositoryPort;
        this.menuRepositoryPort = menuRepositoryPort;
        this.idEncoder = idEncoder;
        this.cacheManager = cacheManager;
    }

    @Override
    protected String resourceType() {
        return "Role";
    }

    @Override
    @Transactional
    public RoleDetailResponse create(RoleCreateRequest request) {
        // Validate + fetch all referenced entities upfront (single batch per type)
        ResolvedRefs refs = validateAndResolveIds(
                request.getPermissions(), request.getRecordRules(), request.getMenuIds());

        RoleDetailResponse response = super.create(request);

        Long roleId = getIdFromResponse(response);
        Role role = roleRepositoryPort.findById(roleId).orElse(null);

        savePermissions(request.getPermissions(), role, refs.actionMap, refs.resourceMap);
        saveRoleRecordRules(request.getRecordRules(), role, refs.recordRuleMap);
        saveRoleMenus(request.getMenuIds(), role);

        return enrichDetail(response, roleId);
    }

    @Override
    @Transactional
    public RoleDetailResponse update(Long id, RoleUpdateRequest request) {
        // Validate + fetch all referenced entities upfront (single batch per type)
        ResolvedRefs refs = validateAndResolveIds(
                request.getPermissions(), request.getRecordRules(), request.getMenuIds());

        RoleDetailResponse response = super.update(id, request);

        Role role = roleRepositoryPort.findById(id).orElse(null);

        permissionEntityRepository.deleteByRoleId(id);
        roleRecordRuleEntityRepository.deleteByRoleId(id);
        roleMenuEntityRepository.deleteByRoleId(id);

        savePermissions(request.getPermissions(), role, refs.actionMap, refs.resourceMap);
        saveRoleRecordRules(request.getRecordRules(), role, refs.recordRuleMap);
        saveRoleMenus(request.getMenuIds(), role);

        if (role != null && cacheManager.getCache("rolePermissions") != null) {
            cacheManager.getCache("rolePermissions").evictIfPresent(role.getCode());
        }

        return enrichDetail(response, id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = roleRepositoryPort.findById(id).orElse(null);
        super.delete(id);
        if (role != null && cacheManager.getCache("rolePermissions") != null) {
            cacheManager.getCache("rolePermissions").evictIfPresent(role.getCode());
        }
    }

    @Override
    public RoleDetailResponse findById(Long id) {
        RoleDetailResponse response = super.findById(id);
        return enrichDetail(response, id);
    }

    private RoleDetailResponse enrichDetail(RoleDetailResponse response, Long roleId) {
        List<PermissionEntity> permissionEntities = permissionEntityRepository.findByRoleId(roleId);
        List<PermissionResponse> permissionResponses = permissionEntities.stream()
                .map(entity -> roleDtoMapper.toPermissionResponse(permissionMapper.toDomain(entity)))
                .collect(Collectors.toList());

        List<RoleRecordRuleEntity> roleRecordRuleEntities = roleRecordRuleEntityRepository.findByRoleId(roleId);
        List<RoleRecordRuleResponse> roleRecordRuleResponses = roleRecordRuleEntities.stream()
                .map(entity -> roleDtoMapper.toRoleRecordRuleResponse(roleRecordRuleMapper.toDomain(entity)))
                .collect(Collectors.toList());

        List<RoleMenuEntity> roleMenuEntities = roleMenuEntityRepository.findByRoleId(roleId);
        Set<String> menuIds = roleMenuEntities.stream()
                .map(entity -> idEncoder.encode(entity.getMenu().getId()))
                .collect(Collectors.toSet());

        response.setPermissions(permissionResponses);
        response.setRecordRules(roleRecordRuleResponses);
        response.setMenuIds(menuIds);
        return response;
    }

    // ── Save methods (use pre-fetched maps, no extra queries) ──

    private void savePermissions(List<PermissionRequest> requests, Role role,
            Map<Long, Action> actionMap, Map<Long, Resource> resourceMap) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        List<PermissionEntity> entities = requests.stream()
                .map(req -> {
                    Long actionId = idEncoder.decode(req.getActionId());
                    Long resourceId = idEncoder.decode(req.getResourceId());
                    Permission permission = Permission.builder()
                            .role(role)
                            .action(actionMap.get(actionId))
                            .resource(resourceMap.get(resourceId))
                            .build();
                    return permissionMapper.toEntity(permission);
                })
                .collect(Collectors.toList());
        permissionEntityRepository.saveAll(entities);
    }

    private void saveRoleRecordRules(List<RoleRecordRuleRequest> requests, Role role,
            Map<Long, RecordRule> recordRuleMap) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        List<RoleRecordRuleEntity> entities = requests.stream()
                .map(req -> {
                    Long ruleId = idEncoder.decode(req.getRecordRuleId());
                    RoleRecordRule rr = RoleRecordRule.builder()
                            .role(role)
                            .recordRule(recordRuleMap.get(ruleId))
                            .isNegated(req.getIsNegated())
                            .build();
                    return roleRecordRuleMapper.toEntity(rr);
                })
                .collect(Collectors.toList());
        roleRecordRuleEntityRepository.saveAll(entities);
    }

    private void saveRoleMenus(Set<String> menuIds, Role role) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        RoleEntity roleEntity = RoleEntity.builder().id(role.getId()).build();
        List<RoleMenuEntity> entities = menuIds.stream()
                .map(menuId -> {
                    Long decodedMenuId = idEncoder.decode(menuId);
                    MenuEntity menuEntity = MenuEntity.builder().id(decodedMenuId).build();
                    return RoleMenuEntity.builder()
                            .role(roleEntity)
                            .menu(menuEntity)
                            .build();
                })
                .collect(Collectors.toList());
        roleMenuEntityRepository.saveAll(entities);
    }

    // ── Validation + batch fetch (single pass) ───────────

    /**
     * Holds pre-fetched reference data so we don't query twice.
     */
    private static class ResolvedRefs {
        final Map<Long, Action> actionMap;
        final Map<Long, Resource> resourceMap;
        final Map<Long, RecordRule> recordRuleMap;

        ResolvedRefs(Map<Long, Action> actionMap,
                Map<Long, Resource> resourceMap,
                Map<Long, RecordRule> recordRuleMap) {
            this.actionMap = actionMap;
            this.resourceMap = resourceMap;
            this.recordRuleMap = recordRuleMap;
        }
    }

    /**
     * Batch-fetch all referenced entities, validate they exist,
     * and return the maps for reuse by save methods.
     */
    private ResolvedRefs validateAndResolveIds(List<PermissionRequest> permissionRequests,
            List<RoleRecordRuleRequest> recordRuleRequests, Set<String> menuIds) {
        BusinessException.Collector collector = new BusinessException.Collector();

        Map<Long, Action> actionMap = Collections.emptyMap();
        Map<Long, Resource> resourceMap = Collections.emptyMap();
        Map<Long, RecordRule> recordRuleMap = Collections.emptyMap();

        if (permissionRequests != null && !permissionRequests.isEmpty()) {
            Set<Long> actionIds = permissionRequests.stream()
                    .map(req -> idEncoder.decode(req.getActionId()))
                    .collect(Collectors.toSet());
            Set<Long> resourceIds = permissionRequests.stream()
                    .map(req -> idEncoder.decode(req.getResourceId()))
                    .collect(Collectors.toSet());

            actionMap = batchFetch(actionRepositoryPort, actionIds);
            resourceMap = batchFetch(resourceRepositoryPort, resourceIds);

            collectMissing(actionIds, actionMap, "actionId", collector);
            collectMissing(resourceIds, resourceMap, "resourceId", collector);
        }

        if (recordRuleRequests != null && !recordRuleRequests.isEmpty()) {
            Set<Long> ruleIds = recordRuleRequests.stream()
                    .map(req -> idEncoder.decode(req.getRecordRuleId()))
                    .collect(Collectors.toSet());

            recordRuleMap = batchFetch(recordRuleRepositoryPort, ruleIds);

            collectMissing(ruleIds, recordRuleMap, "recordRuleId", collector);
        }

        if (menuIds != null && !menuIds.isEmpty()) {
            Set<Long> decodedMenuIds = menuIds.stream()
                    .map(idEncoder::decode)
                    .collect(Collectors.toSet());

            Map<Long, Menu> menuMap = batchFetch(menuRepositoryPort, decodedMenuIds);

            collectMissing(decodedMenuIds, menuMap, "menuId", collector);
        }

        collector.validate();

        return new ResolvedRefs(actionMap, resourceMap, recordRuleMap);
    }

    // ── Generic helpers ──────────────────────────────────

    private <T extends BaseModel> Map<Long, T> batchFetch(
            BaseRepositoryPort<T> port, Set<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        SimpleFilter inFilter = SimpleFilter.builder()
                .field("id")
                .operator(SimpleFilter.FilterOperator.IN)
                .value(ids)
                .build();
        List<T> results = port.findAll(inFilter, Collections.<SortOrder>emptyList());
        return results.stream()
                .collect(Collectors.toMap(T::getId, Function.identity()));
    }

    private <T> void collectMissing(Set<Long> requestedIds, Map<Long, T> foundMap,
            String fieldName, BusinessException.Collector collector) {
        requestedIds.stream()
                .filter(id -> !foundMap.containsKey(id))
                .map(idEncoder::encode)
                .forEach(encodedId -> collector.add("Invalid " + fieldName + ": " + encodedId));
    }

    private Long getIdFromResponse(RoleDetailResponse response) {
        return idEncoder.decode(response.getId());
    }
}
