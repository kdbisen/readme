package com.banking.onboarding.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor to automatically handle correlation IDs via HTTP headers
 * Creates correlation ID if not present in request headers
 * Adds correlation ID to response headers for all responses
 */
@Slf4j
@Component
public class CorrelationIdInterceptor implements HandlerInterceptor {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_ATTRIBUTE = "correlationId";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = getOrCreateCorrelationId(request);
        
        // Store in request attributes for use in controllers
        request.setAttribute(CORRELATION_ID_ATTRIBUTE, correlationId);
        
        // Add to response headers
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        log.info("[CORRELATION:{}] Request: {} {}", correlationId, request.getMethod(), request.getRequestURI());
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String correlationId = (String) request.getAttribute(CORRELATION_ID_ATTRIBUTE);
        
        if (ex != null) {
            log.error("[CORRELATION:{}] Request failed: {} {}", correlationId, request.getMethod(), request.getRequestURI(), ex);
        } else {
            log.info("[CORRELATION:{}] Request completed: {} {} - Status: {}", 
                    correlationId, request.getMethod(), request.getRequestURI(), response.getStatus());
        }
    }
    
    /**
     * Get correlation ID from request header or create new one
     */
    private String getOrCreateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Created new correlation ID: {}", correlationId);
        } else {
            log.debug("Using existing correlation ID: {}", correlationId);
        }
        
        return correlationId;
    }
}
