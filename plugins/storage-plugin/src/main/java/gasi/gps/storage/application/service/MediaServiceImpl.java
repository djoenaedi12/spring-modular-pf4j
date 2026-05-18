package gasi.gps.storage.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.storage.FileStorageProvider;
import gasi.gps.core.api.storage.FileStorageProvider.FileStoreCommand;
import gasi.gps.core.api.storage.FileStorageService;
import gasi.gps.storage.domain.model.Media;
import gasi.gps.storage.domain.port.inbound.MediaService;
import gasi.gps.storage.domain.port.outbound.MediaRepositoryPort;
import gasi.gps.storage.infrastructure.storage.StorageProviderRegistry;
import gasi.gps.storage.infrastructure.storage.StorageProviderResolver;

/**
 * Default implementation of {@link MediaService} / {@link FileStorageService}.
 *
 * <p>Provides both raw storage operations (no metadata tracking) and
 * managed operations (with persistence in the {@code storage_medias}
 * table).</p>
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class MediaServiceImpl implements MediaService {

    private final StorageProviderResolver resolver;
    private final StorageProviderRegistry registry;
    private final MediaRepositoryPort mediaRepo;

    /**
     * Creates the service.
     *
     * @param resolver  provider resolver
     * @param registry  provider registry
     * @param mediaRepo media repository port
     */
    public MediaServiceImpl(StorageProviderResolver resolver,
            StorageProviderRegistry registry,
            MediaRepositoryPort mediaRepo) {
        this.resolver = resolver;
        this.registry = registry;
        this.mediaRepo = mediaRepo;
    }

    // ── Raw storage (no metadata tracking) ────────────────────────────

    @Override
    public StoreResult store(StoreCommand command) {
        FileStorageProvider provider = resolveProvider(command.resource());

        String fileKey = UUID.randomUUID().toString();
        FileStorageProvider.StoreResult result = provider.store(new FileStoreCommand(
                fileKey, command.originalName(), command.contentType(),
                command.fileSize(), command.inputStream(), command.resource()));

        return new StoreResult(result.storagePath(), result.checksum(), result.storedSize());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FileContent> load(String resource, String storagePath) {
        FileStorageProvider provider = resolveProvider(resource);
        return provider.retrieve(storagePath)
                .map(fc -> new FileContent(
                        fc.inputStream(), fc.contentType(), fc.contentLength()));
    }

    @Override
    public void remove(String resource, String storagePath) {
        FileStorageProvider provider = resolveProvider(resource);
        provider.delete(storagePath);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String resource, String storagePath) {
        FileStorageProvider provider = resolveProvider(resource);
        return provider.exists(storagePath);
    }

    // ── Managed storage (with metadata tracking) ──────────────────────

    @Override
    public FileInfo upload(UploadCommand command) {
        Long providerId = resolver.resolveProviderId(command.resource());
        FileStorageProvider provider = registry.getProvider(providerId);

        String fileKey = UUID.randomUUID().toString();
        FileStorageProvider.StoreResult result = provider.store(new FileStoreCommand(
                fileKey, command.originalName(), command.contentType(),
                command.fileSize(), command.inputStream(), command.resource()));

        Media media = Media.builder()
                .fileKey(fileKey)
                .originalName(command.originalName())
                .contentType(command.contentType())
                .fileSize(result.storedSize())
                .storagePath(result.storagePath())
                .providerId(providerId)
                .checksum(result.checksum())
                .resource(command.resource())
                .resourceId(command.resourceId())
                .build();

        Media saved = mediaRepo.save(media);
        return toFileInfo(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadResult download(String fileKey) {
        Media media = mediaRepo.findByFileKey(fileKey)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Media not found: " + fileKey));

        FileStorageProvider provider = registry.getProvider(media.getProviderId());
        FileStorageProvider.FileContent content = provider.retrieve(media.getStoragePath())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Physical file missing for key: " + fileKey));

        return new DownloadResult(
                media.getOriginalName(),
                content.contentType(),
                content.contentLength(),
                content.inputStream());
    }

    @Override
    public void delete(String fileKey) {
        Media media = mediaRepo.findByFileKey(fileKey)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Media not found: " + fileKey));

        FileStorageProvider provider = registry.getProvider(media.getProviderId());
        provider.delete(media.getStoragePath());
        mediaRepo.delete(media.getId());
    }

    @Override
    public void attach(String fileKey, Long resourceId) {
        Media media = mediaRepo.findByFileKey(fileKey)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Media not found: " + fileKey));

        media.setResourceId(resourceId);
        mediaRepo.save(media);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileInfo> findByOwner(String resource, Long resourceId) {
        return mediaRepo.findByResourceAndResourceId(resource, resourceId)
                .stream()
                .map(this::toFileInfo)
                .toList();
    }

    // ── Internal helpers ──────────────────────────────────────────────

    private FileStorageProvider resolveProvider(String resource) {
        Long providerId = resolver.resolveProviderId(resource);
        return registry.getProvider(providerId);
    }

    private FileInfo toFileInfo(Media media) {
        return new FileInfo(
                media.getFileKey(),
                media.getOriginalName(),
                media.getContentType(),
                media.getFileSize(),
                media.getChecksum(),
                media.getResource(),
                media.getResourceId());
    }
}
