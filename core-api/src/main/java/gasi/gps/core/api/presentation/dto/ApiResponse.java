package gasi.gps.core.api.presentation.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response envelope for all REST endpoints.
 *
 * <h3>Success example:</h3>
 *
 * <pre>{@code
 * {
 * "success": true,
 * "message": "OK",
 * "data": { ... },
 * "timestamp": "2026-03-03T10:15:30Z"
 * }
 * }</pre>
 *
 * <h3>Error example:</h3>
 *
 * <pre>{@code
 * {
 * "success": false,
 * "message": "Validation failed",
 * "errors": ["Name is required", "Email is invalid"],
 * "timestamp": "2026-03-03T10:15:30Z"
 * }
 * }</pre>
 *
 * @param <T> the data payload type
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private List<String> errors;

    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Creates a success response with data and HTTP 200.
     *
     * @param data the response payload
     * @param <T>  the payload type
     * @return a success {@code ApiResponse}
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("OK")
                .data(data)
                .build();
    }

    /**
     * Creates a success response with data, a custom message, and HTTP 200.
     *
     * @param data    the response payload
     * @param message a custom success message
     * @param <T>     the payload type
     * @return a success {@code ApiResponse}
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates a success response with no content (HTTP 204).
     *
     * @return a no-content {@code ApiResponse}
     */
    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder()
                .success(true)
                .message("No Content")
                .build();
    }

    /**
     * Creates an error response with a single error message.
     *
     * @param code    the HTTP status code
     * @param message the error summary
     * @param <T>     the payload type
     * @return an error {@code ApiResponse}
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(List.of(message))
                .build();
    }

    /**
     * Creates an error response with multiple error messages.
     *
     * @param code    the HTTP status code
     * @param message the error summary
     * @param errors  the list of detailed error messages
     * @param <T>     the payload type
     * @return an error {@code ApiResponse}
     */
    public static <T> ApiResponse<T> error(int code, String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(Collections.unmodifiableList(errors))
                .build();
    }
}
