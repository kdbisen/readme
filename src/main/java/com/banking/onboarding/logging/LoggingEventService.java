package com.banking.onboarding.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive Logging Event Service
 * Logs all inbound/outbound requests and responses with correlation IDs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoggingEventService {

    private final ObjectMapper objectMapper;

    /**
     * Log inbound request
     */
    public void logInboundRequest(String method, String uri, Map<String, String> headers, 
                                 String body, String correlationId, String traceId) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("eventType", "INBOUND_REQUEST");
            logData.put("method", method);
            logData.put("uri", uri);
            logData.put("headers", sanitizeHeaders(headers));
            logData.put("body", sanitizeBody(body));
            logData.put("correlationId", correlationId);
            logData.put("traceId", traceId);
            logData.put("timestamp", LocalDateTime.now());
            logData.put("service", "banking-onboarding-service");
            logData.put("environment", System.getProperty("ENVIRONMENT", "dev"));
            logData.put("version", System.getProperty("APP_VERSION", "1.0.0"));

            // Set MDC for structured logging
            MDC.put("correlationId", correlationId);
            MDC.put("traceId", traceId);
            MDC.put("eventType", "INBOUND_REQUEST");
            MDC.put("method", method);
            MDC.put("uri", uri);

            log.info("Inbound request: {} {} - CorrelationId: {}, TraceId: {}", 
                    method, uri, correlationId, traceId);

            // Log detailed request data as JSON
            log.debug("Inbound request details: {}", objectMapper.writeValueAsString(logData));

        } catch (Exception e) {
            log.error("Failed to log inbound request: {}", e.getMessage(), e);
        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    /**
     * Log outbound response
     */
    public void logOutboundResponse(String method, String uri, int statusCode, 
                                  Map<String, String> headers, String body, 
                                  String correlationId, String traceId, long durationMs) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("eventType", "OUTBOUND_RESPONSE");
            logData.put("method", method);
            logData.put("uri", uri);
            logData.put("statusCode", statusCode);
            logData.put("headers", sanitizeHeaders(headers));
            logData.put("body", sanitizeBody(body));
            logData.put("correlationId", correlationId);
            logData.put("traceId", traceId);
            logData.put("durationMs", durationMs);
            logData.put("timestamp", LocalDateTime.now());
            logData.put("service", "banking-onboarding-service");
            logData.put("environment", System.getProperty("ENVIRONMENT", "dev"));
            logData.put("version", System.getProperty("APP_VERSION", "1.0.0"));

            // Set MDC for structured logging
            MDC.put("correlationId", correlationId);
            MDC.put("traceId", traceId);
            MDC.put("eventType", "OUTBOUND_RESPONSE");
            MDC.put("method", method);
            MDC.put("uri", uri);
            MDC.put("statusCode", String.valueOf(statusCode));
            MDC.put("durationMs", String.valueOf(durationMs));

            String logLevel = statusCode >= 400 ? "WARN" : "INFO";
            if (logLevel.equals("WARN")) {
                log.warn("Outbound response: {} {} - Status: {} ({}ms) - CorrelationId: {}, TraceId: {}", 
                        method, uri, statusCode, durationMs, correlationId, traceId);
            } else {
                log.info("Outbound response: {} {} - Status: {} ({}ms) - CorrelationId: {}, TraceId: {}", 
                        method, uri, statusCode, durationMs, correlationId, traceId);
            }

            // Log detailed response data as JSON
            log.debug("Outbound response details: {}", objectMapper.writeValueAsString(logData));

        } catch (Exception e) {
            log.error("Failed to log outbound response: {}", e.getMessage(), e);
        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    /**
     * Log external API call (outbound request)
     */
    public void logExternalApiCall(String method, String url, Map<String, String> headers, 
                                 String body, String correlationId, String traceId) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("eventType", "EXTERNAL_API_CALL");
            logData.put("method", method);
            logData.put("url", url);
            logData.put("headers", sanitizeHeaders(headers));
            logData.put("body", sanitizeBody(body));
            logData.put("correlationId", correlationId);
            logData.put("traceId", traceId);
            logData.put("timestamp", LocalDateTime.now());
            logData.put("service", "banking-onboarding-service");
            logData.put("environment", System.getProperty("ENVIRONMENT", "dev"));
            logData.put("version", System.getProperty("APP_VERSION", "1.0.0"));

            // Set MDC for structured logging
            MDC.put("correlationId", correlationId);
            MDC.put("traceId", traceId);
            MDC.put("eventType", "EXTERNAL_API_CALL");
            MDC.put("method", method);
            MDC.put("url", url);

            log.info("External API call: {} {} - CorrelationId: {}, TraceId: {}", 
                    method, url, correlationId, traceId);

            // Log detailed API call data as JSON
            log.debug("External API call details: {}", objectMapper.writeValueAsString(logData));

        } catch (Exception e) {
            log.error("Failed to log external API call: {}", e.getMessage(), e);
        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    /**
     * Log external API response
     */
    public void logExternalApiResponse(String method, String url, int statusCode, 
                                     Map<String, String> headers, String body, 
                                     String correlationId, String traceId, long durationMs) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("eventType", "EXTERNAL_API_RESPONSE");
            logData.put("method", method);
            logData.put("url", url);
            logData.put("statusCode", statusCode);
            logData.put("headers", sanitizeHeaders(headers));
            logData.put("body", sanitizeBody(body));
            logData.put("correlationId", correlationId);
            logData.put("traceId", traceId);
            logData.put("durationMs", durationMs);
            logData.put("timestamp", LocalDateTime.now());
            logData.put("service", "banking-onboarding-service");
            logData.put("environment", System.getProperty("ENVIRONMENT", "dev"));
            logData.put("version", System.getProperty("APP_VERSION", "1.0.0"));

            // Set MDC for structured logging
            MDC.put("correlationId", correlationId);
            MDC.put("traceId", traceId);
            MDC.put("eventType", "EXTERNAL_API_RESPONSE");
            MDC.put("method", method);
            MDC.put("url", url);
            MDC.put("statusCode", String.valueOf(statusCode));
            MDC.put("durationMs", String.valueOf(durationMs));

            String logLevel = statusCode >= 400 ? "WARN" : "INFO";
            if (logLevel.equals("WARN")) {
                log.warn("External API response: {} {} - Status: {} ({}ms) - CorrelationId: {}, TraceId: {}", 
                        method, url, statusCode, durationMs, correlationId, traceId);
            } else {
                log.info("External API response: {} {} - Status: {} ({}ms) - CorrelationId: {}, TraceId: {}", 
                        method, url, statusCode, durationMs, correlationId, traceId);
            }

            // Log detailed API response data as JSON
            log.debug("External API response details: {}", objectMapper.writeValueAsString(logData));

        } catch (Exception e) {
            log.error("Failed to log external API response: {}", e.getMessage(), e);
        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    /**
     * Log business event
     */
    public void logBusinessEvent(String eventType, String eventName, Map<String, Object> eventData, 
                               String correlationId, String traceId) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("eventType", "BUSINESS_EVENT");
            logData.put("businessEventType", eventType);
            logData.put("eventName", eventName);
            logData.put("eventData", eventData);
            logData.put("correlationId", correlationId);
            logData.put("traceId", traceId);
            logData.put("timestamp", LocalDateTime.now());
            logData.put("service", "banking-onboarding-service");
            logData.put("environment", System.getProperty("ENVIRONMENT", "dev"));
            logData.put("version", System.getProperty("APP_VERSION", "1.0.0"));

            // Set MDC for structured logging
            MDC.put("correlationId", correlationId);
            MDC.put("traceId", traceId);
            MDC.put("eventType", "BUSINESS_EVENT");
            MDC.put("businessEventType", eventType);
            MDC.put("eventName", eventName);

            log.info("Business event: {} - {} - CorrelationId: {}, TraceId: {}", 
                    eventType, eventName, correlationId, traceId);

            // Log detailed business event data as JSON
            log.debug("Business event details: {}", objectMapper.writeValueAsString(logData));

        } catch (Exception e) {
            log.error("Failed to log business event: {}", e.getMessage(), e);
        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    /**
     * Log performance metrics
     */
    public void logPerformanceMetrics(String operation, long durationMs, Map<String, Object> metrics, 
                                    String correlationId, String traceId) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("eventType", "PERFORMANCE_METRICS");
            logData.put("operation", operation);
            logData.put("durationMs", durationMs);
            logData.put("metrics", metrics);
            logData.put("correlationId", correlationId);
            logData.put("traceId", traceId);
            logData.put("timestamp", LocalDateTime.now());
            logData.put("service", "banking-onboarding-service");
            logData.put("environment", System.getProperty("ENVIRONMENT", "dev"));
            logData.put("version", System.getProperty("APP_VERSION", "1.0.0"));

            // Set MDC for structured logging
            MDC.put("correlationId", correlationId);
            MDC.put("traceId", traceId);
            MDC.put("eventType", "PERFORMANCE_METRICS");
            MDC.put("operation", operation);
            MDC.put("durationMs", String.valueOf(durationMs));

            log.info("Performance metrics: {} - Duration: {}ms - CorrelationId: {}, TraceId: {}", 
                    operation, durationMs, correlationId, traceId);

            // Log detailed performance metrics as JSON
            log.debug("Performance metrics details: {}", objectMapper.writeValueAsString(logData));

        } catch (Exception e) {
            log.error("Failed to log performance metrics: {}", e.getMessage(), e);
        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    /**
     * Sanitize headers to remove sensitive information
     */
    private Map<String, String> sanitizeHeaders(Map<String, String> headers) {
        if (headers == null) return new HashMap<>();
        
        Map<String, String> sanitized = new HashMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.contains("authorization") || key.contains("password") || 
                key.contains("secret") || key.contains("token")) {
                sanitized.put(entry.getKey(), "***REDACTED***");
            } else {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }
        return sanitized;
    }

    /**
     * Sanitize body to remove sensitive information
     */
    private String sanitizeBody(String body) {
        if (body == null || body.isEmpty()) return body;
        
        // Truncate very large bodies
        if (body.length() > 10000) {
            return body.substring(0, 10000) + "... [TRUNCATED]";
        }
        
        // Remove sensitive fields from JSON
        try {
            Map<String, Object> jsonBody = objectMapper.readValue(body, Map.class);
            Map<String, Object> sanitized = sanitizeJsonBody(jsonBody);
            return objectMapper.writeValueAsString(sanitized);
        } catch (Exception e) {
            // If not JSON, return as is
            return body;
        }
    }

    /**
     * Recursively sanitize JSON body
     */
    private Map<String, Object> sanitizeJsonBody(Map<String, Object> jsonBody) {
        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonBody.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.contains("password") || key.contains("secret") || 
                key.contains("token") || key.contains("ssn") || 
                key.contains("credit") || key.contains("card")) {
                sanitized.put(entry.getKey(), "***REDACTED***");
            } else if (entry.getValue() instanceof Map) {
                sanitized.put(entry.getKey(), sanitizeJsonBody((Map<String, Object>) entry.getValue()));
            } else {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }
        return sanitized;
    }
}
