package com.banking.onboarding.service.collections;

import com.banking.onboarding.model.collections.ErrorEvent;
import com.banking.onboarding.repository.collections.ErrorEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Comprehensive Error Event Logging Service
 * Provides detailed error tracking, analysis, and management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorEventLoggingService {

    private final ErrorEventRepository errorEventRepository;

    /**
     * Log a new error event
     */
    public ErrorEvent logError(ErrorEventRequest request) {
        log.info("[CORRELATION:{}] Logging error event: {} - {}", 
                request.getCorrelationId(), request.getErrorType(), request.getErrorMessage());

        ErrorEvent errorEvent = ErrorEvent.builder()
                .processId(request.getProcessId())
                .stepId(request.getStepId())
                .correlationId(request.getCorrelationId())
                .errorType(request.getErrorType())
                .errorCategory(request.getErrorCategory())
                .errorSeverity(request.getErrorSeverity())
                .errorCode(request.getErrorCode())
                .errorMessage(request.getErrorMessage())
                .errorDescription(request.getErrorDescription())
                .exceptionClass(request.getExceptionClass())
                .stackTrace(request.getStackTrace())
                .errorSource(request.getErrorSource())
                .errorComponent(request.getErrorComponent())
                .errorMethod(request.getErrorMethod())
                .errorLineNumber(request.getErrorLineNumber())
                .errorContext(request.getErrorContext())
                .errorData(request.getErrorData())
                .errorPayload(request.getErrorPayload())
                .errorPayloadType(request.getErrorPayloadType())
                .recoverable(request.isRecoverable())
                .recoveryAction(request.getRecoveryAction())
                .retryCount(request.getRetryCount())
                .maxRetries(request.getMaxRetries())
                .retryStrategy(request.getRetryStrategy())
                .resolved(false)
                .timestamp(LocalDateTime.now())
                .firstOccurrence(LocalDateTime.now())
                .lastOccurrence(LocalDateTime.now())
                .impactLevel(request.getImpactLevel())
                .businessImpact(request.getBusinessImpact())
                .technicalImpact(request.getTechnicalImpact())
                .userFacing(request.isUserFacing())
                .affectedUsers(request.getAffectedUsers())
                .systemVersion(request.getSystemVersion())
                .environment(request.getEnvironment())
                .region(request.getRegion())
                .systemContext(request.getSystemContext())
                .build();

        ErrorEvent savedEvent = errorEventRepository.save(errorEvent);
        
        log.error("[CORRELATION:{}] Error event logged with ID: {} - Severity: {}", 
                request.getCorrelationId(), savedEvent.getId(), request.getErrorSeverity());

        return savedEvent;
    }

    /**
     * Log error from exception
     */
    public ErrorEvent logErrorFromException(String processId, String stepId, String correlationId, 
                                          Exception exception, Map<String, Object> context) {
        log.info("[CORRELATION:{}] Logging error from exception: {}", correlationId, exception.getClass().getSimpleName());

        ErrorEventRequest request = ErrorEventRequest.builder()
                .processId(processId)
                .stepId(stepId)
                .correlationId(correlationId)
                .errorType(determineErrorType(exception))
                .errorCategory(determineErrorCategory(exception))
                .errorSeverity(determineErrorSeverity(exception))
                .errorCode(exception.getClass().getSimpleName())
                .errorMessage(exception.getMessage())
                .errorDescription("Exception occurred during processing")
                .exceptionClass(exception.getClass().getName())
                .stackTrace(getStackTrace(exception))
                .errorSource("STEP_EXECUTION")
                .errorComponent(extractComponentFromStackTrace(exception))
                .errorMethod(extractMethodFromStackTrace(exception))
                .errorLineNumber(extractLineNumberFromStackTrace(exception))
                .errorContext(context)
                .isRecoverable(isRecoverableException(exception))
                .recoveryAction(determineRecoveryAction(exception))
                .retryCount(0)
                .maxRetries(3)
                .retryStrategy("EXPONENTIAL_BACKOFF")
                .impactLevel(determineImpactLevel(exception))
                .businessImpact(determineBusinessImpact(exception))
                .technicalImpact(determineTechnicalImpact(exception))
                .isUserFacing(isUserFacingException(exception))
                .affectedUsers(0)
                .systemVersion("1.0.0")
                .environment("PROD")
                .region("US-EAST")
                .build();

        return logError(request);
    }

    /**
     * Update error event with retry information
     */
    public ErrorEvent updateErrorWithRetry(String errorEventId, int retryCount, String retryResult) {
        log.info("Updating error event {} with retry count: {}", errorEventId, retryCount);

        Optional<ErrorEvent> errorOpt = errorEventRepository.findById(errorEventId);
        if (errorOpt.isEmpty()) {
            log.warn("Error event not found: {}", errorEventId);
            return null;
        }

        ErrorEvent errorEvent = errorOpt.get();
        errorEvent.setRetryCount(retryCount);
        errorEvent.setLastOccurrence(LocalDateTime.now());

        if ("SUCCESS".equals(retryResult)) {
            errorEvent.setResolved(true);
            errorEvent.setResolutionAction("RETRY_SUCCESS");
            errorEvent.setResolvedBy("system");
            errorEvent.setResolvedAt(LocalDateTime.now());
            errorEvent.setResolutionNotes("Error resolved through retry mechanism");
        }

        ErrorEvent updatedEvent = errorEventRepository.save(errorEvent);
        
        log.info("Error event {} updated - Retry count: {}, Resolved: {}", 
                errorEventId, retryCount, errorEvent.getIsResolved());

        return updatedEvent;
    }

    /**
     * Resolve error event
     */
    public ErrorEvent resolveError(String errorEventId, String resolvedBy, String resolutionAction, 
                                 String resolutionNotes) {
        log.info("Resolving error event: {} by {}", errorEventId, resolvedBy);

        Optional<ErrorEvent> errorOpt = errorEventRepository.findById(errorEventId);
        if (errorOpt.isEmpty()) {
            log.warn("Error event not found: {}", errorEventId);
            return null;
        }

        ErrorEvent errorEvent = errorOpt.get();
        errorEvent.setIsResolved(true);
        errorEvent.setResolvedBy(resolvedBy);
        errorEvent.setResolutionAction(resolutionAction);
        errorEvent.setResolutionNotes(resolutionNotes);
        errorEvent.setResolvedAt(LocalDateTime.now());

        ErrorEvent resolvedEvent = errorEventRepository.save(errorEvent);
        
        log.info("Error event {} resolved by {} with action: {}", 
                errorEventId, resolvedBy, resolutionAction);

        return resolvedEvent;
    }

    /**
     * Get error analysis for a process
     */
    public ErrorAnalysis getErrorAnalysis(String processId) {
        log.info("Getting error analysis for process: {}", processId);

        List<ErrorEvent> errors = errorEventRepository.findByProcessId(processId);
        if (errors.isEmpty()) {
            return ErrorAnalysis.builder()
                    .processId(processId)
                    .totalErrors(0)
                    .errorsByType(Map.of())
                    .errorsBySeverity(Map.of())
                    .errorsByCategory(Map.of())
                    .recoverableErrors(0)
                    .resolvedErrors(0)
                    .userFacingErrors(0)
                    .criticalErrors(0)
                    .build();
        }

        // Analyze errors
        Map<String, Long> errorsByType = errors.stream()
                .collect(Collectors.groupingBy(ErrorEvent::getErrorType, Collectors.counting()));

        Map<String, Long> errorsBySeverity = errors.stream()
                .collect(Collectors.groupingBy(ErrorEvent::getErrorSeverity, Collectors.counting()));

        Map<String, Long> errorsByCategory = errors.stream()
                .collect(Collectors.groupingBy(ErrorEvent::getErrorCategory, Collectors.counting()));

        long recoverableErrors = errors.stream().mapToLong(e -> e.isRecoverable() ? 1 : 0).sum();
        long resolvedErrors = errors.stream().mapToLong(e -> e.isResolved() ? 1 : 0).sum();
        long userFacingErrors = errors.stream().mapToLong(e -> e.isUserFacing() ? 1 : 0).sum();
        long criticalErrors = errors.stream().mapToLong(e -> "CRITICAL".equals(e.getErrorSeverity()) ? 1 : 0).sum();

        return ErrorAnalysis.builder()
                .processId(processId)
                .totalErrors(errors.size())
                .errorsByType(errorsByType)
                .errorsBySeverity(errorsBySeverity)
                .errorsByCategory(errorsByCategory)
                .recoverableErrors((int) recoverableErrors)
                .resolvedErrors((int) resolvedErrors)
                .userFacingErrors((int) userFacingErrors)
                .criticalErrors((int) criticalErrors)
                .errors(errors)
                .build();
    }

    /**
     * Get unresolved errors
     */
    public List<ErrorEvent> getUnresolvedErrors() {
        log.info("Getting all unresolved errors");
        return errorEventRepository.findByIsResolvedFalse();
    }

    /**
     * Get critical errors
     */
    public List<ErrorEvent> getCriticalErrors() {
        log.info("Getting all critical errors");
        return errorEventRepository.findByErrorSeverity("CRITICAL");
    }

    /**
     * Get user-facing errors
     */
    public List<ErrorEvent> getUserFacingErrors() {
        log.info("Getting all user-facing errors");
        return errorEventRepository.findByIsUserFacingTrue();
    }

    /**
     * Get errors by correlation ID
     */
    public List<ErrorEvent> getErrorsByCorrelationId(String correlationId) {
        log.info("[CORRELATION:{}] Getting all errors", correlationId);
        return errorEventRepository.findByCorrelationId(correlationId);
    }

    /**
     * Get error statistics
     */
    public ErrorStatistics getErrorStatistics(LocalDateTime since) {
        log.info("Getting error statistics since: {}", since);

        List<ErrorEvent> recentErrors = errorEventRepository.findByTimestampBetween(since, LocalDateTime.now());
        
        if (recentErrors.isEmpty()) {
            return ErrorStatistics.builder()
                    .totalErrors(0)
                    .errorsByType(Map.of())
                    .errorsBySeverity(Map.of())
                    .errorsByCategory(Map.of())
                    .resolutionRate(0.0)
                    .averageResolutionTime(0.0)
                    .build();
        }

        Map<String, Long> errorsByType = recentErrors.stream()
                .collect(Collectors.groupingBy(ErrorEvent::getErrorType, Collectors.counting()));

        Map<String, Long> errorsBySeverity = recentErrors.stream()
                .collect(Collectors.groupingBy(ErrorEvent::getErrorSeverity, Collectors.counting()));

        Map<String, Long> errorsByCategory = recentErrors.stream()
                .collect(Collectors.groupingBy(ErrorEvent::getErrorCategory, Collectors.counting()));

        long resolvedErrors = recentErrors.stream().mapToLong(e -> e.isResolved() ? 1 : 0).sum();
        double resolutionRate = (double) resolvedErrors / recentErrors.size() * 100.0;

        double averageResolutionTime = recentErrors.stream()
                .filter(ErrorEvent::isResolved)
                .filter(e -> e.getResolvedAt() != null && e.getTimestamp() != null)
                .mapToLong(e -> java.time.Duration.between(e.getTimestamp(), e.getResolvedAt()).toMinutes())
                .average()
                .orElse(0.0);

        return ErrorStatistics.builder()
                .totalErrors(recentErrors.size())
                .errorsByType(errorsByType)
                .errorsBySeverity(errorsBySeverity)
                .errorsByCategory(errorsByCategory)
                .resolutionRate(resolutionRate)
                .averageResolutionTime(averageResolutionTime)
                .build();
    }

    // Helper methods for error classification
    private String determineErrorType(Exception exception) {
        if (exception instanceof IllegalArgumentException) return "VALIDATION_ERROR";
        if (exception instanceof SecurityException) return "SECURITY_ERROR";
        if (exception instanceof java.net.SocketTimeoutException) return "TIMEOUT_ERROR";
        if (exception instanceof java.net.ConnectException) return "CONNECTION_ERROR";
        if (exception instanceof OutOfMemoryError) return "RESOURCE_ERROR";
        return "SYSTEM_ERROR";
    }

    private String determineErrorCategory(Exception exception) {
        if (exception instanceof IllegalArgumentException) return "BUSINESS";
        if (exception instanceof SecurityException) return "SECURITY";
        if (exception instanceof java.net.SocketTimeoutException) return "INFRASTRUCTURE";
        if (exception instanceof java.net.ConnectException) return "INFRASTRUCTURE";
        if (exception instanceof OutOfMemoryError) return "TECHNICAL";
        return "TECHNICAL";
    }

    private String determineErrorSeverity(Exception exception) {
        if (exception instanceof SecurityException) return "CRITICAL";
        if (exception instanceof OutOfMemoryError) return "CRITICAL";
        if (exception instanceof java.net.SocketTimeoutException) return "HIGH";
        if (exception instanceof java.net.ConnectException) return "HIGH";
        if (exception instanceof IllegalArgumentException) return "MEDIUM";
        return "MEDIUM";
    }

    private boolean isRecoverableException(Exception exception) {
        return !(exception instanceof SecurityException) && 
               !(exception instanceof OutOfMemoryError);
    }

    private String determineRecoveryAction(Exception exception) {
        if (exception instanceof java.net.SocketTimeoutException) return "RETRY";
        if (exception instanceof java.net.ConnectException) return "RETRY";
        if (exception instanceof IllegalArgumentException) return "VALIDATE_INPUT";
        return "MANUAL_INTERVENTION";
    }

    private String determineImpactLevel(Exception exception) {
        if (exception instanceof SecurityException) return "CRITICAL";
        if (exception instanceof OutOfMemoryError) return "CRITICAL";
        if (exception instanceof java.net.SocketTimeoutException) return "HIGH";
        if (exception instanceof java.net.ConnectException) return "HIGH";
        return "MEDIUM";
    }

    private String determineBusinessImpact(Exception exception) {
        if (exception instanceof SecurityException) return "Security violation detected";
        if (exception instanceof OutOfMemoryError) return "System resource exhaustion";
        if (exception instanceof java.net.SocketTimeoutException) return "External service timeout";
        if (exception instanceof java.net.ConnectException) return "External service unavailable";
        return "Processing interrupted";
    }

    private String determineTechnicalImpact(Exception exception) {
        if (exception instanceof SecurityException) return "Security breach";
        if (exception instanceof OutOfMemoryError) return "Memory exhaustion";
        if (exception instanceof java.net.SocketTimeoutException) return "Network timeout";
        if (exception instanceof java.net.ConnectException) return "Network connectivity issue";
        return "Application error";
    }

    private boolean isUserFacingException(Exception exception) {
        return exception instanceof IllegalArgumentException;
    }

    private String getStackTrace(Exception exception) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

    private String extractComponentFromStackTrace(Exception exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace.length > 0) {
            return stackTrace[0].getClassName();
        }
        return "Unknown";
    }

    private String extractMethodFromStackTrace(Exception exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace.length > 0) {
            return stackTrace[0].getMethodName();
        }
        return "Unknown";
    }

    private int extractLineNumberFromStackTrace(Exception exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace.length > 0) {
            return stackTrace[0].getLineNumber();
        }
        return 0;
    }

    // Data transfer objects
    @lombok.Data
    @lombok.Builder
    public static class ErrorEventRequest {
        private String processId;
        private String stepId;
        private String correlationId;
        private String errorType;
        private String errorCategory;
        private String errorSeverity;
        private String errorCode;
        private String errorMessage;
        private String errorDescription;
        private String exceptionClass;
        private String stackTrace;
        private String errorSource;
        private String errorComponent;
        private String errorMethod;
        private int errorLineNumber;
        private Map<String, Object> errorContext;
        private Map<String, Object> errorData;
        private Object errorPayload;
        private String errorPayloadType;
        private boolean isRecoverable;
        private String recoveryAction;
        private int retryCount;
        private int maxRetries;
        private String retryStrategy;
        private String impactLevel;
        private String businessImpact;
        private String technicalImpact;
        private boolean isUserFacing;
        private int affectedUsers;
        private String systemVersion;
        private String environment;
        private String region;
        private Map<String, Object> systemContext;
    }

    @lombok.Data
    @lombok.Builder
    public static class ErrorAnalysis {
        private String processId;
        private int totalErrors;
        private Map<String, Long> errorsByType;
        private Map<String, Long> errorsBySeverity;
        private Map<String, Long> errorsByCategory;
        private int recoverableErrors;
        private int resolvedErrors;
        private int userFacingErrors;
        private int criticalErrors;
        private List<ErrorEvent> errors;
    }

    @lombok.Data
    @lombok.Builder
    public static class ErrorStatistics {
        private int totalErrors;
        private Map<String, Long> errorsByType;
        private Map<String, Long> errorsBySeverity;
        private Map<String, Long> errorsByCategory;
        private double resolutionRate;
        private double averageResolutionTime;
    }
}
