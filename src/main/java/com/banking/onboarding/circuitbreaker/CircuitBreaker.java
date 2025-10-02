package com.banking.onboarding.circuitbreaker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Circuit Breaker Pattern Implementation
 * Prevents cascading failures from external services
 */
@Slf4j
@Component
public class CircuitBreaker {
    
    private final ConcurrentHashMap<String, CircuitState> circuitStates = new ConcurrentHashMap<>();
    
    // Circuit breaker configuration
    private static final int FAILURE_THRESHOLD = 5;
    private static final long TIMEOUT_DURATION_MS = 60000; // 1 minute
    private static final long HALF_OPEN_TIMEOUT_MS = 30000; // 30 seconds
    
    public enum State {
        CLOSED,    // Normal operation
        OPEN,      // Circuit is open, failing fast
        HALF_OPEN  // Testing if service is back
    }
    
    /**
     * Execute operation with circuit breaker protection
     */
    public <T> T execute(String serviceName, CircuitBreakerOperation<T> operation) throws Exception {
        CircuitState state = getOrCreateCircuitState(serviceName);
        
        if (state.getState() == State.OPEN) {
            if (shouldAttemptReset(state)) {
                state.setState(State.HALF_OPEN);
                log.info("Circuit breaker for {} moved to HALF_OPEN state", serviceName);
            } else {
                throw new CircuitBreakerOpenException("Circuit breaker is OPEN for service: " + serviceName);
            }
        }
        
        try {
            T result = operation.execute();
            onSuccess(state);
            return result;
        } catch (Exception e) {
            onFailure(state, serviceName);
            throw e;
        }
    }
    
    /**
     * Check if circuit breaker is open for a service
     */
    public boolean isCircuitOpen(String serviceName) {
        CircuitState state = circuitStates.get(serviceName);
        return state != null && state.getState() == State.OPEN;
    }
    
    /**
     * Get circuit breaker state for a service
     */
    public State getCircuitState(String serviceName) {
        CircuitState state = circuitStates.get(serviceName);
        return state != null ? state.getState() : State.CLOSED;
    }
    
    /**
     * Reset circuit breaker for a service
     */
    public void resetCircuit(String serviceName) {
        CircuitState state = circuitStates.get(serviceName);
        if (state != null) {
            state.setState(State.CLOSED);
            state.resetCounters();
            log.info("Circuit breaker for {} has been manually reset", serviceName);
        }
    }
    
    /**
     * Get circuit breaker metrics
     */
    public CircuitBreakerMetrics getMetrics(String serviceName) {
        CircuitState state = circuitStates.get(serviceName);
        if (state == null) {
            return new CircuitBreakerMetrics(serviceName, State.CLOSED, 0, 0, 0, LocalDateTime.now());
        }
        
        return new CircuitBreakerMetrics(
            serviceName,
            state.getState(),
            state.getSuccessCount(),
            state.getFailureCount(),
            state.getTotalRequests(),
            state.getLastFailureTime()
        );
    }
    
    private CircuitState getOrCreateCircuitState(String serviceName) {
        return circuitStates.computeIfAbsent(serviceName, k -> new CircuitState());
    }
    
    private void onSuccess(CircuitState state) {
        state.incrementSuccess();
        
        if (state.getState() == State.HALF_OPEN) {
            state.setState(State.CLOSED);
            log.info("Circuit breaker moved to CLOSED state after successful call");
        }
    }
    
    private void onFailure(CircuitState state, String serviceName) {
        state.incrementFailure();
        
        if (state.getFailureCount() >= FAILURE_THRESHOLD) {
            state.setState(State.OPEN);
            state.setLastFailureTime(LocalDateTime.now());
            log.warn("Circuit breaker for {} moved to OPEN state after {} failures", 
                    serviceName, state.getFailureCount());
        }
    }
    
    private boolean shouldAttemptReset(CircuitState state) {
        return state.getLastFailureTime() != null &&
               System.currentTimeMillis() - state.getLastFailureTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() > TIMEOUT_DURATION_MS;
    }
    
    /**
     * Circuit breaker operation interface
     */
    @FunctionalInterface
    public interface CircuitBreakerOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * Circuit breaker state
     */
    private static class CircuitState {
        private volatile State state = State.CLOSED;
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong totalRequests = new AtomicLong(0);
        private volatile LocalDateTime lastFailureTime;
        
        public State getState() { return state; }
        public void setState(State state) { this.state = state; }
        
        public int getSuccessCount() { return successCount.get(); }
        public int getFailureCount() { return failureCount.get(); }
        public long getTotalRequests() { return totalRequests.get(); }
        public LocalDateTime getLastFailureTime() { return lastFailureTime; }
        public void setLastFailureTime(LocalDateTime lastFailureTime) { this.lastFailureTime = lastFailureTime; }
        
        public void incrementSuccess() {
            successCount.incrementAndGet();
            totalRequests.incrementAndGet();
        }
        
        public void incrementFailure() {
            failureCount.incrementAndGet();
            totalRequests.incrementAndGet();
        }
        
        public void resetCounters() {
            successCount.set(0);
            failureCount.set(0);
            totalRequests.set(0);
            lastFailureTime = null;
        }
    }
    
    /**
     * Circuit breaker metrics
     */
    public static class CircuitBreakerMetrics {
        private final String serviceName;
        private final State state;
        private final int successCount;
        private final int failureCount;
        private final long totalRequests;
        private final LocalDateTime lastFailureTime;
        
        public CircuitBreakerMetrics(String serviceName, State state, int successCount, 
                                   int failureCount, long totalRequests, LocalDateTime lastFailureTime) {
            this.serviceName = serviceName;
            this.state = state;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.totalRequests = totalRequests;
            this.lastFailureTime = lastFailureTime;
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public State getState() { return state; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public long getTotalRequests() { return totalRequests; }
        public LocalDateTime getLastFailureTime() { return lastFailureTime; }
        
        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successCount / totalRequests * 100 : 0.0;
        }
        
        public double getFailureRate() {
            return totalRequests > 0 ? (double) failureCount / totalRequests * 100 : 0.0;
        }
    }
    
    /**
     * Circuit breaker open exception
     */
    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
