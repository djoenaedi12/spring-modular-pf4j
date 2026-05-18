package gasi.gps.core.starter.application.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.dto.DataRowUplDetailResponse;
import gasi.gps.core.api.application.dto.DataRowUplSummaryResponse;
import gasi.gps.core.api.application.dto.DataUplDetailResponse;
import gasi.gps.core.api.application.dto.DataUplSummaryResponse;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.domain.model.AndFilter;
import gasi.gps.core.api.domain.model.DataRowUpl;
import gasi.gps.core.api.domain.model.DataUplCommand;
import gasi.gps.core.api.domain.model.DataUpl;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.model.UploadRowStatus;
import gasi.gps.core.api.domain.model.UploadStatus;
import gasi.gps.core.api.domain.port.inbound.DataUplProcessor;
import gasi.gps.core.api.domain.port.inbound.DataUplService;
import gasi.gps.core.api.domain.port.outbound.DataRowUplRepositoryPort;
import gasi.gps.core.api.domain.port.outbound.DataUplRepositoryPort;
import gasi.gps.core.starter.application.mapper.DataRowUplDtoMapper;
import gasi.gps.core.starter.application.mapper.DataUplDtoMapper;
import gasi.gps.core.starter.infrastructure.i18n.MessageUtil;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Shared upload workflow service.
 *
 * <p>This service owns upload persistence and status transitions. Resource
 * specific parsing, validation, and commit behavior is delegated to
 * {@link DataUplProcessor} implementations.</p>
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class DataUplServiceImpl
        extends BaseReadServiceImpl<DataUpl, DataUplSummaryResponse, DataUplDetailResponse>
        implements DataUplService {

    private final DataUplRepositoryPort dataUplRepositoryPort;
    private final DataRowUplRepositoryPort dataRowUplRepositoryPort;
    private final DataUplDtoMapper dataUplDtoMapper;
    private final DataRowUplDtoMapper dataRowUplDtoMapper;
    private final DataUplProcessorRegistry processorRegistry;

    /**
     * Creates the shared upload service.
     *
     * @param dataUplRepositoryPort    upload header repository
     * @param dataRowUplRepositoryPort upload row repository
     * @param dataUplDtoMapper         upload header response mapper
     * @param dataRowUplDtoMapper      upload row response mapper
     * @param processorRegistry        resource processor registry
     * @param messageUtil              localized message helper
     * @param idEncoder                public ID encoder
     */
    public DataUplServiceImpl(DataUplRepositoryPort dataUplRepositoryPort,
            DataRowUplRepositoryPort dataRowUplRepositoryPort,
            DataUplDtoMapper dataUplDtoMapper,
            DataRowUplDtoMapper dataRowUplDtoMapper,
            DataUplProcessorRegistry processorRegistry,
            MessageUtil messageUtil,
            IdEncoder idEncoder) {
        super(dataUplRepositoryPort, dataUplDtoMapper, messageUtil, idEncoder);
        this.dataUplRepositoryPort = dataUplRepositoryPort;
        this.dataRowUplRepositoryPort = dataRowUplRepositoryPort;
        this.dataUplDtoMapper = dataUplDtoMapper;
        this.dataRowUplDtoMapper = dataRowUplDtoMapper;
        this.processorRegistry = processorRegistry;
    }

    @Override
    public DataUplDetailResponse upload(String resource, DataUplCommand file) {
        String normalizedResource = normalize(resource);
        DataUplProcessor processor = processorRegistry.get(normalizedResource);

        DataUpl dataUpl = DataUpl.builder()
                .resource(normalizedResource)
                .fileName(file != null ? file.originalName() : null)
                .uploadStatus(UploadStatus.UPLOADING)
                .build();
        DataUpl saved = dataUplRepositoryPort.save(dataUpl);

        List<DataRowUpl> rows = processor.parse(file, saved);
        List<DataRowUpl> preparedRows = rows == null ? Collections.emptyList()
                : rows.stream()
                        .map(row -> prepareRow(saved, row))
                        .toList();
        dataRowUplRepositoryPort.saveAll(preparedRows);

        saved.setTotalRows(preparedRows.size());
        saved.setUploadStatus(UploadStatus.UPLOADED);
        return dataUplDtoMapper.toDetail(dataUplRepositoryPort.save(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public DataUplDetailResponse findById(String resource, String id) {
        return dataUplDtoMapper.toDetail(findUpload(resource, id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<DataUplSummaryResponse> findAll(String resource, int page, int size,
            GenericFilter filter, List<SortOrder> orders) {
        return super.findAll(page, size, resourceFilter(resource, filter), orders);
    }

    @Override
    public void validate(String resource, String id) {
        DataUpl dataUpl = findUpload(resource, id);
        DataUplProcessor processor = processorRegistry.get(dataUpl.getResource());
        dataUpl.setUploadStatus(UploadStatus.VALIDATING);
        dataUplRepositoryPort.save(dataUpl);

        List<DataRowUpl> rows = findRows(dataUpl);
        List<DataRowUpl> validatedRows = processor.validateRows(dataUpl, rows);
        dataRowUplRepositoryPort.saveAll(validatedRows);

        long validRows = validatedRows.stream()
                .filter(row -> row.getRowStatus() == UploadRowStatus.VALID)
                .count();
        long invalidRows = validatedRows.stream()
                .filter(row -> row.getRowStatus() == UploadRowStatus.INVALID)
                .count();

        dataUpl.setValidRows((int) validRows);
        dataUpl.setInvalidRows((int) invalidRows);
        dataUpl.setUploadStatus(UploadStatus.VALIDATED);
        dataUplRepositoryPort.save(dataUpl);
    }

    @Override
    public void commit(String resource, String id) {
        DataUpl dataUpl = findUpload(resource, id);
        DataUplProcessor processor = processorRegistry.get(dataUpl.getResource());
        List<DataRowUpl> rows = findRows(dataUpl);
        List<DataRowUpl> validRows = rows.stream()
                .filter(row -> row.getRowStatus() == UploadRowStatus.VALID)
                .toList();

        dataUpl.setUploadStatus(UploadStatus.COMMITTING);
        dataUplRepositoryPort.save(dataUpl);

        processor.commitRows(dataUpl, validRows);
        validRows.forEach(row -> row.setRowStatus(UploadRowStatus.COMMITTED));
        dataRowUplRepositoryPort.saveAll(validRows);

        dataUpl.setCommittedRows(validRows.size());
        dataUpl.setUploadStatus(UploadStatus.COMMITTED);
        dataUplRepositoryPort.save(dataUpl);
    }

    @Override
    @Transactional(readOnly = true)
    public DataRowUplDetailResponse findRowById(String resource, String uploadId, String rowId) {
        DataUpl dataUpl = findUpload(resource, uploadId);
        Long decodedRowId = idEncoder.decode(rowId);
        DataRowUpl row = dataRowUplRepositoryPort.findById(decodedRowId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", "DataRowUpl", rowId)));
        if (row.getDataUpl() == null || !dataUpl.getId().equals(row.getDataUpl().getId())) {
            throw new EntityNotFoundException(messageUtil.get("error.entity.notFound", "DataRowUpl", rowId));
        }
        return dataRowUplDtoMapper.toDetail(row);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<DataRowUplSummaryResponse> findRows(String resource, String uploadId, int page, int size,
            GenericFilter filter, List<SortOrder> orders) {
        DataUpl dataUpl = findUpload(resource, uploadId);
        PageResult<DataRowUpl> result = dataRowUplRepositoryPort.findAll(
                page,
                size,
                rowFilter(dataUpl, filter),
                orders);
        List<DataRowUplSummaryResponse> content = result.getContent().stream()
                .map(dataRowUplDtoMapper::toSummary)
                .toList();
        return PageResult.<DataRowUplSummaryResponse>builder()
                .content(content)
                .page(result.getPage())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Override
    protected String resourceType() {
        return "DataUpl";
    }

    private DataRowUpl prepareRow(DataUpl dataUpl, DataRowUpl row) {
        return row.toBuilder()
                .dataUpl(dataUpl)
                .rowStatus(row.getRowStatus() != null ? row.getRowStatus() : UploadRowStatus.RAW)
                .build();
    }

    private DataUpl findUpload(String resource, String encodedId) {
        String normalizedResource = normalize(resource);
        Long id = idEncoder.decode(encodedId);
        DataUpl dataUpl = dataUplRepositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", "DataUpl", encodedId)));
        if (!normalizedResource.equals(normalize(dataUpl.getResource()))) {
            throw new BusinessException("Upload does not belong to resource: " + resource);
        }
        return dataUpl;
    }

    private GenericFilter resourceFilter(String resource, GenericFilter filter) {
        SimpleFilter resourceFilter = SimpleFilter.builder()
                .field("resource")
                .operator(SimpleFilter.FilterOperator.EQUALS)
                .value(normalize(resource))
                .build();
        if (filter == null) {
            return resourceFilter;
        }
        return AndFilter.builder()
                .filters(List.of(resourceFilter, filter))
                .build();
    }

    private List<DataRowUpl> findRows(DataUpl dataUpl) {
        return dataRowUplRepositoryPort.findAll(rowFilter(dataUpl, null), Collections.emptyList());
    }

    private GenericFilter rowFilter(DataUpl dataUpl, GenericFilter filter) {
        SimpleFilter uploadFilter = SimpleFilter.builder()
                .field("dataUpl")
                .operator(SimpleFilter.FilterOperator.EQUALS)
                .value(dataUpl)
                .build();
        if (filter == null) {
            return uploadFilter;
        }
        return AndFilter.builder()
                .filters(List.of(uploadFilter, filter))
                .build();
    }

    private String normalize(String resource) {
        if (resource == null || resource.isBlank()) {
            throw new BusinessException("Upload resource is required");
        }
        return resource.trim().toLowerCase();
    }
}
