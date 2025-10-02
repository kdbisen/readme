package com.banking.onboarding.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Audit Logging Service
 * Tracks all critical operations for compliance and security
 */
@Slf4j
@Service
public class AuditService {
    
    private final MongoTemplate mongoTemplate;
    
    public AuditService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    /**
     * Log audit event synchronously
     */
    public void logAuditEvent(AuditEvent event) {
        try {
            mongoTemplate.save(event, "audit_events");
            log.debug("Audit event logged: {} - {}", event.getEventType(), event.getEventId());
        } catch (Exception e) {
            log.error("Failed to log audit event: {}", event.getEventId(), e);
        }
    }
    
    /**
     * Log audit event asynchronously
     */
    public CompletableFuture<Void> logAuditEventAsync(AuditEvent event) {
        return CompletableFuture.runAsync(() -> logAuditEvent(event));
    }
    
    /**
     * Log process start event
     */
    public void logProcessStart(String processId, String correlationId, String userId, 
                              String requestType, Map<String, Object> inputData) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.PROCESS_START)
                .processId(processId)
                .correlationId(correlationId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .details(Map.of(
                    "requestType", requestType,
                    "inputDataSize", inputData != null ? inputData.size() : 0
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log process completion event
     */
    public void logProcessCompletion(String processId, String correlationId, String userId, 
                                   boolean success, String status, Map<String, Object> resultData) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.PROCESS_COMPLETION)
                .processId(processId)
                .correlationId(correlationId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .success(success)
                .details(Map.of(
                    "status", status,
                    "resultDataSize", resultData != null ? resultData.size() : 0
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log step execution event
     */
    public void logStepExecution(String processId, String correlationId, String stepName, 
                               boolean success, long durationMs, Map<String, Object> stepData) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.STEP_EXECUTION)
                .processId(processId)
                .correlationId(correlationId)
                .stepName(stepName)
                .timestamp(LocalDateTime.now())
                .success(success)
                .durationMs(durationMs)
                .details(Map.of(
                    "stepDataSize", stepData != null ? stepData.size() : 0
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log external API call event
     */
    public void logExternalApiCall(String processId, String correlationId, String apiProvider, 
                                 String endpoint, String method, boolean success, 
                                 int statusCode, long durationMs) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.EXTERNAL_API_CALL)
                .processId(processId)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .success(success)
                .durationMs(durationMs)
                .details(Map.of(
                    "apiProvider", apiProvider,
                    "endpoint", endpoint,
                    "method", method,
                    "statusCode", statusCode
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log authentication event
     */
    public void logAuthentication(String userId, String authMethod, boolean success, 
                                String ipAddress, String userAgent) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.AUTHENTICATION)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .success(success)
                .details(Map.of(
                    "authMethod", authMethod,
                    "ipAddress", ipAddress != null ? ipAddress : "unknown",
                    "userAgent", userAgent != null ? userAgent : "unknown"
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log data access event
     */
    public void logDataAccess(String userId, String dataType, String operation, 
                            boolean success, String resourceId) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.DATA_ACCESS)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .success(success)
                .details(Map.of(
                    "dataType", dataType,
                    "operation", operation,
                    "resourceId", resourceId != null ? resourceId : "unknown"
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log security event
     */
    public void logSecurityEvent(String eventType, String userId, String ipAddress, 
                               String description, Map<String, Object> additionalData) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.SECURITY_EVENT)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .success(false) // Security events are typically failures
                .details(Map.of(
                    "securityEventType", eventType,
                    "ipAddress", ipAddress != null ? ipAddress : "unknown",
                    "description", description,
                    "additionalData", additionalData != null ? additionalData : Map.of()
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log configuration change event
     */
    public void logConfigurationChange(String userId, String configKey, String oldValue, 
                                     String newValue, String reason) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.CONFIGURATION_CHANGE)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .success(true)
                .details(Map.of(
                    "configKey", configKey,
                    "oldValue", oldValue != null ? oldValue : "null",
                    "newValue", newValue != null ? newValue : "null",
                    "reason", reason != null ? reason : "no reason provided"
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    // ===========================================
    // ERROR LOGGING METHODS
    // ===========================================
    
    /**
     * Log application error event
     */
    public void logError(String processId, String correlationId, String errorType, 
                        String errorMessage, String stackTrace, Map<String, Object> context) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.ERROR_EVENT)
                .processId(processId)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .success(false)
                .details(Map.of(
                    "errorType", errorType != null ? errorType : "UNKNOWN_ERROR",
                    "errorMessage", errorMessage != null ? errorMessage : "No error message",
                    "stackTrace", stackTrace != null ? stackTrace : "No stack trace",
                    "context", context != null ? context : Map.of(),
                    "severity", determineSeverity(errorType)
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log validation error event
     */
    public void logValidationError(String processId, String correlationId, String fieldName, 
                                  String validationRule, String errorMessage, Object inputValue) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.ERROR_EVENT)
                .processId(processId)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .success(false)
                .details(Map.of(
                    "errorType", "VALIDATION_ERROR",
                    "fieldName", fieldName != null ? fieldName : "unknown",
                    "validationRule", validationRule != null ? validationRule : "unknown",
                    "errorMessage", errorMessage != null ? errorMessage : "Validation failed",
                    "inputValue", inputValue != null ? inputValue.toString() : "null",
                    "severity", "MEDIUM"
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log external API error event
     */
    public void logExternalApiError(String processId, String correlationId, String apiProvider, 
                                   String endpoint, int statusCode, String errorMessage, 
                                   String responseBody, long durationMs) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.ERROR_EVENT)
                .processId(processId)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .success(false)
                .durationMs(durationMs)
                .details(Map.of(
                    "errorType", "EXTERNAL_API_ERROR",
                    "apiProvider", apiProvider != null ? apiProvider : "unknown",
                    "endpoint", endpoint != null ? endpoint : "unknown",
                    "statusCode", statusCode,
                    "errorMessage", errorMessage != null ? errorMessage : "API call failed",
                    "responseBody", responseBody != null ? responseBody : "No response body",
                    "severity", determineApiErrorSeverity(statusCode)
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log step execution error event
     */
    public void logStepError(String processId, String correlationId, String stepName, 
                            String errorType, String errorMessage, String stackTrace, 
                            long durationMs, Map<String, Object> stepContext) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.ERROR_EVENT)
                .processId(processId)
                .correlationId(correlationId)
                .stepName(stepName)
                .timestamp(LocalDateTime.now())
                .success(false)
                .durationMs(durationMs)
                .details(Map.of(
                    "errorType", errorType != null ? errorType : "STEP_EXECUTION_ERROR",
                    "stepName", stepName != null ? stepName : "unknown",
                    "errorMessage", errorMessage != null ? errorMessage : "Step execution failed",
                    "stackTrace", stackTrace != null ? stackTrace : "No stack trace",
                    "stepContext", stepContext != null ? stepContext : Map.of(),
                    "severity", "HIGH"
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log system error event
     */
    public void logSystemError(String component, String operation, String errorMessage, 
                              String errorCode, String severity) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.ERROR_EVENT)
                .timestamp(LocalDateTime.now())
                .success(false)
                .details(Map.of(
                    "errorType", "SYSTEM_ERROR",
                    "component", component != null ? component : "unknown",
                    "operation", operation != null ? operation : "unknown",
                    "errorMessage", errorMessage != null ? errorMessage : "System error occurred",
                    "errorCode", errorCode != null ? errorCode : "UNKNOWN",
                    "severity", severity != null ? severity : "MEDIUM"
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log operation failure error event
     */
    public void logOperationFailure(String operation, String errorMessage, 
                                   String errorCode, long durationMs) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.ERROR_EVENT)
                .timestamp(LocalDateTime.now())
                .success(false)
                .durationMs(durationMs)
                .details(Map.of(
                    "errorType", "OPERATION_FAILURE",
                    "operation", operation != null ? operation : "unknown",
                    "errorMessage", errorMessage != null ? errorMessage : "Operation failed",
                    "errorCode", errorCode != null ? errorCode : "UNKNOWN",
                    "severity", "HIGH"
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log rate limit error event
     */
    public void logRateLimitError(String clientId, String endpoint, String rateLimitKey, 
                                 int currentUsage, int limit) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.ERROR_EVENT)
                .timestamp(LocalDateTime.now())
                .success(false)
                .details(Map.of(
                    "errorType", "RATE_LIMIT_EXCEEDED",
                    "clientId", clientId != null ? clientId : "unknown",
                    "endpoint", endpoint != null ? endpoint : "unknown",
                    "rateLimitKey", rateLimitKey != null ? rateLimitKey : "unknown",
                    "currentUsage", currentUsage,
                    "limit", limit,
                    "severity", "MEDIUM"
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log database error event
     */
    public void logDatabaseError(String operation, String collection, String errorMessage, 
                                String stackTrace, Map<String, Object> queryContext) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.ERROR_EVENT)
                .timestamp(LocalDateTime.now())
                .success(false)
                .details(Map.of(
                    "errorType", "DATABASE_ERROR",
                    "operation", operation != null ? operation : "unknown",
                    "collection", collection != null ? collection : "unknown",
                    "errorMessage", errorMessage != null ? errorMessage : "Database operation failed",
                    "stackTrace", stackTrace != null ? stackTrace : "No stack trace",
                    "queryContext", queryContext != null ? queryContext : Map.of(),
                    "severity", "HIGH"
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log system error event
     */
    public void logSystemError(String component, String errorType, String errorMessage, 
                              String stackTrace, Map<String, Object> systemContext) {
        AuditEvent event = AuditEvent.builder()
                .eventId(generateEventId())
                .eventType(AuditEventType.SYSTEM_EVENT)
                .timestamp(LocalDateTime.now())
                .success(false)
                .details(Map.of(
                    "errorType", errorType != null ? errorType : "SYSTEM_ERROR",
                    "component", component != null ? component : "unknown",
                    "errorMessage", errorMessage != null ? errorMessage : "System error occurred",
                    "stackTrace", stackTrace != null ? stackTrace : "No stack trace",
                    "systemContext", systemContext != null ? systemContext : Map.of(),
                    "severity", "CRITICAL"
                ))
                .build();
        
        logAuditEvent(event);
    }
    
    // ===========================================
    // UTILITY METHODS FOR ERROR LOGGING
    // ===========================================
    
    /**
     * Determine error severity based on error type
     */
    private String determineSeverity(String errorType) {
        if (errorType == null) return "MEDIUM";
        
        switch (errorType.toUpperCase()) {
            case "VALIDATION_ERROR":
            case "RATE_LIMIT_EXCEEDED":
                return "MEDIUM";
            case "EXTERNAL_API_ERROR":
            case "STEP_EXECUTION_ERROR":
            case "DATABASE_ERROR":
                return "HIGH";
            case "CIRCUIT_BREAKER_ERROR":
            case "RETRY_FAILURE":
            case "SYSTEM_ERROR":
                return "CRITICAL";
            default:
                return "MEDIUM";
        }
    }
    
    /**
     * Determine API error severity based on status code
     */
    private String determineApiErrorSeverity(int statusCode) {
        if (statusCode >= 400 && statusCode < 500) {
            return "MEDIUM"; // Client errors
        } else if (statusCode >= 500) {
            return "HIGH";   // Server errors
        } else {
            return "LOW";    // Other status codes
        }
    }
    
    /**
     * Log error with automatic context extraction
     */
    public void logErrorWithContext(Exception exception, String processId, String correlationId, 
                                   Map<String, Object> additionalContext) {
        Map<String, Object> context = Map.of(
            "exceptionClass", exception.getClass().getSimpleName(),
            "exceptionMessage", exception.getMessage() != null ? exception.getMessage() : "No message",
            "additionalContext", additionalContext != null ? additionalContext : Map.of()
        );
        
        logError(processId, correlationId, exception.getClass().getSimpleName(), 
                exception.getMessage(), getStackTrace(exception), context);
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
    
    private String generateEventId() {
        return "AUDIT_" + System.currentTimeMillis() + "_" + 
               java.util.UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Audit event types
     */
    public enum AuditEventType {
        PROCESS_START,
        PROCESS_COMPLETION,
        STEP_EXECUTION,
        EXTERNAL_API_CALL,
        AUTHENTICATION,
        DATA_ACCESS,
        SECURITY_EVENT,
        CONFIGURATION_CHANGE,
        ERROR_EVENT,
        SYSTEM_EVENT
    }
}
