package gasi.gps.core.api.domain.model;

/**
 * Immutable data transfer object (DTO) representing metadata for a plugin
 * module.
 *
 * <p>
 * This record provides a simple, type-safe way to bundle the essential
 * information about a plugin, including its unique name, human-readable
 * description, and version number. It is used by the core application to
 * maintain an inventory of all available modules and their properties.
 * </p>
 *
 * <p>
 * <strong>Key Features:</strong>
 * </p>
 * <ul>
 * <li><strong>Immutability:</strong> All fields are final, ensuring that
 * once a {@code ModuleInfo} object is created, its state cannot be
 * changed. This promotes thread safety and predictable behavior.</li>
 * <li><strong>Compact Representation:</strong> As a Java record, it
 * automatically provides canonical constructors, {@code equals()},
 * {@code hashCode()}, and {@code toString()} methods, reducing boilerplate
 * code.</li>
 * <li><strong>Type Safety:</strong> Uses {@link String} types for all
 * fields, ensuring type consistency across the application.</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 *
 * <pre>{@code
 * // Creating a new ModuleInfo instance
 * ModuleInfo inventoryModule = new ModuleInfo("Inventory", "Manages stock and products", "1.0.0");
 *
 * // Accessing the properties
 * String name = inventoryModule.name();
 * String description = inventoryModule.description();
 * String version = inventoryModule.version();
 *
 * // Records automatically provide equals(), hashCode(), and toString()
 * System.out.println(inventoryModule);
 * }</pre>
 *
 * @param name        unique name of the plugin module
 * @param description human-readable description of the plugin
 * @param version     version of the plugin module
 * @see gasi.gps.core.api.AppExtension
 * @since 1.0.0
 */
public record ModuleInfo(String name, String description, String version) {
}
