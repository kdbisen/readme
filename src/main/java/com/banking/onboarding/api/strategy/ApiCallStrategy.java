package com.banking.onboarding.api.strategy;

import com.banking.onboarding.domain.ApiRequest;
import com.banking.onboarding.domain.ApiResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Strategy interface for different API call implementations
 */
public interface ApiCallStrategy {
    
    /**
     * Execute API call based on the strategy implementation
     */
    CompletableFuture<ApiResponse> execute(ApiRequest request);
    
    /**
     * Check if this strategy supports the given API type
     */
    boolean supports(ApiRequest request);
    
    /**
     * Get the strategy name
     */
    String getStrategyName();
}
