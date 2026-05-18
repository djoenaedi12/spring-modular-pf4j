package gasi.gps.core.api.file;

import java.util.List;
import java.util.Set;

/**
 * Reader for one or more file formats.
 *
 * <p>
 * Implementations declare supported extensions. The shared registry selects
 * the reader automatically from {@link FileReadInput#extension()}.
 * </p>
 *
 * @since 1.0.0
 */
public interface FileReader {

    /**
     * Supported file extensions without leading dots.
     *
     * @return supported extensions, for example {@code csv} or {@code xlsx}
     */
    Set<String> extensions();

    /**
     * Reads a file into generic rows.
     *
     * @param command file read command
     * @return parsed rows
     */
    List<FileRow> read(FileReadInput command);
}
