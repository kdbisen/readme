package com.banking.onboarding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Correlation ID Strategy Service
 * Handles multiple requests with the same correlation ID
 * If data exists for a correlation ID, treats it as a new request with the same correlation ID
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CorrelationIdStrategyService {
    
    // Simple storage for correlation IDs and their request counts
    private final Map<String, Integer> correlationRequestCounts = new ConcurrentHashMap<>();
    
    /**
     * Process correlation ID - Simple version
     * @param providedCorrelationId The correlation ID provided in the request (can be null)
     * @param requestData The request data (not used in simple version)
     * @return Simple correlation ID result
     */
    public SimpleCorrelationResult processCorrelationId(String providedCorrelationId, Object requestData) {
        // Use provided correlation ID or generate new one
        String correlationId = providedCorrelationId != null ? providedCorrelationId : generateNewCorrelationId();
        
        // Increment request count for this correlation ID
        int requestCount = correlationRequestCounts.merge(correlationId, 1, Integer::sum);
        
        log.info("[CORRELATION:{}] Request #{} for this correlation ID", correlationId, requestCount);
        
        return SimpleCorrelationResult.builder()
                .correlationId(correlationId)
                .requestNumber(requestCount)
                .isNewCorrelationId(requestCount == 1)
                .build();
    }
    
    /**
     * Generate a new simple correlation ID
     */
    public String generateNewCorrelationId() {
        return "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Get request count for a correlation ID
     */
    public int getRequestCount(String correlationId) {
        return correlationRequestCounts.getOrDefault(correlationId, 0);
    }
    
    /**
     * Get all active correlation IDs
     */
    public Map<String, Integer> getAllCorrelationIds() {
        return Map.copyOf(correlationRequestCounts);
    }
    
    /**
     * Simple Correlation Result
     */
    @lombok.Data
    @lombok.Builder
    public static class SimpleCorrelationResult {
        private String correlationId;
        private int requestNumber;
        private boolean isNewCorrelationId;
        
        public String getRequestIdentifier() {
            return String.format("%s-R%d", correlationId, requestNumber);
        }
    }
}