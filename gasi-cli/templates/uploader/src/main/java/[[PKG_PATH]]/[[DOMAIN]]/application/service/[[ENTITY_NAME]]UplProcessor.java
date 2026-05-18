package {{PACKAGE_NAME}}.application.service;

import java.util.List;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.domain.model.DataRowUpl;
import gasi.gps.core.api.domain.model.DataUpl;
import gasi.gps.core.api.domain.model.DataUplCommand;
import gasi.gps.core.api.domain.model.UploadRowStatus;
import gasi.gps.core.api.domain.port.inbound.DataUplProcessor;
import gasi.gps.core.api.file.FileReaderRegistry;

/**
 * Upload processor for {{ENTITY_NAME}} data.
 *
 * @since 1.0.0
 */
@Component
public class {{ENTITY_NAME}}UplProcessor implements DataUplProcessor {

    private final FileReaderRegistry fileReaderRegistry;

    /**
     * Creates the upload processor.
     *
     * @param fileReaderRegistry file reader registry
     */
    public {{ENTITY_NAME}}UplProcessor(FileReaderRegistry fileReaderRegistry) {
        this.fileReaderRegistry = fileReaderRegistry;
    }

    @Override
    public String resource() {
        return "{{RESOURCE_NAME}}";
    }

    @Override
    public List<DataRowUpl> parse(DataUplCommand upload, DataUpl dataUpl) {
        return fileReaderRegistry.read(upload).stream()
                .map(row -> DataRowUpl.builder()
                        .dataUpl(dataUpl)
                        .rowNumber(row.rowNumber())
                        .rowData(row.rawData())
                        .lookupValue1(row.values().get("lookupValue1"))
                        .lookupValue2(row.values().get("lookupValue2"))
                        .lookupValue3(row.values().get("lookupValue3"))
                        .rowStatus(UploadRowStatus.RAW)
                        .build())
                .toList();
    }

    @Override
    public List<DataRowUpl> validateRows(DataUpl dataUpl, List<DataRowUpl> rows) {
        rows.forEach(row -> {
            row.setRowStatus(UploadRowStatus.VALID);
            row.setErrorMessage(null);
        });
        return rows;
    }

    @Override
    public void commitRows(DataUpl dataUpl, List<DataRowUpl> rows) {
        // TODO: Insert or update {{ENTITY_NAME}} data from valid upload rows.
    }
}
