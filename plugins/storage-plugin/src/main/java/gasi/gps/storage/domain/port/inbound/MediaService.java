package gasi.gps.storage.domain.port.inbound;

import gasi.gps.core.api.storage.FileStorageService;

/**
 * Plugin-internal marker interface for file storage service.
 *
 * <p>Extends {@link FileStorageService} from core-api so that both
 * external plugins (via {@code FileStorageService}) and internal
 * components (via {@code MediaService}) can inject the same bean.</p>
 *
 * @since 1.0.0
 */
public interface MediaService extends FileStorageService {
    // No additional methods — acts as a typed marker within the
    // storage plugin. Plugin-specific methods can be added here
    // in the future without polluting the core-api contract.
}
