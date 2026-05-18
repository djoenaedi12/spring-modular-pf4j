package gasi.gps.core.api.file;

import java.util.List;

/**
 * Registry that resolves file readers by file extension.
 *
 * @since 1.0.0
 */
public interface FileReaderRegistry {

    /**
     * Finds a reader for a file.
     *
     * @param command file read command
     * @return matching reader
     */
    FileReader get(FileReadCommand command);

    /**
     * Reads a file using the matching reader.
     *
     * @param command file read command
     * @return parsed rows
     */
    List<FileRow> read(FileReadCommand command);
}
