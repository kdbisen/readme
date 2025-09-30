package com.banking.onboarding.proxy.advice;

import com.banking.onboarding.proxy.ProxyException;
import com.banking.onboarding.proxy.ProxyResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * AOP advice for proxy error handling and monitoring
 */
@Slf4j
@Aspect
@Component
public class ProxyErrorHandlingAdvice {
    
    /**
     * Around advice for proxy service calls
     */
    @Around("execution(* com.banking.onboarding.proxy.FenergoProxyService.callFenergoApi(..))")
    public Object handleProxyCall(ProceedingJoinPoint joinPoint) throws Throwable {
        
        Instant startTime = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // Extract arguments for logging
        String proxyUrl = args.length > 0 ? (String) args[0] : "unknown";
        String fenergoEndpoint = args.length > 1 ? (String) args[1] : "unknown";
        
        log.info("Starting proxy call - Method: {}, Proxy: {}, Fenergo: {}", 
                methodName, proxyUrl, fenergoEndpoint);
        
        try {
            // Execute the method
            Object result = joinPoint.proceed();
            
            // Calculate execution time
            Duration executionTime = Duration.between(startTime, Instant.now());
            
            if (result instanceof ProxyResponse) {
                ProxyResponse response = (ProxyResponse) result;
                log.info("Proxy call completed - Status: {}, Time: {}ms, Endpoint: {}", 
                        response.getStatusCode(), executionTime.toMillis(), fenergoEndpoint);
                
                // Log warning for slow responses
                if (executionTime.toMillis() > 10000) {
                    log.warn("Slow proxy response detected - Time: {}ms, Endpoint: {}", 
                            executionTime.toMillis(), fenergoEndpoint);
                }
            }
            
            return result;
            
        } catch (ProxyException e) {
            Duration executionTime = Duration.between(startTime, Instant.now());
            log.error("Proxy exception - Time: {}ms, Proxy: {}, Fenergo: {}, Error: {}", 
                    executionTime.toMillis(), proxyUrl, fenergoEndpoint, e.getMessage());
            
            return ProxyResponse.error(500, "Proxy Error", fenergoEndpoint, e.getMessage());
            
        } catch (Exception e) {
            Duration executionTime = Duration.between(startTime, Instant.now());
            log.error("Unexpected error in proxy call - Time: {}ms, Proxy: {}, Fenergo: {}", 
                    executionTime.toMillis(), proxyUrl, fenergoEndpoint, e);
            
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
        
        log.debug("Checking proxy availability: {}", proxyUrl);
        
        try {
            Object result = joinPoint.proceed();
            log.debug("Proxy availability check result: {} for {}", result, proxyUrl);
            return result;
            
        } catch (Exception e) {
            log.warn("Proxy availability check failed: {} - Error: {}", proxyUrl, e.getMessage());
            return false;
        }
    }
}
