package gasi.gps.auth.application.service;

import org.springframework.stereotype.Service;

import gasi.gps.auth.application.dto.RecordRuleCreateRequest;
import gasi.gps.auth.application.dto.RecordRuleDetailResponse;
import gasi.gps.auth.application.dto.RecordRuleSummaryResponse;
import gasi.gps.auth.application.dto.RecordRuleUpdateRequest;
import gasi.gps.auth.application.mapper.RecordRuleDtoMapper;
import gasi.gps.auth.domain.model.RecordRule;
import gasi.gps.auth.domain.model.Resource;
import gasi.gps.auth.domain.port.inbound.RecordRuleService;
import gasi.gps.auth.domain.port.outbound.RecordRuleRepositoryPort;
import gasi.gps.auth.domain.port.outbound.ResourceRepositoryPort;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Service
public class RecordRuleServiceImpl extends
        BaseServiceImpl<RecordRule, RecordRuleCreateRequest, RecordRuleUpdateRequest, RecordRuleSummaryResponse, RecordRuleDetailResponse>
        implements RecordRuleService {

    private final RecordRuleRepositoryPort recordRuleRepositoryPort;
    private final RecordRuleDtoMapper recordRuleMapper;
    private final ResourceRepositoryPort resourceRepositoryPort;
    private final IdEncoder idEncoder;

    public RecordRuleServiceImpl(RecordRuleRepositoryPort repositoryPort,
            RecordRuleDtoMapper mapper,
            MessageUtil messageUtil,
            ResourceRepositoryPort resourceRepositoryPort,
            IdEncoder idEncoder) {
        super(repositoryPort, mapper, messageUtil);
        this.recordRuleRepositoryPort = repositoryPort;
        this.recordRuleMapper = mapper;
        this.resourceRepositoryPort = resourceRepositoryPort;
        this.idEncoder = idEncoder;
    }

    @Override
    public RecordRuleDetailResponse create(RecordRuleCreateRequest request) {
        validateCreate(request);
        RecordRule domain = recordRuleMapper.toCreateDomain(request);
        domain.setResource(resolveResource(request.getResourceId()));
        RecordRule saved = recordRuleRepositoryPort.save(domain);
        return recordRuleMapper.toDetail(saved);
    }

    @Override
    public RecordRuleDetailResponse update(Long id, RecordRuleUpdateRequest request) {
        RecordRule existing = recordRuleRepositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), id)));
        validateUpdate(id, request);
        recordRuleMapper.updateDomain(request, existing);
        existing.setResource(resolveResource(request.getResourceId()));
        RecordRule saved = recordRuleRepositoryPort.save(existing);
        return recordRuleMapper.toDetail(saved);
    }

    private Resource resolveResource(String resourceId) {
        if (resourceId == null) {
            return null;
        }
        Long decodedId = idEncoder.decode(resourceId);
        return resourceRepositoryPort.findById(decodedId)
                .orElseThrow(() -> new BusinessException("Invalid resourceId: " + resourceId));
    }

    @Override
    protected String resourceType() {
        return "RecordRule";
    }
}
