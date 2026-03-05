package gasi.gps.core.presentation.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles entity not found (404).
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    /**
     * Handles requests to non-existent endpoints (404).
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = String.format("No endpoint found: %s %s", ex.getHttpMethod(), ex.getRequestURL());
        log.warn("No handler found: {}", message);
        return ApiResponse.error(HttpStatus.NOT_FOUND.value(), message);
    }

    /**
     * Handles unsupported HTTP method requests (405).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "unknown";
        String message = String.format("Method '%s' is not supported. Supported methods: %s",
                ex.getMethod(), supported);
        log.warn("Method not supported: {}", message);
        return ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), message);
    }

    /**
     * Handles business rule violations (422).
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ApiResponse.error(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Business rule violation",
                ex.getErrors());
    }

    /**
     * Handles bean validation errors from {@code @Valid} (400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        log.warn("Validation failed: {}", errors);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
    }

    /**
     * Handles malformed JSON or unreadable request body (400).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        String detail = extractReadableDetail(ex);
        log.warn("Malformed request body: {}", detail);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Malformed request body", List.of(detail));
    }

    /**
     * Extracts a human-readable detail message from the root cause of
     * an {@link HttpMessageNotReadableException}.
     */
    private String extractReadableDetail(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        log.debug("HttpMessageNotReadableException cause type: {}, message: {}",
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
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' must be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch: {}", message);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * Catches any unhandled exception (500).
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred");
    }
}
