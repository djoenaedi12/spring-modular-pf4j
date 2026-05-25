package gasi.gps.core.starter.presentation.handler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.presentation.dto.ApiResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.InvalidTypeIdException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Global exception handler that translates exceptions into standard
 * {@link ApiResponse} error envelopes.
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Creates a global exception handler.
     */
    public GlobalExceptionHandler() {
    }

    /**
     * Handles entity not found (404).
     *
     * @param ex exception thrown when an entity cannot be found
     * @return API error response
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleEntityNotFound(EntityNotFoundException ex) {
        LOG.warn("Entity not found: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    /**
     * Handles requests to non-existent endpoints (404).
     *
     * @param ex exception thrown when no request handler exists
     * @return API error response
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = String.format("No endpoint found: %s %s", ex.getHttpMethod(), ex.getRequestURL());
        LOG.warn("No handler found: {}", message);
        return ApiResponse.error(HttpStatus.NOT_FOUND.value(), message);
    }

    /**
     * Handles unsupported HTTP method requests (405).
     *
     * @param ex exception thrown for unsupported HTTP methods
     * @return API error response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "unknown";
        String message = String.format("Method '%s' is not supported. Supported methods: %s",
                ex.getMethod(), supported);
        LOG.warn("Method not supported: {}", message);
        return ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), message);
    }

    /**
     * Handles business rule violations (422).
     *
     * @param ex exception containing business validation errors
     * @return API error response
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        LOG.warn("Business rule violation: {}", ex.getMessage());
        return ApiResponse.error(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Business rule violation",
                ex.getErrors());
    }

    /**
     * Handles failed authentication (wrong username/password, locked, disabled)
     * (401).
     *
     * @param ex authentication failure
     * @return API error response
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthenticationFailed(AuthenticationException ex) {
        LOG.warn("Authentication failed: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials");
    }

    /**
     * Handles authorization denied errors from Spring Security (401 or 403).
     *
     * <p>
     * Returns 401 if the current user is not authenticated (anonymous),
     * 403 if authenticated but lacks the required permission.
     * </p>
     *
     * @param ex authorization failure
     * @return API error response with the appropriate HTTP status
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationDenied(
            AuthorizationDeniedException ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = auth == null || auth instanceof AnonymousAuthenticationToken;

        if (isAnonymous) {
            LOG.warn("Unauthenticated access denied");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"));
        }
        LOG.warn("Authorization denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Access denied"));
    }

    /**
     * Handles bean validation errors from {@code @Valid} (400).
     *
     * @param ex method argument validation failure
     * @return API error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, List<String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        fe -> fe.getField(),
                        Collectors.mapping(fe -> fe.getDefaultMessage(), Collectors.toList())));
        LOG.warn("Validation failed: {}", fieldErrors);
        return ApiResponse.fieldError(HttpStatus.BAD_REQUEST.value(), "Validation failed", fieldErrors);
    }

    /**
     * Handles malformed JSON or unreadable request body (400).
     *
     * @param ex unreadable request body exception
     * @return API error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        String detail = extractReadableDetail(ex);
        LOG.warn("Malformed request body: {}", detail);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Malformed request body", List.of(detail));
    }

    /**
     * Extracts a human-readable detail message from the root cause of
     * an {@link HttpMessageNotReadableException}.
     */
    private String extractReadableDetail(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        LOG.debug("HttpMessageNotReadableException cause type: {}, message: {}",
                cause != null ? cause.getClass().getName() : "null",
                cause != null ? cause.getMessage() : ex.getMessage());

        if (cause instanceof InvalidTypeIdException itid) {
            String path = buildJsonPath(itid.getPath());
            return String.format("Unknown type '%s' at '%s'", itid.getTypeId(), path);
        }

        if (cause instanceof UnrecognizedPropertyException upe) {
            String path = buildJsonPath(upe.getPath());
            return String.format("Unrecognized field '%s' at '%s'", upe.getPropertyName(), path);
        }

        if (cause instanceof MismatchedInputException mie) {
            String path = buildJsonPath(mie.getPath());
            return String.format("Invalid value at '%s'", path);
        }

        if (cause instanceof StreamReadException sre) {
            return String.format("Invalid JSON syntax at line %d, column %d: %s",
                    sre.getLocation().getLineNr(),
                    sre.getLocation().getColumnNr(),
                    sre.getOriginalMessage());
        }

        return "Malformed request body";
    }

    /**
     * Builds a dotted JSON path string from Jackson's reference list,
     * e.g. {@code "filter.filters[1].type"}.
     */
    private String buildJsonPath(List<JacksonException.Reference> path) {
        if (path == null || path.isEmpty()) {
            return "$";
        }
        StringBuilder sb = new StringBuilder();
        for (JacksonException.Reference ref : path) {
            if (ref.getIndex() >= 0) {
                sb.append('[').append(ref.getIndex()).append(']');
            } else {
                if (!sb.isEmpty()) {
                    sb.append('.');
                }
                sb.append(ref.getPropertyName());
            }
        }
        return sb.toString();
    }

    /**
     * Handles type mismatch in path variables or request params (400).
     *
     * @param ex type mismatch exception
     * @return API error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' must be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        LOG.warn("Type mismatch: {}", message);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * Catches any unhandled exception (500).
     *
     * @param ex unhandled exception
     * @return API error response
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneric(Exception ex) {
        LOG.error("Unexpected error", ex);
        return ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred");
    }
}
