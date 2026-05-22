package gasi.gps.core.starter.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.dto.DataRowUplDetailResponse;
import gasi.gps.core.api.application.dto.DataRowUplSummaryResponse;
import gasi.gps.core.api.application.dto.DataUplDetailResponse;
import gasi.gps.core.api.application.dto.DataUplSummaryResponse;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.domain.model.AndFilter;
import gasi.gps.core.api.domain.model.DataRowUpl;
import gasi.gps.core.api.domain.model.DataUpl;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.model.UploadRowStatus;
import gasi.gps.core.api.domain.model.UploadStatus;
import gasi.gps.core.api.domain.port.inbound.BaseUplService;
import gasi.gps.core.api.domain.port.inbound.DataUplProcessor;
import gasi.gps.core.api.domain.port.outbound.DataRowUplRepositoryPort;
import gasi.gps.core.api.domain.port.outbound.DataUplRepositoryPort;
import gasi.gps.core.api.file.FileReadInput;
import gasi.gps.core.api.file.FileReaderRegistry;
import gasi.gps.core.api.file.FileRow;
import gasi.gps.core.starter.application.mapper.DataRowUplDtoMapper;
import gasi.gps.core.starter.application.mapper.DataUplDtoMapper;
import gasi.gps.core.starter.infrastructure.i18n.MessageUtil;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Base implementation for shared upload workflows.
 *
 * <p>
 * This service owns upload persistence and status transitions. Resource
 * specific parsing, validation, and commit behavior is delegated to
 * {@link DataUplProcessor} implementations.
 * </p>
 *
 * @since 1.0.0
 */
@Transactional
public abstract class BaseUplServiceImpl
        extends BaseReadServiceImpl<DataUpl, DataUplSummaryResponse, DataUplDetailResponse>
        implements BaseUplService {

    private static final List<UploadStatus> DISCARDABLE_STATUSES = List.of(
            UploadStatus.UPLOADED,
            UploadStatus.VALIDATED,
            UploadStatus.FAILED,
            UploadStatus.REJECTED);

    private final DataUplRepositoryPort dataUplRepositoryPort;
    private final DataRowUplRepositoryPort dataRowUplRepositoryPort;
    private final DataUplDtoMapper dataUplDtoMapper;
    private final DataRowUplDtoMapper dataRowUplDtoMapper;
    private final DataUplProcessorRegistry processorRegistry;
    private final FileReaderRegistry fileReaderRegistry;

    protected BaseUplServiceImpl(DataUplRepositoryPort dataUplRepositoryPort,
            DataRowUplRepositoryPort dataRowUplRepositoryPort,
            DataUplDtoMapper dataUplDtoMapper,
            DataRowUplDtoMapper dataRowUplDtoMapper,
            DataUplProcessorRegistry processorRegistry,
            FileReaderRegistry fileReaderRegistry,
            MessageUtil messageUtil,
            IdEncoder idEncoder) {
        super(dataUplRepositoryPort, dataUplDtoMapper, messageUtil, idEncoder);
        this.dataUplRepositoryPort = dataUplRepositoryPort;
        this.dataRowUplRepositoryPort = dataRowUplRepositoryPort;
        this.dataUplDtoMapper = dataUplDtoMapper;
        this.dataRowUplDtoMapper = dataRowUplDtoMapper;
        this.processorRegistry = processorRegistry;
        this.fileReaderRegistry = fileReaderRegistry;
    }

    @Override
    public DataUplDetailResponse upload(String resource, FileReadInput file) {
        String normalizedResource = normalize(resource);
        DataUplProcessor processor = processorRegistry.get(normalizedResource);
        Map<String, String> params = file != null ? file.parameters() : Collections.emptyMap();

        DataUpl dataUpl = DataUpl.builder()
                .resource(normalizedResource)
                .fileName(file != null ? file.originalName() : null)
                .uploadStatus(UploadStatus.UPLOADING)
                .build();
        DataUpl saved = dataUplRepositoryPort.save(dataUpl);

        List<FileRow> fileRows = fileReaderRegistry.read(file);
        List<DataRowUpl> rows = processor.parse(fileRows, saved, params);
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
    public void validate(String resource, String id, Map<String, String> params) {
        DataUpl dataUpl = findUpload(resource, id);
        DataUplProcessor processor = processorRegistry.get(dataUpl.getResource());
        dataUpl.setUploadStatus(UploadStatus.VALIDATING);
        dataUplRepositoryPort.save(dataUpl);

        List<DataRowUpl> rows = findRows(dataUpl);
        List<DataRowUpl> validatedRows = processor.validateRows(dataUpl, rows, params);
        dataRowUplRepositoryPort.saveAll(validatedRows);

        long validCount = validatedRows.stream()
                .filter(row -> row.getRowStatus() == UploadRowStatus.VALID)
                .count();
        long invalidCount = validatedRows.stream()
                .filter(row -> row.getRowStatus() == UploadRowStatus.INVALID)
                .count();

        dataUpl.setValidRows((int) validCount);
        dataUpl.setInvalidRows((int) invalidCount);
        dataUpl.setUploadStatus(UploadStatus.VALIDATED);
        dataUplRepositoryPort.save(dataUpl);
    }

    @Override
    public void commit(String resource, String id, Map<String, String> params) {
        DataUpl dataUpl = findUpload(resource, id);
        DataUplProcessor processor = processorRegistry.get(dataUpl.getResource());
        List<DataRowUpl> rows = findRows(dataUpl);
        List<DataRowUpl> validRows = rows.stream()
                .filter(row -> row.getRowStatus() == UploadRowStatus.VALID)
                .toList();

        if (processor.requiresApproval(dataUpl, validRows, params)) {
            dataUpl.setUploadStatus(UploadStatus.PENDING_APPROVAL);
            dataUplRepositoryPort.save(dataUpl);
            return;
        }

        dataUpl.setUploadStatus(UploadStatus.COMMITTING);
        dataUplRepositoryPort.save(dataUpl);

        processor.commitRows(dataUpl, validRows, params);
        validRows.forEach(row -> row.setRowStatus(UploadRowStatus.COMMITTED));
        dataRowUplRepositoryPort.saveAll(validRows);

        dataUpl.setCommittedRows(validRows.size());
        dataUpl.setUploadStatus(UploadStatus.COMMITTED);
        dataUplRepositoryPort.save(dataUpl);
    }

    @Override
    public void discard(String resource, String id) {
        DataUpl dataUpl = findUpload(resource, id);

        if (!DISCARDABLE_STATUSES.contains(dataUpl.getUploadStatus())) {
            throw new BusinessException("Upload cannot be discarded in status: " + dataUpl.getUploadStatus());
        }

        dataRowUplRepositoryPort.deleteAllBy(rowFilter(dataUpl, null));
        dataUplRepositoryPort.delete(dataUpl.getId());
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
                page, size, rowFilter(dataUpl, filter), orders);
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
                .field("dataUpl.id")
                .operator(SimpleFilter.FilterOperator.EQUALS)
                .value(dataUpl.getId())
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
