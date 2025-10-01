package com.banking.onboarding.api.factory;

import com.banking.onboarding.api.strategy.ApiCallStrategy;
import com.banking.onboarding.domain.ApiRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for API call strategies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCallStrategyFactory {
    
    private final List<ApiCallStrategy> strategies;
    
    /**
     * Get the appropriate strategy for the given API request
     */
    public ApiCallStrategy getStrategy(ApiRequest request) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(request))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("[CORRELATION:{}] No strategy found for API type: {} and auth type: {}", 
                            request.getCorrelationId(), 
                            request.getApiType(), 
                            request.getAuthConfig().getAuthType());
                    return new IllegalArgumentException(
                            "No strategy found for API type: " + request.getApiType() + 
                            " and auth type: " + request.getAuthConfig().getAuthType());
                });
    }
    
    /**
     * Get all available strategies
     */
    public List<ApiCallStrategy> getAllStrategies() {
        return strategies;
    }
}
