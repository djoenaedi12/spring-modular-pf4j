package gasi.gps.core.api.domain.model;

import java.io.InputStream;
import java.util.Map;

import gasi.gps.core.api.file.FileReadInput;

/**
 * Command payload for a data upload request.
 *
 * @param originalName original uploaded filename
 * @param contentType  uploaded file content type
 * @param fileSize     uploaded file size in bytes
 * @param inputStream  uploaded file stream
 * @param resource     resource code from the API path
 * @param parameters   extra upload parameters sent by the client
 * @since 1.0.0
 */
public record DataUplInput(
        String originalName,
        String contentType,
        long fileSize,
        InputStream inputStream,
        String resource,
        Map<String, String> parameters) implements FileReadInput {
}
