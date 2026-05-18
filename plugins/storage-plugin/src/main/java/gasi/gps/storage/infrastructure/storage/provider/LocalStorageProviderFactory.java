package gasi.gps.storage.infrastructure.storage.provider;

import java.security.DigestInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.pf4j.Extension;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.storage.FileStorageProvider;
import gasi.gps.core.api.storage.FileStorageProviderFactory;

/**
 * Factory for local filesystem storage providers.
 *
 * <p>Bundled with the media plugin as a default provider.
 * Additional provider types (MinIO, S3, etc.) can be added
 * as separate PF4J plugins.</p>
 *
 * @since 1.0.0
 */
@Extension
public class LocalStorageProviderFactory implements FileStorageProviderFactory {

    private static final String KEY_BASE_PATH = "basePath";

    @Override
    public String getProviderType() {
        return "LOCAL";
    }

    @Override
    public List<ConfigField> getConfigFields() {
        return List.of(
                new ConfigField(KEY_BASE_PATH, "Storage Base Path",
                        "STRING", true, "./uploads"));
    }

    @Override
    public void validate(Map<String, Object> config) {
        if (!config.containsKey(KEY_BASE_PATH)
                || config.get(KEY_BASE_PATH) == null
                || config.get(KEY_BASE_PATH).toString().isBlank()) {
            throw new BusinessException("LOCAL provider requires 'basePath' configuration");
        }
    }

    @Override
    public FileStorageProvider create(Map<String, Object> config) {
        String basePath = config.get(KEY_BASE_PATH).toString();
        return new LocalFileStorageProvider(basePath);
    }

    /**
     * Local filesystem storage provider implementation.
     */
    static class LocalFileStorageProvider implements FileStorageProvider {

        private static final DateTimeFormatter DATE_FMT =
                DateTimeFormatter.ofPattern("yyyy/MM/dd");

        private final Path basePath;

        LocalFileStorageProvider(String basePath) {
            this.basePath = Path.of(basePath);
        }

        @Override
        public StoreResult store(FileStoreCommand command) {
            try {
                String datePath = LocalDate.now().format(DATE_FMT);
                Path dir = basePath.resolve(command.resource()).resolve(datePath);
                Files.createDirectories(dir);

                String ext = extractExtension(command.originalName());
                Path target = dir.resolve(command.fileKey() + ext);

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try (DigestInputStream dis = new DigestInputStream(
                        command.inputStream(), digest)) {
                    Files.copy(dis, target, StandardCopyOption.REPLACE_EXISTING);
                }

                String checksum = HexFormat.of().formatHex(digest.digest());
                long storedSize = Files.size(target);

                return new StoreResult(target.toString(), checksum, storedSize);
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new BusinessException("Failed to store file locally: " + e.getMessage());
            }
        }

        @Override
        public Optional<FileContent> retrieve(String storagePath) {
            try {
                Path file = Path.of(storagePath);
                if (!Files.exists(file)) {
                    return Optional.empty();
                }
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                long size = Files.size(file);
                InputStream stream = Files.newInputStream(file);
                return Optional.of(new FileContent(stream, contentType, size));
            } catch (IOException e) {
                throw new BusinessException(
                        "Failed to retrieve file: " + e.getMessage());
            }
        }

        @Override
        public void delete(String storagePath) {
            try {
                Path file = Path.of(storagePath);
                Files.deleteIfExists(file);
            } catch (IOException e) {
                throw new BusinessException("Failed to delete file: " + e.getMessage());
            }
        }

        @Override
        public boolean exists(String storagePath) {
            return Files.exists(Path.of(storagePath));
        }

        private static String extractExtension(String filename) {
            if (filename == null) {
                return "";
            }
            int dot = filename.lastIndexOf('.');
            return dot >= 0 ? filename.substring(dot) : "";
        }
    }
}
