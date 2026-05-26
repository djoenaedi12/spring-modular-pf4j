package gasi.gps.dataupload.domain.model;

/**
 * Downloadable upload template content.
 *
 * @param fileName    suggested file name for the downloaded template
 * @param contentType MIME type of the template content
 * @param content     raw template bytes
 */
public record DataUplTemplate(String fileName, String contentType, byte[] content) {
}
