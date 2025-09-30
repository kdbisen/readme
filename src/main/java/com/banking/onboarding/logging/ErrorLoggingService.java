package com.banking.onboarding.logging;

import com.banking.onboarding.service.CorrelationIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for logging errors to MongoDB with correlation tracking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorLoggingService {
    
    private final MongoTemplate mongoTemplate;
    private final CorrelationIdService correlationIdService;
    
    /**
     * Log error to MongoDB with full context
     */
    public void logError(String errorType, String errorMessage, Throwable throwable, Map<String, Object> context) {
        try {
            Map<String, Object> errorLog = new HashMap<>();
            
            // Basic error information
            errorLog.put("timestamp", Instant.now());
            errorLog.put("errorType", errorType);
            errorLog.put("errorMessage", errorMessage);
            errorLog.put("correlationId", correlationIdService.getCurrentCorrelationId());
            
            // Exception details
            if (throwable != null) {
                errorLog.put("exceptionClass", throwable.getClass().getSimpleName());
                errorLog.put("exceptionMessage", throwable.getMessage());
                errorLog.put("stackTrace", getStackTrace(throwable));
            }
            
            // Additional context
            if (context != null) {
                errorLog.putAll(context);
            }
            
            // Service metadata
            errorLog.put("service", "banking-onboarding-service");
            errorLog.put("environment", System.getProperty("spring.profiles.active", "dev"));
            errorLog.put("version", System.getProperty("app.version", "1.0.0"));
            
            // Save to MongoDB
            mongoTemplate.save(errorLog, "error_logs");
            
            log.error("Error logged to MongoDB - Type: {}, Message: {}, CorrelationId: {}", 
                    errorType, errorMessage, correlationIdService.getCurrentCorrelationId());
            
        } catch (Exception e) {
            log.error("Failed to log error to MongoDB", e);
        }
    }
    
    /**
     * Log API error
     */
    public void logApiError(String endpoint, String method, int statusCode, String errorMessage, 
                           Object requestPayload, Object responsePayload) {
        Map<String, Object> context = new HashMap<>();
        context.put("endpoint", endpoint);
        context.put("method", method);
        context.put("statusCode", statusCode);
        context.put("requestPayload", requestPayload);
        context.put("responsePayload", responsePayload);
        
        logError("API_ERROR", errorMessage, null, context);
    }
    
    /**
     * Log processing error
     */
    public void logProcessingError(String processId, String step, String errorMessage, Throwable throwable) {
        Map<String, Object> context = new HashMap<>();
        context.put("processId", processId);
        context.put("step", step);
        context.put("processingType", "FUNCTIONAL");
        
        logError("PROCESSING_ERROR", errorMessage, throwable, context);
    }
    
    /**
     * Log proxy error
     */
    public void logProxyError(String proxyUrl, String fenergoEndpoint, String errorMessage, Throwable throwable) {
        Map<String, Object> context = new HashMap<>();
        context.put("proxyUrl", proxyUrl);
        context.put("fenergoEndpoint", fenergoEndpoint);
        context.put("errorSource", "PROXY");
        
        logError("PROXY_ERROR", errorMessage, throwable, context);
    }
    
    /**
     * Log validation error
     */
    public void logValidationError(String entityType, String validationRule, String errorMessage, Object entityData) {
        Map<String, Object> context = new HashMap<>();
        context.put("entityType", entityType);
        context.put("validationRule", validationRule);
        context.put("entityData", entityData);
        
        logError("VALIDATION_ERROR", errorMessage, null, context);
    }
    
    /**
     * Get stack trace as string
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
