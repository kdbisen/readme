package com.banking.onboarding.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Retry Mechanism with Exponential Backoff
 * Handles transient failures gracefully
 */
@Slf4j
@Component
public class RetryService {
    
    // Default retry configuration
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final Duration DEFAULT_INITIAL_DELAY = Duration.ofMillis(1000);
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(30);
    private static final double DEFAULT_JITTER_FACTOR = 0.1;
    
    /**
     * Execute operation with retry logic
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        return executeWithRetry(operation, operationName, DEFAULT_MAX_ATTEMPTS, DEFAULT_INITIAL_DELAY);
    }
    
    /**
     * Execute operation with custom retry configuration
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName, 
                                 int maxAttempts, Duration initialDelay) {
        return executeWithRetry(operation, operationName, maxAttempts, initialDelay, 
                              DEFAULT_BACKOFF_MULTIPLIER, DEFAULT_MAX_DELAY, DEFAULT_JITTER_FACTOR);
    }
    
    /**
     * Execute operation with full retry configuration
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName, 
                                 int maxAttempts, Duration initialDelay, 
                                 double backoffMultiplier, Duration maxDelay, double jitterFactor) {
        
        Exception lastException = null;
        Duration currentDelay = initialDelay;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("Executing {} - Attempt {}/{}", operationName, attempt, maxAttempts);
                T result = operation.get();
                
                if (attempt > 1) {
                    log.info("{} succeeded on attempt {}", operationName, attempt);
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                
                // Check if this is a retryable exception
                if (!isRetryableException(e)) {
                    log.warn("{} failed with non-retryable exception: {}", operationName, e.getMessage());
                    throw new RetryException("Non-retryable exception occurred", e);
                }
                
                if (attempt == maxAttempts) {
                    log.error("{} failed after {} attempts", operationName, maxAttempts, e);
                    throw new RetryException("Operation failed after " + maxAttempts + " attempts", e);
                }
                
                // Calculate delay with jitter
                Duration delayWithJitter = calculateDelayWithJitter(currentDelay, jitterFactor, maxDelay);
                
                log.warn("{} failed on attempt {}: {}. Retrying in {}ms", 
                        operationName, attempt, e.getMessage(), delayWithJitter.toMillis());
                
                try {
                    Thread.sleep(delayWithJitter.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryException("Retry interrupted", ie);
                }
                
                // Calculate next delay
                currentDelay = Duration.ofMillis(
                    Math.min((long) (currentDelay.toMillis() * backoffMultiplier), maxDelay.toMillis())
                );
            }
        }
        
        throw new RetryException("Operation failed after " + maxAttempts + " attempts", lastException);
    }
    
    /**
     * Execute operation with retry for specific exception types
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName, 
                                 Class<? extends Exception>... retryableExceptions) {
        return executeWithRetry(operation, operationName, DEFAULT_MAX_ATTEMPTS, 
                              DEFAULT_INITIAL_DELAY, retryableExceptions);
    }
    
    /**
     * Execute operation with retry for specific exception types and custom config
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName, 
                                 int maxAttempts, Duration initialDelay, 
                                 Class<? extends Exception>... retryableExceptions) {
        
        Exception lastException = null;
        Duration currentDelay = initialDelay;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("Executing {} - Attempt {}/{}", operationName, attempt, maxAttempts);
                T result = operation.get();
                
                if (attempt > 1) {
                    log.info("{} succeeded on attempt {}", operationName, attempt);
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                
                // Check if this is a retryable exception type
                boolean isRetryable = false;
                for (Class<? extends Exception> retryableException : retryableExceptions) {
                    if (retryableException.isInstance(e)) {
                        isRetryable = true;
                        break;
                    }
                }
                
                if (!isRetryable) {
                    log.warn("{} failed with non-retryable exception: {}", operationName, e.getMessage());
                    throw new RetryException("Non-retryable exception occurred", e);
                }
                
                if (attempt == maxAttempts) {
                    log.error("{} failed after {} attempts", operationName, maxAttempts, e);
                    throw new RetryException("Operation failed after " + maxAttempts + " attempts", e);
                }
                
                // Calculate delay with jitter
                Duration delayWithJitter = calculateDelayWithJitter(currentDelay, DEFAULT_JITTER_FACTOR, DEFAULT_MAX_DELAY);
                
                log.warn("{} failed on attempt {}: {}. Retrying in {}ms", 
                        operationName, attempt, e.getMessage(), delayWithJitter.toMillis());
                
                try {
                    Thread.sleep(delayWithJitter.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryException("Retry interrupted", ie);
                }
                
                // Calculate next delay
                currentDelay = Duration.ofMillis(
                    Math.min((long) (currentDelay.toMillis() * DEFAULT_BACKOFF_MULTIPLIER), DEFAULT_MAX_DELAY.toMillis())
                );
            }
        }
        
        throw new RetryException("Operation failed after " + maxAttempts + " attempts", lastException);
    }
    
    /**
     * Check if exception is retryable
     */
    private boolean isRetryableException(Exception e) {
        // Network-related exceptions
        if (e instanceof java.net.ConnectException ||
            e instanceof java.net.SocketTimeoutException ||
            e instanceof java.net.UnknownHostException) {
            return true;
        }
        
        // HTTP-related exceptions
        if (e.getMessage() != null) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("timeout") ||
                message.contains("connection") ||
                message.contains("network") ||
                message.contains("temporary") ||
                message.contains("unavailable")) {
                return true;
            }
        }
        
        // HTTP status codes that are retryable
        if (e instanceof org.springframework.web.client.HttpServerErrorException) {
            org.springframework.web.client.HttpServerErrorException httpEx = 
                (org.springframework.web.client.HttpServerErrorException) e;
            int statusCode = httpEx.getStatusCode().value();
            return statusCode >= 500 && statusCode < 600; // 5xx errors
        }
        
        return false;
    }
    
    /**
     * Calculate delay with jitter to avoid thundering herd
     */
    private Duration calculateDelayWithJitter(Duration baseDelay, double jitterFactor, Duration maxDelay) {
        long baseDelayMs = baseDelay.toMillis();
        long maxDelayMs = maxDelay.toMillis();
        
        // Apply jitter (±jitterFactor% of base delay)
        long jitterMs = (long) (baseDelayMs * jitterFactor);
        long jitteredDelayMs = baseDelayMs + ThreadLocalRandom.current().nextLong(-jitterMs, jitterMs + 1);
        
        // Ensure delay is within bounds
        jitteredDelayMs = Math.max(0, Math.min(jitteredDelayMs, maxDelayMs));
        
        return Duration.ofMillis(jitteredDelayMs);
    }
    
    /**
     * Retry exception
     */
    public static class RetryException extends RuntimeException {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
