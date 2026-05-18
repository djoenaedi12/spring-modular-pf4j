package gasi.gps.core.api.file;

import java.io.InputStream;
import java.util.Map;

/**
 * Generic input for reading an uploaded/imported file.
 *
 * @since 1.0.0
 */
public interface FileReadInput {

    /**
     * Original source filename.
     *
     * @return original filename
     */
    String originalName();

    /**
     * Source content type.
     *
     * @return content type
     */
    String contentType();

    /**
     * File size in bytes.
     *
     * @return file size
     */
    long fileSize();

    /**
     * File content stream.
     *
     * @return input stream
     */
    InputStream inputStream();

    /**
     * Additional read parameters.
     *
     * @return parameters
     */
    Map<String, String> parameters();

    /**
     * Returns the lowercase file extension without the leading dot.
     *
     * @return normalized file extension, or an empty string when unavailable
     */
    default String extension() {
        String name = originalName();
        if (name == null || name.isBlank()) {
            return "";
        }
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return "";
        }
        return name.substring(dotIndex + 1).trim().toLowerCase();
    }
}
