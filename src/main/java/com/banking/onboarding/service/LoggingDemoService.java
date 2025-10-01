package com.banking.onboarding.service;

import com.banking.onboarding.logging.ErrorEventService;
import com.banking.onboarding.logging.LoggingEventService;
import com.banking.onboarding.service.CorrelationIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to demonstrate logging functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoggingDemoService {

    private final LoggingEventService loggingEventService;
    private final ErrorEventService errorEventService;
    private final CorrelationIdService correlationIdService;

    /**
     * Demonstrate business event logging
     */
    public void logBusinessEvent(String eventName, Map<String, Object> eventData) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        Map<String, Object> businessData = new HashMap<>();
        businessData.put("eventName", eventName);
        businessData.put("eventData", eventData);
        businessData.put("timestamp", System.currentTimeMillis());
        
        loggingEventService.logBusinessEvent(
            "BUSINESS_OPERATION",
            eventName,
            businessData,
            correlationId,
            traceId
        );
    }

    /**
     * Demonstrate performance metrics logging
     */
    public void logPerformanceMetrics(String operation, long durationMs, Map<String, Object> metrics) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        Map<String, Object> performanceData = new HashMap<>();
        performanceData.put("operation", operation);
        performanceData.put("durationMs", durationMs);
        performanceData.put("metrics", metrics);
        
        loggingEventService.logPerformanceMetrics(
            operation,
            durationMs,
            performanceData,
            correlationId,
            traceId
        );
    }

    /**
     * Demonstrate external API call logging
     */
    public void logExternalApiCall(String method, String url, Map<String, String> headers, String body) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        loggingEventService.logExternalApiCall(
            method,
            url,
            headers,
            body,
            correlationId,
            traceId
        );
    }

    /**
     * Demonstrate external API response logging
     */
    public void logExternalApiResponse(String method, String url, int statusCode, 
                                     Map<String, String> headers, String body, long durationMs) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        loggingEventService.logExternalApiResponse(
            method,
            url,
            statusCode,
            headers,
            body,
            correlationId,
            traceId,
            durationMs
        );
    }

    /**
     * Demonstrate error logging
     */
    public void demonstrateErrorLogging() {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        try {
            // Simulate an error
            throw new RuntimeException("Simulated error for demonstration");
        } catch (Exception e) {
            // Log different types of errors
            errorEventService.logApplicationError(
                "Demonstration error occurred",
                correlationId,
                traceId,
                "LoggingDemoService",
                "demonstrateErrorLogging",
                e
            );
            
            // Log validation error
            Map<String, Object> validationErrors = new HashMap<>();
            validationErrors.put("field", "testField");
            validationErrors.put("value", "invalidValue");
            
            errorEventService.logValidationError(
                "Validation failed for demonstration",
                correlationId,
                traceId,
                "LoggingDemoService",
                "demonstrateErrorLogging",
                validationErrors
            );
            
            // Log business logic error
            Map<String, Object> businessContext = new HashMap<>();
            businessContext.put("rule", "DEMO_RULE");
            businessContext.put("context", "demonstration");
            
            errorEventService.logBusinessLogicError(
                "DEMO_BUSINESS_RULE",
                "Business rule violation for demonstration",
                correlationId,
                traceId,
                "LoggingDemoService",
                "demonstrateErrorLogging",
                businessContext
            );
        }
    }

    /**
     * Demonstrate timeout error logging
     */
    public void demonstrateTimeoutErrorLogging() {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        try {
            // Simulate a timeout
            Thread.sleep(100); // Simulate some work
            throw new java.util.concurrent.TimeoutException("Operation timed out");
        } catch (Exception e) {
            errorEventService.logTimeoutError(
                "demonstrateTimeoutErrorLogging",
                5000L, // 5 second timeout
                "Operation timed out during demonstration",
                correlationId,
                traceId,
                e
            );
        }
    }

    /**
     * Demonstrate external service error logging
     */
    public void demonstrateExternalServiceErrorLogging() {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();
        
        try {
            // Simulate external service error
            throw new RuntimeException("External service unavailable");
        } catch (Exception e) {
            errorEventService.logExternalServiceError(
                "DemoExternalService",
                "/api/v1/demo",
                503,
                "External service unavailable",
                correlationId,
                traceId,
                e
            );
        }
    }

    /**
     * Generate trace ID
     */
    private String generateTraceId() {
        return "TRACE-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
