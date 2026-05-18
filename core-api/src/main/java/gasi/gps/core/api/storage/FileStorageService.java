package gasi.gps.core.api.storage;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Generic service contract for file storage operations.
 *
 * <p>Provides two modes of operation:</p>
 * <ul>
 *   <li><b>Raw storage</b> ({@link #store}, {@link #load}, {@link #remove},
 *       {@link #exists}) — direct file operations without metadata tracking.
 *       The caller is responsible for managing storage paths.</li>
 *   <li><b>Managed storage</b> ({@link #upload}, {@link #download},
 *       {@link #delete}, {@link #attach}, {@link #findByOwner}) — file
 *       operations with automatic metadata persistence and file-key based
 *       lookup.</li>
 * </ul>
 *
 * <p>Both modes use the {@code resource} parameter for storage provider
 * routing via database configuration.</p>
 *
 * @since 1.0.0
 */
public interface FileStorageService {

    // ── Raw storage (no metadata tracking) ────────────────────────────

    /**
     * Stores a file without metadata tracking.
     *
     * <p>The caller is responsible for remembering the returned
     * {@link StoreResult#storagePath()} for later retrieval or deletion.</p>
     *
     * @param command store command with file data and resource for routing
     * @return result with storage path and checksum
     */
    StoreResult store(StoreCommand command);

    /**
     * Loads a file directly from storage without metadata lookup.
     *
     * @param resource    resource type for provider routing
     * @param storagePath internal storage path from a previous {@link #store}
     * @return file content, or empty if not found
     */
    Optional<FileContent> load(String resource, String storagePath);

    /**
     * Removes a file directly from storage without metadata cleanup.
     *
     * @param resource    resource type for provider routing
     * @param storagePath internal storage path from a previous {@link #store}
     */
    void remove(String resource, String storagePath);

    /**
     * Checks whether a file exists at the given storage path.
     *
     * @param resource    resource type for provider routing
     * @param storagePath internal storage path from a previous {@link #store}
     * @return {@code true} if the file exists
     */
    boolean exists(String resource, String storagePath);

    // ── Managed storage (with metadata tracking) ──────────────────────

    /**
     * Uploads a file with automatic metadata persistence.
     *
     * <p>The {@link UploadCommand#resourceId()} may be {@code null} when the
     * owning entity has not been created yet. Use {@link #attach} to link
     * the file later.</p>
     *
     * @param command upload command with file data and ownership info
     * @return file info with generated file key
     */
    FileInfo upload(UploadCommand command);

    /**
     * Downloads a managed file by its unique file key.
     *
     * @param fileKey unique file key returned from {@link #upload}
     * @return download result with stream and metadata
     */
    DownloadResult download(String fileKey);

    /**
     * Deletes a managed file by its unique file key.
     *
     * <p>Removes both the physical file and its metadata record.</p>
     *
     * @param fileKey unique file key
     */
    void delete(String fileKey);

    /**
     * Links a previously uploaded file to a resource owner.
     *
     * <p>Typical flow: upload file (no resourceId) → create entity →
     * attach file to entity using the returned ID.</p>
     *
     * @param fileKey    unique file key
     * @param resourceId resource owner identifier
     */
    void attach(String fileKey, Long resourceId);

    /**
     * Finds all managed files owned by a specific resource.
     *
     * @param resource   resource type
     * @param resourceId resource owner identifier
     * @return list of file info records
     */
    List<FileInfo> findByOwner(String resource, Long resourceId);

    // ── Records ───────────────────────────────────────────────────────

    /**
     * Command for raw file storage.
     *
     * @param originalName original filename from the client
     * @param contentType  MIME content type
     * @param fileSize     declared file size in bytes
     * @param inputStream  content stream
     * @param resource     resource type for provider routing
     */
    record StoreCommand(
            String originalName,
            String contentType,
            long fileSize,
            InputStream inputStream,
            String resource) { }

    /**
     * Result of a raw store operation.
     *
     * @param storagePath internal path within the storage backend
     * @param checksum    SHA-256 hex digest
     * @param storedSize  actual stored size in bytes
     */
    record StoreResult(String storagePath, String checksum, long storedSize) { }

    /**
     * Command for managed file upload.
     *
     * @param originalName original filename from the client
     * @param contentType  MIME content type
     * @param fileSize     declared file size in bytes
     * @param inputStream  content stream
     * @param resource     resource type for provider routing and ownership
     * @param resourceId   resource owner identifier, may be {@code null}
     */
    record UploadCommand(
            String originalName,
            String contentType,
            long fileSize,
            InputStream inputStream,
            String resource,
            Long resourceId) { }

    /**
     * Metadata for a managed file.
     *
     * @param fileKey      unique file key (UUID)
     * @param originalName original filename
     * @param contentType  MIME content type
     * @param fileSize     stored file size in bytes
     * @param checksum     SHA-256 hex digest
     * @param resource     resource type
     * @param resourceId   resource owner identifier, may be {@code null}
     */
    record FileInfo(
            String fileKey,
            String originalName,
            String contentType,
            long fileSize,
            String checksum,
            String resource,
            Long resourceId) { }

    /**
     * Raw file content returned from {@link #load}.
     *
     * @param inputStream   content stream — caller must close
     * @param contentType   MIME content type
     * @param contentLength content length in bytes
     */
    record FileContent(
            InputStream inputStream,
            String contentType,
            long contentLength) { }

    /**
     * Download result for managed files, includes original filename.
     *
     * @param originalName  original filename
     * @param contentType   MIME content type
     * @param contentLength content length in bytes
     * @param inputStream   content stream — caller must close
     */
    record DownloadResult(
            String originalName,
            String contentType,
            long contentLength,
            InputStream inputStream) { }
}
