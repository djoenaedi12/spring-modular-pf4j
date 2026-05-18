package gasi.gps.core.api.storage;

import java.io.InputStream;
import java.util.Optional;

/**
 * Contract for a single storage backend instance.
 *
 * <p>Implementations are created by {@link FileStorageProviderFactory}
 * at runtime using configuration stored in the database. Multiple
 * provider instances of the same or different types can be active
 * simultaneously.</p>
 *
 * @since 1.0.0
 */
public interface FileStorageProvider {

    /**
     * Stores file content and returns the result.
     *
     * @param command file storage command containing stream and metadata
     * @return result with internal storage path and checksum
     */
    StoreResult store(FileStoreCommand command);

    /**
     * Retrieves file content by internal storage path.
     *
     * @param storagePath internal path assigned during storage
     * @return file content, or empty if not found
     */
    Optional<FileContent> retrieve(String storagePath);

    /**
     * Deletes file content by internal storage path.
     *
     * @param storagePath internal path assigned during storage
     */
    void delete(String storagePath);

    /**
     * Checks whether a file exists at the given storage path.
     *
     * @param storagePath internal path assigned during storage
     * @return {@code true} if the file exists
     */
    boolean exists(String storagePath);

    /**
     * Immutable result of a store operation.
     *
     * @param storagePath internal path within the backend
     * @param checksum    SHA-256 hex digest
     * @param storedSize  actual stored size in bytes
     */
    record StoreResult(String storagePath, String checksum, long storedSize) { }

    /**
     * Immutable file content descriptor returned on retrieval.
     *
     * @param inputStream   content stream — caller must close
     * @param contentType   MIME content type
     * @param contentLength content length in bytes
     */
    record FileContent(InputStream inputStream, String contentType, long contentLength) { }

    /**
     * Immutable command describing a file to store.
     *
     * @param fileKey      pre-generated unique key (UUID)
     * @param originalName original filename from the client
     * @param contentType  MIME content type
     * @param fileSize     declared file size in bytes
     * @param inputStream  content stream
     * @param resource     resource type for path structuring
     */
    record FileStoreCommand(
            String fileKey,
            String originalName,
            String contentType,
            long fileSize,
            InputStream inputStream,
            String resource) { }
}
