package gasi.gps.storage.presentation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.core.api.presentation.dto.ApiResponse;
import gasi.gps.storage.application.dto.StorageProviderMappingRequest;
import gasi.gps.storage.application.dto.StorageProviderMappingResponse;
import gasi.gps.storage.application.dto.StorageProviderRequest;
import gasi.gps.storage.application.dto.StorageProviderResponse;
import gasi.gps.storage.application.service.StorageProviderAdminService;
import gasi.gps.storage.infrastructure.storage.StorageProviderRegistry;
import gasi.gps.storage.infrastructure.storage.StorageProviderRegistry.AvailableProviderType;
import jakarta.validation.Valid;

/**
 * Admin REST controller for managing storage providers and resource mappings.
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/admin/storage-providers")
public class StorageProviderAdminController {

    private final StorageProviderAdminService adminService;
    private final StorageProviderRegistry registry;

    /**
     * Creates the controller.
     *
     * @param adminService admin service
     * @param registry     provider registry for available types
     */
    public StorageProviderAdminController(
            StorageProviderAdminService adminService,
            StorageProviderRegistry registry) {
        this.adminService = adminService;
        this.registry = registry;
    }

    // ── Available Types (from installed plugins) ─────────

    /**
     * Lists all provider types available from installed plugins.
     *
     * @return available provider types with config field metadata
     */
    @GetMapping("/types")
    public ApiResponse<List<AvailableProviderType>> getAvailableTypes() {
        return ApiResponse.ok(registry.getAvailableTypes());
    }

    // ── Provider CRUD ────────────────────────────────────

    /**
     * Lists all registered storage providers.
     *
     * @return list of provider responses
     */
    @GetMapping
    public ApiResponse<List<StorageProviderResponse>> listProviders() {
        return ApiResponse.ok(adminService.listProviders());
    }

    /**
     * Creates a new storage provider.
     *
     * @param request provider details
     * @return created provider response
     */
    @PostMapping
    public ApiResponse<StorageProviderResponse> createProvider(
            @Valid @RequestBody StorageProviderRequest request) {
        return ApiResponse.ok(adminService.createProvider(request));
    }

    /**
     * Updates an existing storage provider.
     *
     * @param id      encoded provider ID
     * @param request updated provider details
     * @return updated provider response
     */
    @PutMapping("/{id}")
    public ApiResponse<StorageProviderResponse> updateProvider(
            @PathVariable String id,
            @Valid @RequestBody StorageProviderRequest request) {
        return ApiResponse.ok(adminService.updateProvider(id, request));
    }

    /**
     * Deletes a storage provider.
     *
     * @param id encoded provider ID
     * @return empty success response
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteProvider(@PathVariable String id) {
        adminService.deleteProvider(id);
        return ApiResponse.noContent();
    }

    /**
     * Sets a provider as the default.
     *
     * @param id encoded provider ID
     * @return updated provider response
     */
    @PutMapping("/{id}/default")
    public ApiResponse<StorageProviderResponse> setDefault(@PathVariable String id) {
        return ApiResponse.ok(adminService.setDefault(id));
    }

    // ── Mapping CRUD ─────────────────────────────────────

    /**
     * Lists all resource-to-provider mappings.
     *
     * @return list of mapping responses
     */
    @GetMapping("/mappings")
    public ApiResponse<List<StorageProviderMappingResponse>> listMappings() {
        return ApiResponse.ok(adminService.listMappings());
    }

    /**
     * Creates a resource-to-provider mapping.
     *
     * @param request mapping details
     * @return created mapping response
     */
    @PostMapping("/mappings")
    public ApiResponse<StorageProviderMappingResponse> createMapping(
            @Valid @RequestBody StorageProviderMappingRequest request) {
        return ApiResponse.ok(adminService.createMapping(request));
    }

    /**
     * Updates an existing mapping.
     *
     * @param id      encoded mapping ID
     * @param request updated mapping details
     * @return updated mapping response
     */
    @PutMapping("/mappings/{id}")
    public ApiResponse<StorageProviderMappingResponse> updateMapping(
            @PathVariable String id,
            @Valid @RequestBody StorageProviderMappingRequest request) {
        return ApiResponse.ok(adminService.updateMapping(id, request));
    }

    /**
     * Deletes a mapping.
     *
     * @param id encoded mapping ID
     * @return empty success response
     */
    @DeleteMapping("/mappings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteMapping(@PathVariable String id) {
        adminService.deleteMapping(id);
        return ApiResponse.noContent();
    }
}
