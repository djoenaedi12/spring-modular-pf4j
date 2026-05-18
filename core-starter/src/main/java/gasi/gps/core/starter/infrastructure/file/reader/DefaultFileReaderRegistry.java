package gasi.gps.core.starter.infrastructure.file.reader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.file.FileReadCommand;
import gasi.gps.core.api.file.FileReader;
import gasi.gps.core.api.file.FileReaderRegistry;
import gasi.gps.core.api.file.FileRow;

/**
 * Default registry that resolves file readers by extension.
 *
 * @since 1.0.0
 */
@Component
public class DefaultFileReaderRegistry implements FileReaderRegistry {

    private final Map<String, FileReader> readers;

    /**
     * Creates a registry from all reader beans.
     *
     * @param readers file readers contributed by modules
     */
    public DefaultFileReaderRegistry(List<FileReader> readers) {
        this.readers = readers.stream()
                .flatMap(reader -> reader.extensions().stream()
                        .map(extension -> Map.entry(normalize(extension), reader)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        this::duplicateReader));
    }

    @Override
    public FileReader get(FileReadCommand command) {
        String extension = command == null ? "" : command.extension();
        FileReader reader = readers.get(normalize(extension));
        if (reader == null) {
            throw new BusinessException("Unsupported file extension: " + extension);
        }
        return reader;
    }

    @Override
    public List<FileRow> read(FileReadCommand command) {
        return get(command).read(command);
    }

    private FileReader duplicateReader(FileReader first, FileReader second) {
        throw new BusinessException("Duplicate file reader extension");
    }

    private String normalize(String extension) {
        if (extension == null) {
            return "";
        }
        String value = extension.trim().toLowerCase();
        return value.startsWith(".") ? value.substring(1) : value;
    }
}
