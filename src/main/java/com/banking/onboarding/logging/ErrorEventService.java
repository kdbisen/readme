package com.banking.onboarding.logging;

import com.banking.onboarding.model.ErrorEvent;
import com.banking.onboarding.repository.ErrorEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Error Event Service
 * Stores all errors and exceptions in the database with correlation IDs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorEventService {

    private final ErrorEventRepository errorEventRepository;

    /**
     * Log and store error event
     */
    public void logErrorEvent(String errorType, String errorMessage, String correlationId, 
                             String traceId, String serviceName, String methodName, 
                             Throwable exception, Map<String, Object> contextData) {
        try {
            ErrorEvent errorEvent = ErrorEvent.builder()
                    .errorType(errorType)
                    .errorMessage(errorMessage)
                    .correlationId(correlationId)
                    .traceId(traceId)
                    .serviceName(serviceName)
                    .methodName(methodName)
                    .exceptionType(exception != null ? exception.getClass().getSimpleName() : null)
                    .stackTrace(exception != null ? getStackTrace(exception) : null)
                    .contextData(contextData != null ? contextData : new HashMap<>())
                    .timestamp(LocalDateTime.now())
                    .environment(System.getProperty("ENVIRONMENT", "dev"))
                    .version(System.getProperty("APP_VERSION", "1.0.0"))
                    .build();

            errorEventRepository.save(errorEvent);

            log.error("Error event stored - Type: {}, Message: {}, CorrelationId: {}, TraceId: {}", 
                    errorType, errorMessage, correlationId, traceId);

        } catch (Exception e) {
            log.error("Failed to store error event: {}", e.getMessage(), e);
        }
    }

    /**
     * Log application error
     */
    public void logApplicationError(String errorMessage, String correlationId, String traceId, 
                                  String serviceName, String methodName, Throwable exception) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("errorCategory", "APPLICATION_ERROR");
        contextData.put("severity", "ERROR");
        
        logErrorEvent("APPLICATION_ERROR", errorMessage, correlationId, traceId, 
                     serviceName, methodName, exception, contextData);
    }

    /**
     * Log validation error
     */
    public void logValidationError(String errorMessage, String correlationId, String traceId, 
                                 String serviceName, String methodName, Map<String, Object> validationErrors) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("errorCategory", "VALIDATION_ERROR");
        contextData.put("severity", "WARN");
        contextData.put("validationErrors", validationErrors);
        
        logErrorEvent("VALIDATION_ERROR", errorMessage, correlationId, traceId, 
                     serviceName, methodName, null, contextData);
    }

    /**
     * Log external service error
     */
    public void logExternalServiceError(String serviceName, String endpoint, int statusCode, 
                                       String errorMessage, String correlationId, String traceId, 
                                       Throwable exception) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("errorCategory", "EXTERNAL_SERVICE_ERROR");
        contextData.put("severity", "ERROR");
        contextData.put("externalService", serviceName);
        contextData.put("endpoint", endpoint);
        contextData.put("statusCode", statusCode);
        
        logErrorEvent("EXTERNAL_SERVICE_ERROR", errorMessage, correlationId, traceId, 
                     "banking-onboarding-service", "external-service-call", exception, contextData);
    }

    /**
     * Log database error
     */
    public void logDatabaseError(String operation, String errorMessage, String correlationId, 
                               String traceId, Throwable exception) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("errorCategory", "DATABASE_ERROR");
        contextData.put("severity", "ERROR");
        contextData.put("databaseOperation", operation);
        
        logErrorEvent("DATABASE_ERROR", errorMessage, correlationId, traceId, 
                     "banking-onboarding-service", "database-operation", exception, contextData);
    }

    /**
     * Log authentication error
     */
    public void logAuthenticationError(String errorMessage, String correlationId, String traceId, 
                                     String serviceName, String methodName, Throwable exception) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("errorCategory", "AUTHENTICATION_ERROR");
        contextData.put("severity", "WARN");
        
        logErrorEvent("AUTHENTICATION_ERROR", errorMessage, correlationId, traceId, 
                     serviceName, methodName, exception, contextData);
    }

    /**
     * Log authorization error
     */
    public void logAuthorizationError(String errorMessage, String correlationId, String traceId, 
                                   String serviceName, String methodName, String resource) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("errorCategory", "AUTHORIZATION_ERROR");
        contextData.put("severity", "WARN");
        contextData.put("resource", resource);
        
        logErrorEvent("AUTHORIZATION_ERROR", errorMessage, correlationId, traceId, 
                     serviceName, methodName, null, contextData);
    }

    /**
     * Log timeout error
     */
    public void logTimeoutError(String operation, long timeoutMs, String errorMessage, 
                              String correlationId, String traceId, Throwable exception) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("errorCategory", "TIMEOUT_ERROR");
        contextData.put("severity", "ERROR");
        contextData.put("operation", operation);
        contextData.put("timeoutMs", timeoutMs);
        
        logErrorEvent("TIMEOUT_ERROR", errorMessage, correlationId, traceId, 
                     "banking-onboarding-service", operation, exception, contextData);
    }

    /**
     * Log configuration error
     */
    public void logConfigurationError(String configKey, String errorMessage, String correlationId, 
                                   String traceId, Throwable exception) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("errorCategory", "CONFIGURATION_ERROR");
        contextData.put("severity", "ERROR");
        contextData.put("configKey", configKey);
        
        logErrorEvent("CONFIGURATION_ERROR", errorMessage, correlationId, traceId, 
                     "banking-onboarding-service", "configuration-load", exception, contextData);
    }

    /**
     * Log business logic error
     */
    public void logBusinessLogicError(String businessRule, String errorMessage, String correlationId, 
                                   String traceId, String serviceName, String methodName, 
                                   Map<String, Object> businessContext) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("errorCategory", "BUSINESS_LOGIC_ERROR");
        contextData.put("severity", "WARN");
        contextData.put("businessRule", businessRule);
        contextData.put("businessContext", businessContext);
        
        logErrorEvent("BUSINESS_LOGIC_ERROR", errorMessage, correlationId, traceId, 
                     serviceName, methodName, null, contextData);
    }

    /**
     * Log system error
     */
    public void logSystemError(String component, String errorMessage, String correlationId, 
                             String traceId, Throwable exception) {
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("errorCategory", "SYSTEM_ERROR");
        contextData.put("severity", "ERROR");
        contextData.put("component", component);
        
        logErrorEvent("SYSTEM_ERROR", errorMessage, correlationId, traceId, 
                     "banking-onboarding-service", component, exception, contextData);
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Throwable exception) {
        if (exception == null) return null;
        
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Get error statistics
     */
    public Map<String, Object> getErrorStatistics(String correlationId) {
        try {
            // This would typically involve aggregation queries
            // For now, return basic statistics
            Map<String, Object> stats = new HashMap<>();
            stats.put("correlationId", correlationId);
            stats.put("timestamp", LocalDateTime.now());
            stats.put("message", "Error statistics retrieval not implemented yet");
            return stats;
        } catch (Exception e) {
            log.error("Failed to get error statistics: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}
