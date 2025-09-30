package com.banking.onboarding.interceptor;

import com.banking.onboarding.service.CorrelationIdService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Enhanced interceptor for correlation ID management with MDC support
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CorrelationIdInterceptor implements HandlerInterceptor {
    
    private final CorrelationIdService correlationIdService;
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String PROCESS_ID_HEADER = "X-Process-ID";
    private static final String CORRELATION_ID_ATTRIBUTE = "correlationId";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // Get or generate correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = correlationIdService.generateCorrelationId();
                log.info("Generated new correlation ID: {}", correlationId);
            }
            
            // Get or generate trace ID
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (traceId == null || traceId.isEmpty()) {
                traceId = correlationIdService.generateTraceId();
            }
            
            // Get or generate process ID
            String processId = request.getHeader(PROCESS_ID_HEADER);
            if (processId == null || processId.isEmpty()) {
                processId = correlationIdService.generateProcessId();
            }
            
            // Set in request attributes for later use
            request.setAttribute(CORRELATION_ID_ATTRIBUTE, correlationId);
            request.setAttribute("traceId", traceId);
            request.setAttribute("processId", processId);
            
            // Set in MDC for logging
            correlationIdService.setCorrelationIdInMDC(correlationId);
            correlationIdService.setTraceIdInMDC(traceId);
            correlationIdService.setProcessIdInMDC(processId);
            
            // Add to response headers
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            response.setHeader(TRACE_ID_HEADER, traceId);
            response.setHeader(PROCESS_ID_HEADER, processId);
            
            log.info("[CORRELATION:{}] Request: {} {} - Trace: {}, Process: {}", 
                    correlationId, request.getMethod(), request.getRequestURI(), traceId, processId);
            
        } catch (Exception e) {
            log.error("Error in correlation ID interceptor", e);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        try {
            String correlationId = (String) request.getAttribute(CORRELATION_ID_ATTRIBUTE);
            
            if (ex != null) {
                log.error("[CORRELATION:{}] Request failed: {} {} - Status: {}", 
                        correlationId, request.getMethod(), request.getRequestURI(), response.getStatus(), ex);
            } else {
                log.info("[CORRELATION:{}] Request completed: {} {} - Status: {}", 
                        correlationId, request.getMethod(), request.getRequestURI(), response.getStatus());
            }
            
            // Clean up MDC
            correlationIdService.clearMDC();
            
        } catch (Exception e) {
            log.error("Error cleaning up correlation ID interceptor", e);
        }
    }
}
