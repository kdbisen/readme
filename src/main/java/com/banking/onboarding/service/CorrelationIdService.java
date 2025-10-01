package com.banking.onboarding.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Enhanced service for correlation ID management with MDC support
 */
@Slf4j
@Service
public class CorrelationIdService {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String PROCESS_ID_HEADER = "X-Process-ID";
    private static final String CORRELATION_ID_ATTRIBUTE = "correlationId";
    
    /**
     * Get current correlation ID from request context
     */
    public String getCurrentCorrelationId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // First try header
                String correlationId = request.getHeader(CORRELATION_ID_HEADER);
                if (correlationId != null && !correlationId.isEmpty()) {
                    return correlationId;
                }
                
                // Then try attribute
                correlationId = (String) request.getAttribute(CORRELATION_ID_ATTRIBUTE);
                if (correlationId != null && !correlationId.isEmpty()) {
                    return correlationId;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get correlation ID from request context", e);
        }
        
        return "NO-CORRELATION-ID";
    }
    
    /**
     * Set correlation ID in MDC for logging
     */
    public void setCorrelationIdInMDC(String correlationId) {
        if (correlationId != null && !correlationId.isEmpty()) {
            MDC.put("correlationId", correlationId);
        }
    }
    
    /**
     * Set trace ID in MDC for distributed tracing
     */
    public void setTraceIdInMDC(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put("traceId", traceId);
        }
    }
    
    /**
     * Set process ID in MDC for process tracking
     */
    public void setProcessIdInMDC(String processId) {
        if (processId != null && !processId.isEmpty()) {
            MDC.put("processId", processId);
        }
    }
    
    /**
     * Generate new correlation ID
     */
    public String generateCorrelationId() {
        return "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Generate new trace ID
     */
    public String generateTraceId() {
        return "TRACE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Generate new process ID
     */
    public String generateProcessId() {
        return "PROC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Clear all MDC values
     */
    public void clearMDC() {
        MDC.clear();
    }
    
    /**
     * Get trace ID from request context
     */
    public String getCurrentTraceId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String traceId = request.getHeader(TRACE_ID_HEADER);
                
                if (traceId != null && !traceId.isEmpty()) {
                    return traceId;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get trace ID from request context", e);
        }
        
        return null;
    }
    
    /**
     * Get process ID from request context
     */
    public String getCurrentProcessId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String processId = request.getHeader(PROCESS_ID_HEADER);
                
                if (processId != null && !processId.isEmpty()) {
                    return processId;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get process ID from request context", e);
        }
        
        return null;
    }
    
    /**
     * Get or generate correlation ID
     */
    public String getOrGenerateCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.trim().isEmpty()) {
            return correlationId;
        }
        
        String currentCorrelationId = getCurrentCorrelationId();
        if (!"NO-CORRELATION-ID".equals(currentCorrelationId)) {
            return currentCorrelationId;
        }
        
        return generateCorrelationId();
    }
    
    /**
     * Check if correlation ID exists in current request
     */
    public boolean hasCorrelationId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getAttribute(CORRELATION_ID_ATTRIBUTE) != null ||
                       request.getHeader(CORRELATION_ID_HEADER) != null;
            }
        } catch (Exception e) {
            log.warn("Failed to check correlation ID: {}", e.getMessage());
        }
        return false;
    }
}
