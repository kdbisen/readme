package com.banking.onboarding.logging;

import com.banking.onboarding.service.CorrelationIdService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for automatic request/response logging
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestResponseLogger {
    
    private final CorrelationIdService correlationIdService;
    private final ObjectMapper objectMapper;
    
    /**
     * Log incoming request
     */
    public void logRequest(String method, String uri, Map<String, String> headers, Object body) {
        try {
            Map<String, Object> requestLog = new HashMap<>();
            requestLog.put("logType", "REQUEST");
            requestLog.put("timestamp", Instant.now());
            requestLog.put("method", method);
            requestLog.put("uri", uri);
            requestLog.put("headers", sanitizeHeaders(headers));
            requestLog.put("body", sanitizeBody(body));
            requestLog.put("correlationId", correlationIdService.getCurrentCorrelationId());
            
            // Add to MDC for structured logging
            MDC.put("requestMethod", method);
            MDC.put("requestUri", uri);
            
            log.info("Incoming request: {} {}", method, uri);
            log.debug("Request details: {}", objectMapper.writeValueAsString(requestLog));
            
        } catch (Exception e) {
            log.error("Failed to log request", e);
        }
    }
    
    /**
     * Log outgoing response
     */
    public void logResponse(int statusCode, Map<String, String> headers, Object body, long durationMs) {
        try {
            Map<String, Object> responseLog = new HashMap<>();
            responseLog.put("logType", "RESPONSE");
            responseLog.put("timestamp", Instant.now());
            responseLog.put("statusCode", statusCode);
            responseLog.put("headers", sanitizeHeaders(headers));
            responseLog.put("body", sanitizeBody(body));
            responseLog.put("durationMs", durationMs);
            responseLog.put("correlationId", correlationIdService.getCurrentCorrelationId());
            
            // Add to MDC for structured logging
            MDC.put("responseStatus", String.valueOf(statusCode));
            MDC.put("responseDuration", String.valueOf(durationMs));
            
            log.info("Outgoing response: {} ({}ms)", statusCode, durationMs);
            log.debug("Response details: {}", objectMapper.writeValueAsString(responseLog));
            
        } catch (Exception e) {
            log.error("Failed to log response", e);
        } finally {
            // Clean up MDC
            MDC.remove("requestMethod");
            MDC.remove("requestUri");
            MDC.remove("responseStatus");
            MDC.remove("responseDuration");
        }
    }
    
    /**
     * Log API call to external service
     */
    public void logExternalApiCall(String serviceName, String endpoint, String method, 
                                  Object requestPayload, Object responsePayload, 
                                  int statusCode, long durationMs) {
        try {
            Map<String, Object> apiLog = new HashMap<>();
            apiLog.put("logType", "EXTERNAL_API_CALL");
            apiLog.put("timestamp", Instant.now());
            apiLog.put("serviceName", serviceName);
            apiLog.put("endpoint", endpoint);
            apiLog.put("method", method);
            apiLog.put("requestPayload", sanitizeBody(requestPayload));
            apiLog.put("responsePayload", sanitizeBody(responsePayload));
            apiLog.put("statusCode", statusCode);
            apiLog.put("durationMs", durationMs);
            apiLog.put("correlationId", correlationIdService.getCurrentCorrelationId());
            
            // Add to MDC
            MDC.put("externalService", serviceName);
            MDC.put("externalEndpoint", endpoint);
            MDC.put("externalStatusCode", String.valueOf(statusCode));
            
            log.info("External API call: {} {} -> {} ({}ms)", method, endpoint, statusCode, durationMs);
            log.debug("External API details: {}", objectMapper.writeValueAsString(apiLog));
            
        } catch (Exception e) {
            log.error("Failed to log external API call", e);
        } finally {
            // Clean up MDC
            MDC.remove("externalService");
            MDC.remove("externalEndpoint");
            MDC.remove("externalStatusCode");
        }
    }
    
    /**
     * Log processing step
     */
    public void logProcessingStep(String stepName, String processId, Object inputData, 
                                Object outputData, boolean success, long durationMs) {
        try {
            Map<String, Object> stepLog = new HashMap<>();
            stepLog.put("logType", "PROCESSING_STEP");
            stepLog.put("timestamp", Instant.now());
            stepLog.put("stepName", stepName);
            stepLog.put("processId", processId);
            stepLog.put("inputData", sanitizeBody(inputData));
            stepLog.put("outputData", sanitizeBody(outputData));
            stepLog.put("success", success);
            stepLog.put("durationMs", durationMs);
            stepLog.put("correlationId", correlationIdService.getCurrentCorrelationId());
            
            // Add to MDC
            MDC.put("processingStep", stepName);
            MDC.put("processId", processId);
            MDC.put("stepSuccess", String.valueOf(success));
            
            log.info("Processing step: {} -> {} ({}ms)", stepName, success ? "SUCCESS" : "FAILED", durationMs);
            log.debug("Step details: {}", objectMapper.writeValueAsString(stepLog));
            
        } catch (Exception e) {
            log.error("Failed to log processing step", e);
        } finally {
            // Clean up MDC
            MDC.remove("processingStep");
            MDC.remove("processId");
            MDC.remove("stepSuccess");
        }
    }
    
    /**
     * Sanitize headers (remove sensitive information)
     */
    private Map<String, String> sanitizeHeaders(Map<String, String> headers) {
        if (headers == null) return new HashMap<>();
        
        Map<String, String> sanitized = new HashMap<>(headers);
        
        // Remove sensitive headers
        sanitized.remove("authorization");
        sanitized.remove("x-api-key");
        sanitized.remove("cookie");
        sanitized.remove("x-auth-token");
        
        return sanitized;
    }
    
    /**
     * Sanitize request/response body (remove sensitive information)
     */
    private Object sanitizeBody(Object body) {
        if (body == null) return null;
        
        try {
            String bodyStr = objectMapper.writeValueAsString(body);
            
            // Remove sensitive fields
            bodyStr = bodyStr.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
            bodyStr = bodyStr.replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"");
            bodyStr = bodyStr.replaceAll("\"apiKey\"\\s*:\\s*\"[^\"]*\"", "\"apiKey\":\"***\"");
            bodyStr = bodyStr.replaceAll("\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"***\"");
            
            return objectMapper.readValue(bodyStr, Object.class);
            
        } catch (Exception e) {
            return "Error sanitizing body: " + e.getMessage();
        }
    }
}
