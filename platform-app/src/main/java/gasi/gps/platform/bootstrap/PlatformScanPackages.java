package gasi.gps.platform.bootstrap;

import gasi.gps.core.api.extension.AppExtension;

/**
 * Centralized package scan roots used by the platform host.
 */
public final class PlatformScanPackages {

    /** Platform host package. */
    public static final String PLATFORM = "gasi.gps.platform";

    /** Shared Spring/JPA implementation package. */
    public static final String CORE_STARTER = "gasi.gps.core.starter";

    /**
     * Plugin package root.
     *
     * <p>PF4J plugins in this project currently live below {@code gasi.gps};
     * keep this broad root until {@link AppExtension#getBasePackages()} is
     * wired into a dynamic Spring registration flow.</p>
     */
    public static final String PLUGINS = "gasi.gps";

    private PlatformScanPackages() {
    }
}
