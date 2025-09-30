package com.banking.onboarding.interceptor;

import com.banking.onboarding.logging.RequestResponseLogger;
import com.banking.onboarding.service.CorrelationIdService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor for automatic request/response logging
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {
    
    private final RequestResponseLogger requestResponseLogger;
    private final CorrelationIdService correlationIdService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // Wrap request and response for content caching
            if (!(request instanceof ContentCachingRequestWrapper)) {
                request = new ContentCachingRequestWrapper(request);
            }
            if (!(response instanceof ContentCachingResponseWrapper)) {
                response = new ContentCachingResponseWrapper(response);
            }
            
            // Extract headers
            Map<String, String> headers = extractHeaders(request);
            
            // Log request
            requestResponseLogger.logRequest(
                request.getMethod(),
                request.getRequestURI(),
                headers,
                getRequestBody(request)
            );
            
        } catch (Exception e) {
            log.error("Error in request logging interceptor", e);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        try {
            long startTime = System.currentTimeMillis();
            long duration = System.currentTimeMillis() - startTime;
            
            // Extract headers
            Map<String, String> headers = extractResponseHeaders(response);
            
            // Log response
            requestResponseLogger.logResponse(
                response.getStatus(),
                headers,
                getResponseBody(response),
                duration
            );
            
            // Copy response content if wrapped
            if (response instanceof ContentCachingResponseWrapper) {
                ((ContentCachingResponseWrapper) response).copyBodyToResponse();
            }
            
        } catch (Exception e) {
            log.error("Error in response logging interceptor", e);
        }
    }
    
    /**
     * Extract headers from request
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        
        return headers;
    }
    
    /**
     * Extract headers from response
     */
    private Map<String, String> extractResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        
        for (String headerName : response.getHeaderNames()) {
            headers.put(headerName, response.getHeader(headerName));
        }
        
        return headers;
    }
    
    /**
     * Get request body
     */
    private Object getRequestBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content);
            }
        }
        return null;
    }
    
    /**
     * Get response body
     */
    private Object getResponseBody(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content);
            }
        }
        return null;
    }
}
