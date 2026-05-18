package gasi.gps.storage.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;
import gasi.gps.storage.application.dto.StorageProviderMappingRequest;
import gasi.gps.storage.application.dto.StorageProviderMappingResponse;
import gasi.gps.storage.application.dto.StorageProviderRequest;
import gasi.gps.storage.application.dto.StorageProviderResponse;
import gasi.gps.storage.infrastructure.entity.StorageProviderEntity;
import gasi.gps.storage.infrastructure.entity.StorageProviderMappingEntity;
import gasi.gps.storage.infrastructure.persistence.StorageProviderEntityRepository;
import gasi.gps.storage.infrastructure.persistence.StorageProviderMappingEntityRepository;
import gasi.gps.storage.infrastructure.storage.StorageProviderRegistry;

/**
 * Admin service for managing storage providers and resource mappings.
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class StorageProviderAdminService {

    private final StorageProviderEntityRepository providerRepo;
    private final StorageProviderMappingEntityRepository mappingRepo;
    private final StorageProviderRegistry registry;
    private final IdEncoder idEncoder;

    /**
     * Creates the admin service.
     *
     * @param providerRepo provider repository
     * @param mappingRepo  mapping repository
     * @param registry     provider registry for cache management
     * @param idEncoder    public ID encoder
     */
    public StorageProviderAdminService(
            StorageProviderEntityRepository providerRepo,
            StorageProviderMappingEntityRepository mappingRepo,
            StorageProviderRegistry registry,
            IdEncoder idEncoder) {
        this.providerRepo = providerRepo;
        this.mappingRepo = mappingRepo;
        this.registry = registry;
        this.idEncoder = idEncoder;
    }

    // ── Provider CRUD ────────────────────────────────────

    /**
     * Creates a new storage provider.
     *
     * @param request provider details
     * @return created provider response
     */
    public StorageProviderResponse createProvider(StorageProviderRequest request) {
        providerRepo.findByCode(request.getCode()).ifPresent(e -> {
            throw new BusinessException("Provider code already exists: " + request.getCode());
        });

        registry.validateConfig(request.getProviderType(), request.getConfig());

        if (request.isDefault()) {
            clearDefault();
        }

        StorageProviderEntity entity = StorageProviderEntity.builder()
                .code(request.getCode())
                .name(request.getName())
                .providerType(request.getProviderType())
                .config(request.getConfig())
                .isDefault(request.isDefault())
                .enabled(request.isEnabled())
                .build();

        return toResponse(providerRepo.save(entity));
    }

    /**
     * Lists all registered storage providers.
     *
     * @return list of provider responses
     */
    @Transactional(readOnly = true)
    public List<StorageProviderResponse> listProviders() {
        return providerRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Updates an existing storage provider.
     *
     * @param id      encoded provider ID
     * @param request updated provider details
     * @return updated provider response
     */
    public StorageProviderResponse updateProvider(String id, StorageProviderRequest request) {
        Long decodedId = idEncoder.decode(id);
        StorageProviderEntity entity = providerRepo.findById(decodedId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Storage provider not found: " + id));

        registry.validateConfig(request.getProviderType(), request.getConfig());

        if (request.isDefault() && !entity.isDefault()) {
            clearDefault();
        }

        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setProviderType(request.getProviderType());
        entity.setConfig(request.getConfig());
        entity.setDefault(request.isDefault());
        entity.setEnabled(request.isEnabled());

        registry.evict(decodedId);
        return toResponse(providerRepo.save(entity));
    }

    /**
     * Deletes a storage provider.
     *
     * @param id encoded provider ID
     */
    public void deleteProvider(String id) {
        Long decodedId = idEncoder.decode(id);
        providerRepo.findById(decodedId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Storage provider not found: " + id));
        registry.evict(decodedId);
        providerRepo.deleteById(decodedId);
    }

    /**
     * Sets a provider as the default.
     *
     * @param id encoded provider ID
     * @return updated provider response
     */
    public StorageProviderResponse setDefault(String id) {
        Long decodedId = idEncoder.decode(id);
        StorageProviderEntity entity = providerRepo.findById(decodedId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Storage provider not found: " + id));

        clearDefault();
        entity.setDefault(true);
        return toResponse(providerRepo.save(entity));
    }

    // ── Mapping CRUD ─────────────────────────────────────

    /**
     * Creates a resource-to-provider mapping.
     *
     * @param request mapping details
     * @return created mapping response
     */
    public StorageProviderMappingResponse createMapping(
            StorageProviderMappingRequest request) {
        mappingRepo.findByResource(request.getResource()).ifPresent(e -> {
            throw new BusinessException(
                    "Mapping already exists for resource: " + request.getResource());
        });

        Long providerId = idEncoder.decode(request.getProviderId());
        providerRepo.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Storage provider not found: " + request.getProviderId()));

        StorageProviderMappingEntity entity = StorageProviderMappingEntity.builder()
                .resource(request.getResource())
                .providerId(providerId)
                .build();

        return toMappingResponse(mappingRepo.save(entity));
    }

    /**
     * Lists all resource-to-provider mappings.
     *
     * @return list of mapping responses
     */
    @Transactional(readOnly = true)
    public List<StorageProviderMappingResponse> listMappings() {
        return mappingRepo.findAll().stream()
                .map(this::toMappingResponse)
                .toList();
    }

    /**
     * Updates an existing mapping.
     *
     * @param id      encoded mapping ID
     * @param request updated mapping details
     * @return updated mapping response
     */
    public StorageProviderMappingResponse updateMapping(String id,
            StorageProviderMappingRequest request) {
        Long decodedId = idEncoder.decode(id);
        StorageProviderMappingEntity entity = mappingRepo.findById(decodedId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Mapping not found: " + id));

        Long providerId = idEncoder.decode(request.getProviderId());
        providerRepo.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Storage provider not found: " + request.getProviderId()));

        entity.setResource(request.getResource());
        entity.setProviderId(providerId);
        return toMappingResponse(mappingRepo.save(entity));
    }

    /**
     * Deletes a mapping.
     *
     * @param id encoded mapping ID
     */
    public void deleteMapping(String id) {
        Long decodedId = idEncoder.decode(id);
        mappingRepo.findById(decodedId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Mapping not found: " + id));
        mappingRepo.deleteById(decodedId);
    }

    // ── Helpers ──────────────────────────────────────────

    private void clearDefault() {
        providerRepo.findByIsDefaultTrue().ifPresent(existing -> {
            existing.setDefault(false);
            providerRepo.save(existing);
        });
    }

    private StorageProviderResponse toResponse(StorageProviderEntity entity) {
        return StorageProviderResponse.builder()
                .id(idEncoder.encode(entity.getId()))
                .code(entity.getCode())
                .name(entity.getName())
                .providerType(entity.getProviderType())
                .config(entity.getConfigAsMap())
                .isDefault(entity.isDefault())
                .enabled(entity.isEnabled())
                .build();
    }

    private StorageProviderMappingResponse toMappingResponse(
            StorageProviderMappingEntity entity) {
        return StorageProviderMappingResponse.builder()
                .id(idEncoder.encode(entity.getId()))
                .resource(entity.getResource())
                .providerId(idEncoder.encode(entity.getProviderId()))
                .build();
    }
}
