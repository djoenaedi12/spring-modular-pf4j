package gasi.gps.platform.bootstrap;

/**
 * Centralized package scan roots used by the platform host.
 */
public final class PlatformScanPackages {

    /** Platform host package. */
    public static final String PLATFORM = "gasi.gps.platform";

    /** Shared Spring/JPA implementation package. */
    public static final String CORE_STARTER = "gasi.gps.core.starter";

    /** Root package used by generated API plugins in this POC. */
    public static final String PLUGIN_ROOT = "gasi.gps";

    private PlatformScanPackages() {
    }
}
