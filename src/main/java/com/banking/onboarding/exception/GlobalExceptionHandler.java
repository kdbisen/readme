package com.banking.onboarding.exception;

import com.banking.onboarding.logging.ErrorEventService;
import com.banking.onboarding.service.CorrelationIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler with Error Event Logging
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorEventService errorEventService;
    private final CorrelationIdService correlationIdService;

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        log.error("[CORRELATION:{}] Runtime exception occurred: {}", correlationId, ex.getMessage(), ex);
        
        // Log error event
        errorEventService.logApplicationError(
            ex.getMessage(),
            correlationId,
            traceId,
            "banking-onboarding-service",
            "runtime-exception-handler",
            ex
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("RUNTIME_ERROR")
                .message("An unexpected error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .correlationId(correlationId)
                .traceId(traceId)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        log.warn("[CORRELATION:{}] Illegal argument exception: {}", correlationId, ex.getMessage());
        
        // Log validation error
        Map<String, Object> validationErrors = new HashMap<>();
        validationErrors.put("argument", ex.getMessage());
        
        errorEventService.logValidationError(
            ex.getMessage(),
            correlationId,
            traceId,
            "banking-onboarding-service",
            "illegal-argument-handler",
            validationErrors
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("VALIDATION_ERROR")
                .message("Invalid argument provided: " + ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .correlationId(correlationId)
                .traceId(traceId)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle null pointer exceptions
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex, WebRequest request) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        log.error("[CORRELATION:{}] Null pointer exception: {}", correlationId, ex.getMessage(), ex);
        
        // Log application error
        errorEventService.logApplicationError(
            "Null pointer exception: " + ex.getMessage(),
            correlationId,
            traceId,
            "banking-onboarding-service",
            "null-pointer-handler",
            ex
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("NULL_POINTER_ERROR")
                .message("A null pointer exception occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .correlationId(correlationId)
                .traceId(traceId)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle illegal state exceptions
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        log.warn("[CORRELATION:{}] Illegal state exception: {}", correlationId, ex.getMessage());
        
        // Log business logic error
        Map<String, Object> businessContext = new HashMap<>();
        businessContext.put("state", ex.getMessage());
        
        errorEventService.logBusinessLogicError(
            "ILLEGAL_STATE",
            ex.getMessage(),
            correlationId,
            traceId,
            "banking-onboarding-service",
            "illegal-state-handler",
            businessContext
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("ILLEGAL_STATE_ERROR")
                .message("Invalid state: " + ex.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .correlationId(correlationId)
                .traceId(traceId)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        log.error("[CORRELATION:{}] Generic exception occurred: {}", correlationId, ex.getMessage(), ex);
        
        // Log system error
        errorEventService.logSystemError(
            "GENERIC_EXCEPTION_HANDLER",
            ex.getMessage(),
            correlationId,
            traceId,
            ex
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("GENERIC_ERROR")
                .message("An unexpected error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .correlationId(correlationId)
                .traceId(traceId)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Generate trace ID
     */
    private String generateTraceId() {
        return "TRACE-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}