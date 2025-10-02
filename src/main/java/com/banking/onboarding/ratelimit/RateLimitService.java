package com.banking.onboarding.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate Limiting Service
 * Implements token bucket algorithm for API rate limiting
 */
@Slf4j
@Component
public class RateLimitService {
    
    private final ConcurrentHashMap<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    
    // Default rate limit configuration
    private static final int DEFAULT_CAPACITY = 100;
    private static final int DEFAULT_REFILL_RATE = 10; // tokens per second
    private static final long DEFAULT_WINDOW_SIZE_MS = 60000; // 1 minute
    
    /**
     * Check if request is allowed for the given key
     */
    public boolean isAllowed(String key) {
        return isAllowed(key, DEFAULT_CAPACITY, DEFAULT_REFILL_RATE);
    }
    
    /**
     * Check if request is allowed with custom limits
     */
    public boolean isAllowed(String key, int capacity, int refillRate) {
        RateLimitBucket bucket = getOrCreateBucket(key, capacity, refillRate);
        return bucket.tryConsume();
    }
    
    /**
     * Get rate limit info for a key
     */
    public RateLimitInfo getRateLimitInfo(String key) {
        RateLimitBucket bucket = buckets.get(key);
        if (bucket == null) {
            return new RateLimitInfo(key, DEFAULT_CAPACITY, DEFAULT_CAPACITY, 
                                   DEFAULT_REFILL_RATE, LocalDateTime.now());
        }
        
        return new RateLimitInfo(
            key,
            bucket.getCapacity(),
            bucket.getAvailableTokens(),
            bucket.getRefillRate(),
            bucket.getLastRefillTime()
        );
    }
    
    /**
     * Reset rate limit for a key
     */
    public void resetRateLimit(String key) {
        RateLimitBucket bucket = buckets.get(key);
        if (bucket != null) {
            bucket.reset();
            log.info("Rate limit reset for key: {}", key);
        }
    }
    
    /**
     * Get all rate limit buckets (for monitoring)
     */
    public ConcurrentHashMap<String, RateLimitInfo> getAllRateLimits() {
        ConcurrentHashMap<String, RateLimitInfo> info = new ConcurrentHashMap<>();
        buckets.forEach((key, bucket) -> {
            info.put(key, new RateLimitInfo(
                key,
                bucket.getCapacity(),
                bucket.getAvailableTokens(),
                bucket.getRefillRate(),
                bucket.getLastRefillTime()
            ));
        });
        return info;
    }
    
    private RateLimitBucket getOrCreateBucket(String key, int capacity, int refillRate) {
        return buckets.computeIfAbsent(key, k -> new RateLimitBucket(capacity, refillRate));
    }
    
    /**
     * Rate limit bucket implementation using token bucket algorithm
     */
    private static class RateLimitBucket {
        private final int capacity;
        private final int refillRate;
        private final AtomicInteger tokens;
        private final AtomicLong lastRefillTime;
        
        public RateLimitBucket(int capacity, int refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
        }
        
        public boolean tryConsume() {
            refillTokens();
            
            int currentTokens = tokens.get();
            if (currentTokens > 0) {
                if (tokens.compareAndSet(currentTokens, currentTokens - 1)) {
                    return true;
                }
            }
            
            return false;
        }
        
        private void refillTokens() {
            long now = System.currentTimeMillis();
            long lastRefill = lastRefillTime.get();
            long timePassed = now - lastRefill;
            
            if (timePassed >= 1000) { // Refill every second
                int tokensToAdd = (int) (timePassed / 1000 * refillRate);
                if (tokensToAdd > 0) {
                    int currentTokens = tokens.get();
                    int newTokens = Math.min(capacity, currentTokens + tokensToAdd);
                    
                    if (tokens.compareAndSet(currentTokens, newTokens)) {
                        lastRefillTime.set(now);
                    }
                }
            }
        }
        
        public int getCapacity() { return capacity; }
        public int getAvailableTokens() { return tokens.get(); }
        public int getRefillRate() { return refillRate; }
        public LocalDateTime getLastRefillTime() { 
            return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(lastRefillTime.get()),
                java.time.ZoneId.systemDefault()
            );
        }
        
        public void reset() {
            tokens.set(capacity);
            lastRefillTime.set(System.currentTimeMillis());
        }
    }
    
    /**
     * Rate limit info
     */
    public static class RateLimitInfo {
        private final String key;
        private final int capacity;
        private final int availableTokens;
        private final int refillRate;
        private final LocalDateTime lastRefillTime;
        
        public RateLimitInfo(String key, int capacity, int availableTokens, 
                           int refillRate, LocalDateTime lastRefillTime) {
            this.key = key;
            this.capacity = capacity;
            this.availableTokens = availableTokens;
            this.refillRate = refillRate;
            this.lastRefillTime = lastRefillTime;
        }
        
        public String getKey() { return key; }
        public int getCapacity() { return capacity; }
        public int getAvailableTokens() { return availableTokens; }
        public int getRefillRate() { return refillRate; }
        public LocalDateTime getLastRefillTime() { return lastRefillTime; }
        
        public double getUtilizationPercent() {
            return capacity > 0 ? (double) (capacity - availableTokens) / capacity * 100 : 0.0;
        }
    }
    
    /**
     * Rate limit exceeded exception
     */
    public static class RateLimitExceededException extends RuntimeException {
        private final String key;
        private final RateLimitInfo rateLimitInfo;
        
        public RateLimitExceededException(String key, RateLimitInfo rateLimitInfo) {
            super("Rate limit exceeded for key: " + key);
            this.key = key;
            this.rateLimitInfo = rateLimitInfo;
        }
        
        public String getKey() { return key; }
        public RateLimitInfo getRateLimitInfo() { return rateLimitInfo; }
    }
}
