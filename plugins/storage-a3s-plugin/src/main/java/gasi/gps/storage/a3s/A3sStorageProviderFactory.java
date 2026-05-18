package gasi.gps.storage.a3s;

import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * PF4J extension that provides an S3-compatible storage provider.
 *
 * <p>Works with any S3-compatible service (AWS S3, MinIO, Ceph,
 * DigitalOcean Spaces, etc.) by setting the {@code endpoint} config
 * to the service URL.</p>
 *
 * <h2>Required config fields</h2>
 * <ul>
 *   <li>{@code endpoint}  — S3-compatible endpoint URL
 *       (e.g. {@code https://s3.amazonaws.com})</li>
 *   <li>{@code region}    — AWS region
 *       (e.g. {@code ap-southeast-1})</li>
 *   <li>{@code accessKey} — access key ID</li>
 *   <li>{@code secretKey} — secret access key</li>
 *   <li>{@code bucket}    — target bucket name</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Extension
public class A3sStorageProviderFactory implements FileStorageProviderFactory {

    private static final String KEY_ENDPOINT = "endpoint";
    private static final String KEY_REGION = "region";
    private static final String KEY_ACCESS_KEY = "accessKey";
    private static final String KEY_SECRET_KEY = "secretKey";
    private static final String KEY_BUCKET = "bucket";

    @Override
    public String getProviderType() {
        return "A3S";
    }

    @Override
    public List<ConfigField> getConfigFields() {
        return List.of(
                new ConfigField(KEY_ENDPOINT, "S3 Endpoint URL",
                        "STRING", true, "https://s3.amazonaws.com"),
                new ConfigField(KEY_REGION, "Region",
                        "STRING", true, "ap-southeast-1"),
                new ConfigField(KEY_ACCESS_KEY, "Access Key",
                        "SECRET", true, null),
                new ConfigField(KEY_SECRET_KEY, "Secret Key",
                        "SECRET", true, null),
                new ConfigField(KEY_BUCKET, "Bucket Name",
                        "STRING", true, null));
    }

    @Override
    public void validate(Map<String, Object> config) {
        List.of(KEY_ENDPOINT, KEY_REGION, KEY_ACCESS_KEY, KEY_SECRET_KEY, KEY_BUCKET)
                .forEach(key -> {
                    Object val = config.get(key);
                    if (val == null || val.toString().isBlank()) {
                        throw new BusinessException(
                                "A3S provider requires '" + key + "' configuration");
                    }
                });
    }

    @Override
    public FileStorageProvider create(Map<String, Object> config) {
        S3Client client = S3Client.builder()
                .endpointOverride(URI.create(config.get(KEY_ENDPOINT).toString()))
                .region(Region.of(config.get(KEY_REGION).toString()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                config.get(KEY_ACCESS_KEY).toString(),
                                config.get(KEY_SECRET_KEY).toString())))
                .forcePathStyle(true)
                .build();

        String bucket = config.get(KEY_BUCKET).toString();
        return new A3sFileStorageProvider(client, bucket);
    }

    /**
     * S3-compatible file storage provider.
     */
    static class A3sFileStorageProvider implements FileStorageProvider {

        private static final DateTimeFormatter DATE_FMT =
                DateTimeFormatter.ofPattern("yyyy/MM/dd");

        private final S3Client s3;
        private final String bucket;

        A3sFileStorageProvider(S3Client s3, String bucket) {
            this.s3 = s3;
            this.bucket = bucket;
        }

        @Override
        public StoreResult store(FileStoreCommand command) {
            try {
                String datePath = LocalDate.now().format(DATE_FMT);
                String ext = extractExtension(command.originalName());
                String objectKey = command.resource() + "/"
                        + datePath + "/" + command.fileKey() + ext;

                // Read stream with checksum calculation
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] content;
                try (DigestInputStream dis = new DigestInputStream(
                        command.inputStream(), digest)) {
                    content = dis.readAllBytes();
                }
                String checksum = HexFormat.of().formatHex(digest.digest());

                // Upload to S3
                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .contentType(command.contentType())
                        .contentLength((long) content.length)
                        .build();

                s3.putObject(putRequest, RequestBody.fromBytes(content));

                return new StoreResult(objectKey, checksum, content.length);
            } catch (NoSuchAlgorithmException e) {
                throw new BusinessException(
                        "SHA-256 algorithm not available: " + e.getMessage());
            } catch (Exception e) {
                throw new BusinessException(
                        "Failed to store file in A3S: " + e.getMessage());
            }
        }

        @Override
        public Optional<FileContent> retrieve(String storagePath) {
            try {
                GetObjectRequest getRequest = GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(storagePath)
                        .build();

                var response = s3.getObject(getRequest);
                String contentType = response.response().contentType();
                long contentLength = response.response().contentLength();

                return Optional.of(new FileContent(
                        response, contentType, contentLength));
            } catch (NoSuchKeyException e) {
                return Optional.empty();
            } catch (Exception e) {
                throw new BusinessException(
                        "Failed to retrieve file from A3S: " + e.getMessage());
            }
        }

        @Override
        public void delete(String storagePath) {
            try {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(storagePath)
                        .build();
                s3.deleteObject(deleteRequest);
            } catch (Exception e) {
                throw new BusinessException(
                        "Failed to delete file from A3S: " + e.getMessage());
            }
        }

        @Override
        public boolean exists(String storagePath) {
            try {
                HeadObjectRequest headRequest = HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(storagePath)
                        .build();
                HeadObjectResponse response = s3.headObject(headRequest);
                return response != null;
            } catch (NoSuchKeyException e) {
                return false;
            } catch (Exception e) {
                return false;
            }
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
