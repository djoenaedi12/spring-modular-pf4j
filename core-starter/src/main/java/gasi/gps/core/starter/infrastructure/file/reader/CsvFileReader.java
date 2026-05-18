package gasi.gps.core.starter.infrastructure.file.reader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.file.FileReadCommand;
import gasi.gps.core.api.file.FileReader;
import gasi.gps.core.api.file.FileRow;

/**
 * CSV file reader with header-based column mapping.
 *
 * @since 1.0.0
 */
@Component
public class CsvFileReader implements FileReader {

    @Override
    public Set<String> extensions() {
        return Set.of("csv");
    }

    @Override
    public List<FileRow> read(FileReadCommand command) {
        if (command == null || command.inputStream() == null) {
            throw new BusinessException("File is required");
        }

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get();

        try (InputStreamReader reader = new InputStreamReader(command.inputStream(), StandardCharsets.UTF_8);
                CSVParser parser = format.parse(reader)) {
            List<String> headers = parser.getHeaderNames();
            List<FileRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                rows.add(new FileRow(
                        Math.toIntExact(record.getRecordNumber() + 1),
                        toValues(headers, record),
                        record.toString()));
            }
            return rows;
        } catch (IOException ex) {
            throw new BusinessException("Failed to read file: " + ex.getMessage());
        }
    }

    private Map<String, String> toValues(List<String> headers, CSVRecord record) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int index = 0; index < headers.size(); index++) {
            String header = headers.get(index);
            String value = index < record.size() ? record.get(index) : "";
            values.put(header, value);
        }
        return Collections.unmodifiableMap(values);
    }
}
