package gasi.gps.dataupload.application.service;

import org.springframework.stereotype.Service;

import gasi.gps.dataupload.domain.port.inbound.DataUplService;
import gasi.gps.dataupload.domain.port.outbound.DataRowUplRepositoryPort;
import gasi.gps.dataupload.domain.port.outbound.DataUplRepositoryPort;
import gasi.gps.core.api.file.FileReaderRegistry;
import gasi.gps.dataupload.application.mapper.DataRowUplDtoMapper;
import gasi.gps.dataupload.application.mapper.DataUplDtoMapper;
import gasi.gps.core.starter.infrastructure.i18n.MessageUtil;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Default upload service implementation.
 *
 * @since 1.0.0
 */
@Service
public class DataUplServiceImpl extends BaseUplServiceImpl implements DataUplService {

    /**
     * Creates the default upload service.
     *
     * @param dataUplRepositoryPort    upload header repository
     * @param dataRowUplRepositoryPort upload row repository
     * @param dataUplDtoMapper         upload header response mapper
     * @param dataRowUplDtoMapper      upload row response mapper
     * @param processorRegistry        resource processor registry
     * @param fileReaderRegistry       file reader registry
     * @param messageUtil              localized message helper
     * @param idEncoder                public ID encoder
     */
    public DataUplServiceImpl(DataUplRepositoryPort dataUplRepositoryPort,
            DataRowUplRepositoryPort dataRowUplRepositoryPort,
            DataUplDtoMapper dataUplDtoMapper,
            DataRowUplDtoMapper dataRowUplDtoMapper,
            DataUplProcessorRegistry processorRegistry,
            FileReaderRegistry fileReaderRegistry,
            MessageUtil messageUtil,
            IdEncoder idEncoder) {
        super(dataUplRepositoryPort, dataRowUplRepositoryPort, dataUplDtoMapper,
                dataRowUplDtoMapper, processorRegistry, fileReaderRegistry,
                messageUtil, idEncoder);
    }
}
