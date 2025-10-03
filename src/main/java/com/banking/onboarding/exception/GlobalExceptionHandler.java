package com.banking.onboarding.exception;

import com.banking.onboarding.audit.AuditService;
import com.banking.onboarding.constants.OnboardingConstants;
import com.banking.onboarding.enums.OnboardingEnums;
import com.banking.onboarding.service.CorrelationIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive global exception handler with detailed error responses
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final CorrelationIdService correlationIdService;
    private final AuditService auditService;

    // ===========================================
    // BUSINESS EXCEPTIONS
    // ===========================================

    /**
     * Handle business exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        String correlationId = ex.getCorrelationId() != null ? 
                ex.getCorrelationId() : correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Business error [{}]: {}", correlationId, ex.getErrorCode(), ex.getMessage());
        
        // Log to audit service
        auditService.logError(
            null, // processId - will be extracted from context if available
            correlationId,
            OnboardingConstants.ErrorTypes.BUSINESS_ERROR,
            ex.getMessage(),
            getStackTrace(ex),
            Map.of(
                "errorCode", ex.getErrorCode(),
                "requestUri", getPath(request),
                "method", getMethod(request),
                "userAgent", request.getHeader(OnboardingConstants.HttpHeaders.USER_AGENT)
            )
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.BUSINESS_ERROR)
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .suggestion(getBusinessSuggestion(ex.getErrorCode()))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        String correlationId = ex.getCorrelationId() != null ? 
                ex.getCorrelationId() : correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Validation error: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.VALIDATION_ERROR)
                .errorCode(OnboardingConstants.ErrorCodes.VALIDATION_ERROR)
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .suggestion(OnboardingConstants.Suggestions.CHECK_INPUT_DATA)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle process exceptions
     */
    @ExceptionHandler(ProcessException.class)
    public ResponseEntity<ErrorResponse> handleProcessException(
            ProcessException ex, WebRequest request) {
        
        String correlationId = ex.getCorrelationId() != null ? 
                ex.getCorrelationId() : correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Process error: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.PROCESS_ERROR)
                .errorCode(OnboardingConstants.ErrorCodes.PROCESS_ERROR)
                .message(ex.getMessage())
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .suggestion(OnboardingConstants.Suggestions.CHECK_PROCESS_STATUS)
                .build();
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    /**
     * Handle external API exceptions
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(
            ExternalApiException ex, WebRequest request) {
        
        String correlationId = ex.getCorrelationId() != null ? 
                ex.getCorrelationId() : correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] External API error [{}]: {} - Provider: {}, Endpoint: {}, Status: {}", 
                correlationId, ex.getErrorCode(), ex.getMessage(), ex.getApiProvider(), ex.getEndpoint(), ex.getHttpStatus());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiProvider", ex.getApiProvider());
        metadata.put("endpoint", ex.getEndpoint());
        if (ex.getHttpStatus() > 0) {
            metadata.put("httpStatus", ex.getHttpStatus());
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.EXTERNAL_API_ERROR)
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .status(HttpStatus.BAD_GATEWAY)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .metadata(metadata)
                .suggestion(OnboardingConstants.Suggestions.EXTERNAL_SERVICE_UNAVAILABLE)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Handle step execution exceptions
     */
    @ExceptionHandler(StepExecutionException.class)
    public ResponseEntity<ErrorResponse> handleStepExecutionException(
            StepExecutionException ex, WebRequest request) {
        
        String correlationId = ex.getCorrelationId() != null ? 
                ex.getCorrelationId() : correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Step execution error: {} - Step: {}, Process: {}", 
                correlationId, ex.getMessage(), ex.getStepName(), ex.getProcessId());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("stepName", ex.getStepName());
        if (ex.getProcessId() != null) {
            metadata.put("processId", ex.getProcessId());
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.STEP_EXECUTION_ERROR)
                .errorCode(OnboardingConstants.ErrorCodes.STEP_EXECUTION_ERROR)
                .message(ex.getMessage())
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .metadata(metadata)
                .suggestion(OnboardingConstants.Suggestions.STEP_EXECUTION_FAILED)
                .build();
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    // ===========================================
    // SPRING FRAMEWORK EXCEPTIONS
    // ===========================================

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String correlationId = correlationIdService.getOrGenerateCorrelationId(null);
        
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });
        
        log.error("[CORRELATION:{}] Validation error: {}", correlationId, details);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.VALIDATION_ERROR)
                .errorCode(OnboardingConstants.ErrorCodes.VALIDATION_ERROR)
                .message("Request validation failed")
                .status(HttpStatus.BAD_REQUEST)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .details(details)
                .suggestion(OnboardingConstants.Suggestions.CHECK_INPUT_DATA)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle binding errors
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, WebRequest request) {
        
        String correlationId = correlationIdService.getOrGenerateCorrelationId(null);
        
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });
        
        log.error("[CORRELATION:{}] Binding error: {}", correlationId, details);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.BINDING_ERROR)
                .errorCode(OnboardingConstants.ErrorCodes.BINDING_ERROR)
                .message("Request binding failed")
                .status(HttpStatus.BAD_REQUEST)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .details(details)
                .suggestion(OnboardingConstants.Suggestions.CHECK_REQUEST_FORMAT)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        
        String correlationId = correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Missing parameter: {}", correlationId, ex.getParameterName());
        
        Map<String, Object> details = new HashMap<>();
        details.put("parameterName", ex.getParameterName());
        details.put("parameterType", ex.getParameterType());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.MISSING_PARAMETER)
                .errorCode(OnboardingConstants.ErrorCodes.MISSING_PARAMETER)
                .message("Required parameter is missing: " + ex.getParameterName())
                .status(HttpStatus.BAD_REQUEST)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .details(details)
                .suggestion(OnboardingConstants.Suggestions.PROVIDE_MISSING_PARAMETER + ex.getParameterName())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        String correlationId = correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Type mismatch: {}", correlationId, ex.getName());
        
        Map<String, Object> details = new HashMap<>();
        details.put("parameterName", ex.getName());
        details.put("requiredType", ex.getRequiredType().getSimpleName());
        details.put("providedValue", ex.getValue());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.TYPE_MISMATCH)
                .errorCode(OnboardingConstants.ErrorCodes.TYPE_MISMATCH)
                .message("Invalid parameter type for: " + ex.getName())
                .status(HttpStatus.BAD_REQUEST)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .details(details)
                .suggestion(OnboardingConstants.Suggestions.PROVIDE_VALID_TYPE + ex.getRequiredType().getSimpleName() + " for parameter " + ex.getName())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle HTTP message not readable (malformed JSON/XML)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        String correlationId = correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Message not readable: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.MALFORMED_REQUEST)
                .errorCode(OnboardingConstants.ErrorCodes.MALFORMED_REQUEST)
                .message("Request body is malformed or not readable")
                .status(HttpStatus.BAD_REQUEST)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .suggestion(OnboardingConstants.Suggestions.CHECK_REQUEST_BODY)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle HTTP method not supported
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        String correlationId = correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Method not supported: {}", correlationId, ex.getMethod());
        
        Map<String, Object> details = new HashMap<>();
        details.put("requestedMethod", ex.getMethod());
        details.put("supportedMethods", ex.getSupportedMethods());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.METHOD_NOT_SUPPORTED)
                .errorCode(OnboardingConstants.ErrorCodes.METHOD_NOT_SUPPORTED)
                .message("HTTP method not supported: " + ex.getMethod())
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .details(details)
                .suggestion(OnboardingConstants.Suggestions.USE_SUPPORTED_METHODS + String.join(", ", ex.getSupportedMethods()))
                .build();
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * Handle 404 - No handler found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {
        
        String correlationId = correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] No handler found: {}", correlationId, ex.getRequestURL());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.NOT_FOUND)
                .errorCode(OnboardingConstants.ErrorCodes.NOT_FOUND)
                .message("Endpoint not found: " + ex.getRequestURL())
                .status(HttpStatus.NOT_FOUND)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .suggestion(OnboardingConstants.Suggestions.CHECK_API_DOCUMENTATION)
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // ===========================================
    // GENERAL EXCEPTIONS
    // ===========================================

    /**
     * Handle illegal argument errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        String correlationId = correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Illegal argument: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.ILLEGAL_ARGUMENT)
                .errorCode(OnboardingConstants.ErrorCodes.ILLEGAL_ARGUMENT)
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .suggestion(OnboardingConstants.Suggestions.CHECK_INPUT_PARAMETERS)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle runtime errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        String correlationId = correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Runtime error: {}", correlationId, ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.RUNTIME_ERROR)
                .errorCode(OnboardingConstants.ErrorCodes.RUNTIME_ERROR)
                .message("An unexpected runtime error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .suggestion(OnboardingConstants.Suggestions.CONTACT_SUPPORT)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        String correlationId = correlationIdService.getOrGenerateCorrelationId(null);
        
        log.error("[CORRELATION:{}] Generic error: {}", correlationId, ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(OnboardingConstants.ErrorTypes.INTERNAL_ERROR)
                .errorCode(OnboardingConstants.ErrorCodes.INTERNAL_ERROR)
                .message("An internal server error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .path(getPath(request))
                .method(getMethod(request))
                .suggestion(OnboardingConstants.Suggestions.CONTACT_SUPPORT)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // ===========================================
    // HELPER METHODS
    // ===========================================

    private String getPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.replace("uri=", "");
    }

    private String getMethod(WebRequest request) {
        // Try to get HTTP method from request attributes
        Object method = request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler", 0);
        if (method != null) {
            return method.toString();
        }
        return "UNKNOWN";
    }

    private String getBusinessSuggestion(String errorCode) {
        return switch (errorCode) {
            case OnboardingConstants.ErrorCodes.VALIDATION_ERROR -> OnboardingConstants.Suggestions.CHECK_INPUT_DATA;
            case OnboardingConstants.ErrorCodes.PROCESS_ERROR -> OnboardingConstants.Suggestions.CHECK_PROCESS_STATUS;
            case OnboardingConstants.ErrorCodes.EXTERNAL_API_ERROR -> OnboardingConstants.Suggestions.EXTERNAL_SERVICE_UNAVAILABLE;
            case OnboardingConstants.ErrorCodes.STEP_EXECUTION_ERROR -> OnboardingConstants.Suggestions.STEP_EXECUTION_FAILED;
            default -> OnboardingConstants.Suggestions.TRY_AGAIN;
        };
    }
    
    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception exception) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
}