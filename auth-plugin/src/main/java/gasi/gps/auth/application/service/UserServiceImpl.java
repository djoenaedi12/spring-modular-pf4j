package gasi.gps.auth.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gasi.gps.auth.application.dto.UserCreateRequest;
import gasi.gps.auth.application.dto.UserDetailResponse;
import gasi.gps.auth.application.dto.UserSummaryResponse;
import gasi.gps.auth.application.dto.UserUpdateRequest;
import gasi.gps.auth.application.mapper.UserDtoMapper;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.domain.model.User;
import gasi.gps.auth.domain.port.inbound.UserService;
import gasi.gps.auth.domain.port.outbound.RoleRepositoryPort;
import gasi.gps.auth.domain.port.outbound.UserRepositoryPort;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Service
public class UserServiceImpl extends
        BaseServiceImpl<User, UserCreateRequest, UserUpdateRequest, UserSummaryResponse, UserDetailResponse>
        implements UserService {

    private final UserRepositoryPort userRepository;
    private final UserDtoMapper userMapper;
    private final RoleRepositoryPort roleRepository;

    public UserServiceImpl(UserRepositoryPort repository,
            UserDtoMapper mapper,
            RoleRepositoryPort roleRepository,
            MessageUtil messageUtil,
            IdEncoder idEncoder) {
        super(repository, mapper, messageUtil, idEncoder);
        this.userRepository = repository;
        this.userMapper = mapper;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetailResponse create(UserCreateRequest request) {
        validateCreate(request);

        User domain = userMapper.toCreateDomain(request);
        domain.setRoles(resolveRoles(request.getRoleIds()));

        User saved = userRepository.save(domain);
        return userMapper.toDetail(saved);
    }

    @Override
    public UserDetailResponse update(Long id, UserUpdateRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));

        validateUpdate(id, request);
        userMapper.updateDomain(request, existing);
        existing.setRoles(resolveRoles(request.getRoleIds()));

        User saved = userRepository.save(existing);
        return userMapper.toDetail(saved);
    }

    private Set<Role> resolveRoles(Set<String> hashRoleIds) {
        if (hashRoleIds == null || hashRoleIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Long> roleIds = hashRoleIds.stream()
                .map(idEncoder::decode)
                .collect(Collectors.toSet());

        GenericFilter roleIdsFilter = SimpleFilter.builder()
                .field("id")
                .operator(SimpleFilter.FilterOperator.IN)
                .value(roleIds)
                .build();

        Set<Role> roles = roleRepository.findAll(roleIdsFilter, Collections.emptyList())
                .stream()
                .collect(Collectors.toSet());

        if (roles.size() != roleIds.size()) {
            Set<Long> foundIds = roles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());
            List<Long> missingIds = roleIds.stream()
                    .filter(roleId -> !foundIds.contains(roleId))
                    .toList();
            throw new BusinessException("Invalid roleIds: " + missingIds);
        }

        return roles;
    }

    @Override
    protected String resourceType() {
        return "User";
    }
}
