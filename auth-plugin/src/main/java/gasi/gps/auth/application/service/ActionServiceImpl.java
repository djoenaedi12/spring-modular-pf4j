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
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Service
public class ActionServiceImpl extends
        BaseServiceImpl<Action, ActionCreateRequest, ActionUpdateRequest, ActionSummaryResponse, ActionDetailResponse>
        implements ActionService {

    public ActionServiceImpl(ActionRepositoryPort repositoryPort,
            ActionDtoMapper mapper,
            MessageUtil messageUtil, IdEncoder idEncoder) {
        super(repositoryPort, mapper, messageUtil, idEncoder);
    }

    @Override
    protected String resourceType() {
        return "Action";
    }
}
