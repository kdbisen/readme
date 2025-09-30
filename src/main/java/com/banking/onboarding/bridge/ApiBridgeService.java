package com.banking.onboarding.bridge;

import com.banking.onboarding.service.CorrelationIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Main API Bridge Service - provides a clean interface for calling any Fenergo API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiBridgeService {
    
    private final GenericHttpClient httpClient;
    private final ApiEndpointRegistry endpointRegistry;
    private final CorrelationIdService correlationIdService;
    
    /**
     * Call Fenergo API by endpoint name with payload
     */
    public ApiResponse callApi(String endpointName, Object payload) {
        return callApi(endpointName, payload, Map.of(), Map.of());
    }
    
    /**
     * Call Fenergo API by endpoint name with payload and custom headers
     */
    public ApiResponse callApi(String endpointName, Object payload, Map<String, Object> headers) {
        return callApi(endpointName, payload, headers, Map.of());
    }
    
    /**
     * Call Fenergo API by endpoint name with payload, headers, and query params
     */
    public ApiResponse callApi(String endpointName, Object payload, 
                             Map<String, Object> headers, Map<String, Object> queryParams) {
        
        String correlationId = correlationIdService.getCurrentCorrelationId();
        
        log.info("[CORRELATION:{}] Calling Fenergo API: {} with payload", 
                correlationId, endpointName);
        
        // Get endpoint configuration
        ApiEndpoint endpoint = endpointRegistry.getEndpoint(endpointName);
        if (endpoint == null) {
            return ApiResponse.error("Endpoint not found: " + endpointName, 404);
        }
        
        // Build full URL
        String fullUrl = endpointRegistry.buildUrl(endpointName);
        
        // Merge default headers with custom headers
        Map<String, Object> mergedHeaders = mergeMaps(endpoint.getDefaultHeaders(), headers);
        
        // Merge default query params with custom query params
        Map<String, Object> mergedQueryParams = mergeMaps(endpoint.getDefaultQueryParams(), queryParams);
        
        // Create API request with authentication configuration
        ApiRequest request = ApiRequest.builder()
                .endpoint(fullUrl)
                .method(endpoint.getMethod())
                .headers(mergedHeaders)
                .queryParams(mergedQueryParams)
                .payload(payload)
                .correlationId(correlationId)
                .timeoutMs(endpoint.getTimeoutMs())
                .retryAttempts(endpoint.getRetryAttempts())
                .authRequired(endpoint.getAuthRequired())
                .authScope(endpoint.getAuthScope())
                .authType(endpoint.getAuthType())
                .build();
        
        // Execute request
        return httpClient.execute(request);
    }
    
    /**
     * Call Fenergo API with direct URL (for dynamic endpoints)
     */
    public ApiResponse callApiDirect(String url, String method, Object payload) {
        return callApiDirect(url, method, payload, Map.of(), Map.of());
    }
    
    /**
     * Call Fenergo API with direct URL and custom parameters
     */
    public ApiResponse callApiDirect(String url, String method, Object payload,
                                   Map<String, Object> headers, Map<String, Object> queryParams) {
        
        String correlationId = correlationIdService.getCurrentCorrelationId();
        
        log.info("[CORRELATION:{}] Calling Fenergo API directly: {} {}", 
                correlationId, method, url);
        
        // Create API request
        ApiRequest request = ApiRequest.builder()
                .endpoint(url)
                .method(method)
                .headers(headers)
                .queryParams(queryParams)
                .payload(payload)
                .correlationId(correlationId)
                .timeoutMs(30000)
                .retryAttempts(3)
                .build();
        
        // Execute request
        return httpClient.execute(request);
    }
    
    /**
     * Get all available endpoints
     */
    public Map<String, ApiEndpoint> getAvailableEndpoints() {
        return endpointRegistry.getAllEndpoints();
    }
    
    /**
     * Check if endpoint exists
     */
    public boolean hasEndpoint(String endpointName) {
        return endpointRegistry.hasEndpoint(endpointName);
    }
    
    /**
     * Register new endpoint dynamically
     */
    public void registerEndpoint(ApiEndpoint endpoint) {
        endpointRegistry.registerEndpoint(endpoint);
    }
    
    private Map<String, Object> mergeMaps(Map<String, Object> defaultMap, Map<String, Object> customMap) {
        Map<String, Object> merged = Map.copyOf(defaultMap);
        merged.putAll(customMap);
        return merged;
    }
}
