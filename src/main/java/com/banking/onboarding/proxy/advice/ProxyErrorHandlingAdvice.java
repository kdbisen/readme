package com.banking.onboarding.proxy.advice;

import com.banking.onboarding.logging.ErrorLoggingService;
import com.banking.onboarding.proxy.ProxyException;
import com.banking.onboarding.proxy.ProxyResponse;
import com.banking.onboarding.service.CorrelationIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * AOP advice for proxy error handling and monitoring
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ProxyErrorHandlingAdvice {
    
    private final ErrorLoggingService errorLoggingService;
    private final CorrelationIdService correlationIdService;
    
    /**
     * Around advice for proxy service calls
     */
    @Around("execution(* com.banking.onboarding.proxy.FenergoProxyService.callFenergoApi(..))")
    public Object handleProxyCall(ProceedingJoinPoint joinPoint) throws Throwable {
        
        Instant startTime = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String correlationId = correlationIdService.getCurrentCorrelationId();
        
        // Extract arguments for logging
        String proxyUrl = args.length > 0 ? (String) args[0] : "unknown";
        String fenergoEndpoint = args.length > 1 ? (String) args[1] : "unknown";
        
        log.info("[CORRELATION:{}] Starting proxy call - Method: {}, Proxy: {}, Fenergo: {}", 
                correlationId, methodName, proxyUrl, fenergoEndpoint);
        
        try {
            // Execute the method
            Object result = joinPoint.proceed();
            
            // Calculate execution time
            Duration executionTime = Duration.between(startTime, Instant.now());
            
            if (result instanceof ProxyResponse) {
                ProxyResponse response = (ProxyResponse) result;
                log.info("[CORRELATION:{}] Proxy call completed - Status: {}, Time: {}ms, Endpoint: {}", 
                        correlationId, response.getStatusCode(), executionTime.toMillis(), fenergoEndpoint);
                
                // Log warning for slow responses
                if (executionTime.toMillis() > 10000) {
                    log.warn("[CORRELATION:{}] Slow proxy response detected - Time: {}ms, Endpoint: {}", 
                            correlationId, executionTime.toMillis(), fenergoEndpoint);
                }
            }
            
            return result;
            
        } catch (ProxyException e) {
            Duration executionTime = Duration.between(startTime, Instant.now());
            log.error("[CORRELATION:{}] Proxy exception - Time: {}ms, Proxy: {}, Fenergo: {}, Error: {}", 
                    correlationId, executionTime.toMillis(), proxyUrl, fenergoEndpoint, e.getMessage());
            
            // Log error to MongoDB
            Map<String, Object> context = new HashMap<>();
            context.put("methodName", methodName);
            context.put("durationMs", executionTime.toMillis());
            context.put("proxyUrl", proxyUrl);
            context.put("fenergoEndpoint", fenergoEndpoint);
            context.put("errorSource", "PROXY_AOP");
            errorLoggingService.logProxyError(proxyUrl, fenergoEndpoint, e.getMessage(), e);
            
            return ProxyResponse.error(500, "Proxy Error", fenergoEndpoint, e.getMessage());
            
        } catch (Exception e) {
            Duration executionTime = Duration.between(startTime, Instant.now());
            log.error("[CORRELATION:{}] Unexpected error in proxy call - Time: {}ms, Proxy: {}, Fenergo: {}", 
                    correlationId, executionTime.toMillis(), proxyUrl, fenergoEndpoint, e);
            
            // Log error to MongoDB
            Map<String, Object> context = new HashMap<>();
            context.put("methodName", methodName);
            context.put("durationMs", executionTime.toMillis());
            context.put("proxyUrl", proxyUrl);
            context.put("fenergoEndpoint", fenergoEndpoint);
            context.put("errorSource", "PROXY_AOP");
            errorLoggingService.logError("PROXY_UNEXPECTED_ERROR", e.getMessage(), e, context);
            
            return ProxyResponse.error(500, "Internal Server Error", fenergoEndpoint, 
                    "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Around advice for proxy availability checks
     */
    @Around("execution(* com.banking.onboarding.proxy.FenergoProxyService.isProxyAvailable(..))")
    public Object handleProxyAvailabilityCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        
        Object[] args = joinPoint.getArgs();
        String proxyUrl = args.length > 0 ? (String) args[0] : "unknown";
        String correlationId = correlationIdService.getCurrentCorrelationId();
        
        log.debug("[CORRELATION:{}] Checking proxy availability: {}", correlationId, proxyUrl);
        
        try {
            Object result = joinPoint.proceed();
            log.debug("[CORRELATION:{}] Proxy availability check result: {} for {}", 
                    correlationId, result, proxyUrl);
            return result;
            
        } catch (Exception e) {
            log.warn("[CORRELATION:{}] Proxy availability check failed: {} - Error: {}", 
                    correlationId, proxyUrl, e.getMessage());
            
            // Log error to MongoDB
            Map<String, Object> context = new HashMap<>();
            context.put("proxyUrl", proxyUrl);
            context.put("checkType", "AVAILABILITY");
            errorLoggingService.logError("PROXY_AVAILABILITY_CHECK_ERROR", e.getMessage(), e, context);
            
            return false;
        }
    }
}
