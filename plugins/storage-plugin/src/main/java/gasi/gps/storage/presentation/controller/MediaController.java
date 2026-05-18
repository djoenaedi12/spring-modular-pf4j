package gasi.gps.storage.presentation.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gasi.gps.core.api.presentation.dto.ApiResponse;
import gasi.gps.core.api.storage.FileStorageService;
import gasi.gps.core.api.storage.FileStorageService.DownloadResult;
import gasi.gps.core.api.storage.FileStorageService.FileInfo;
import gasi.gps.core.api.storage.FileStorageService.UploadCommand;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;
import gasi.gps.storage.application.dto.MediaResponse;

/**
 * REST controller for media upload, download, and management.
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/medias")
public class MediaController {

    private final FileStorageService fileStorageService;
    private final IdEncoder idEncoder;

    /**
     * Creates the controller.
     *
     * @param fileStorageService file storage service
     * @param idEncoder          public ID encoder
     */
    public MediaController(FileStorageService fileStorageService, IdEncoder idEncoder) {
        this.fileStorageService = fileStorageService;
        this.idEncoder = idEncoder;
    }

    /**
     * Uploads a file. The {@code resourceId} parameter is optional — when
     * the owning entity does not exist yet, upload first and then use the
     * {@code PUT /{fileKey}/attach} endpoint to link later.
     *
     * @param file       multipart file to upload
     * @param resource   resource type for ownership and provider routing
     * @param resourceId encoded resource identifier (optional)
     * @return media response with file metadata
     * @throws IOException if reading the file fails
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("resource") String resource,
            @RequestParam(value = "resourceId", required = false) String resourceId)
            throws IOException {

        Long decodedResourceId = resourceId != null ? idEncoder.decode(resourceId) : null;

        FileInfo info = fileStorageService.upload(new UploadCommand(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream(),
                resource,
                decodedResourceId));

        return ApiResponse.ok(toMediaResponse(info));
    }

    /**
     * Downloads a file by its unique key.
     *
     * @param fileKey unique file key
     * @return file content as streaming response
     */
    @GetMapping("/{fileKey}/download")
    public ResponseEntity<Resource> download(@PathVariable String fileKey) {
        DownloadResult result = fileStorageService.download(fileKey);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .contentLength(result.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.originalName() + "\"")
                .body(new InputStreamResource(result.inputStream()));
    }

    /**
     * Deletes a file by its unique key.
     *
     * @param fileKey unique file key
     * @return empty success response
     */
    @DeleteMapping("/{fileKey}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable String fileKey) {
        fileStorageService.delete(fileKey);
        return ApiResponse.noContent();
    }

    /**
     * Links an uploaded file to a resource owner.
     *
     * <p>Use this after uploading without a {@code resourceId} and the
     * owning entity has been created.</p>
     *
     * @param fileKey    unique file key
     * @param resourceId encoded resource owner identifier
     * @return empty success response
     */
    @PutMapping("/{fileKey}/attach")
    public ApiResponse<Void> attach(
            @PathVariable String fileKey,
            @RequestParam("resourceId") String resourceId) {
        fileStorageService.attach(fileKey, idEncoder.decode(resourceId));
        return ApiResponse.ok(null);
    }

    /**
     * Lists all media files for a specific resource owner.
     *
     * @param resource   resource type
     * @param resourceId encoded resource identifier
     * @return list of media responses
     */
    @GetMapping
    public ApiResponse<List<MediaResponse>> findByOwner(
            @RequestParam("resource") String resource,
            @RequestParam("resourceId") String resourceId) {
        List<MediaResponse> responses = fileStorageService
                .findByOwner(resource, idEncoder.decode(resourceId))
                .stream()
                .map(this::toMediaResponse)
                .toList();
        return ApiResponse.ok(responses);
    }

    private MediaResponse toMediaResponse(FileInfo info) {
        return MediaResponse.builder()
                .fileKey(info.fileKey())
                .originalName(info.originalName())
                .contentType(info.contentType())
                .fileSize(info.fileSize())
                .checksum(info.checksum())
                .resource(info.resource())
                .resourceId(info.resourceId() != null
                        ? idEncoder.encode(info.resourceId()) : null)
                .build();
    }
}
