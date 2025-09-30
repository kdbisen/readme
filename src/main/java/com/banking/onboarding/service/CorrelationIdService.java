package com.banking.onboarding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Service to get correlation ID from current request context
 */
@Slf4j
@Service
public class CorrelationIdService {
    
    private static final String CORRELATION_ID_ATTRIBUTE = "correlationId";
    
    /**
     * Get correlation ID from current request context
     */
    public String getCurrentCorrelationId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String correlationId = (String) request.getAttribute(CORRELATION_ID_ATTRIBUTE);
                
                if (correlationId != null) {
                    return correlationId;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get correlation ID from request context: {}", e.getMessage());
        }
        
        // Fallback: generate new correlation ID if not available
        String fallbackCorrelationId = java.util.UUID.randomUUID().toString();
        log.warn("Using fallback correlation ID: {}", fallbackCorrelationId);
        return fallbackCorrelationId;
    }
    
    /**
     * Check if correlation ID exists in current request
     */
    public boolean hasCorrelationId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getAttribute(CORRELATION_ID_ATTRIBUTE) != null;
            }
        } catch (Exception e) {
            log.warn("Failed to check correlation ID: {}", e.getMessage());
        }
        return false;
    }
}
