package com.banking.onboarding.service;

import com.banking.onboarding.api.factory.ApiCallStrategyFactory;
import com.banking.onboarding.api.strategy.ApiCallStrategy;
import com.banking.onboarding.domain.ApiRequest;
import com.banking.onboarding.domain.ApiResponse;
import com.banking.onboarding.domain.ApiType;
import com.banking.onboarding.domain.AuthConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Clean API service layer with proper abstractions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiService {
    
    private final ApiCallStrategyFactory strategyFactory;
    
    /**
     * Execute API call using the appropriate strategy
     */
    public CompletableFuture<ApiResponse> executeApiCall(ApiRequest request) {
        log.info("[CORRELATION:{}] Executing API call using strategy pattern", request.getCorrelationId());
        
        ApiCallStrategy strategy = strategyFactory.getStrategy(request);
        log.info("[CORRELATION:{}] Using strategy: {}", request.getCorrelationId(), strategy.getStrategyName());
        
        return strategy.execute(request);
    }
    
    /**
     * Direct API call without authentication
     */
    public CompletableFuture<ApiResponse> callDirectApi(String endpoint, ApiRequest.HttpMethod method, 
                                                        Object payload, Map<String, String> headers, 
                                                        String correlationId) {
        ApiRequest request = ApiRequest.builder()
                .endpoint(endpoint)
                .method(method)
                .payload(payload)
                .headers(headers)
                .apiType(ApiType.DIRECT)
                .authConfig(AuthConfig.none())
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .build();
        
        return executeApiCall(request);
    }
    
    /**
     * Internal API call via Apigee with token authentication
     */
    public CompletableFuture<ApiResponse> callInternalApi(String endpoint, ApiRequest.HttpMethod method, 
                                                          Object payload, String authScope, 
                                                          Map<String, String> headers, String correlationId) {
        ApiRequest request = ApiRequest.builder()
                .endpoint(endpoint)
                .method(method)
                .payload(payload)
                .headers(headers)
                .apiType(ApiType.INTERNAL_APIGEE)
                .authConfig(AuthConfig.apigee(authScope))
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .build();
        
        return executeApiCall(request);
    }
    
    /**
     * External API call via Fenergo proxy with token authentication
     */
    public CompletableFuture<ApiResponse> callExternalApi(String actualEndpoint, ApiRequest.HttpMethod method, 
                                                         Object payload, String authScope, String proxyUrl,
                                                         Map<String, String> headers, String correlationId) {
        ApiRequest request = ApiRequest.builder()
                .endpoint(actualEndpoint) // This will be overridden by actualEndpoint in auth config
                .method(method)
                .payload(payload)
                .headers(headers)
                .apiType(ApiType.EXTERNAL_FENERGO_PROXY)
                .authConfig(AuthConfig.fenergo(authScope, proxyUrl, actualEndpoint))
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .build();
        
        return executeApiCall(request);
    }
}
