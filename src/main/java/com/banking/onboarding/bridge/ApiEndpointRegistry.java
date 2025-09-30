package com.banking.onboarding.bridge;

import com.banking.onboarding.bridge.config.EndpointConfig;
import com.banking.onboarding.bridge.config.EndpointConfigurationLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for API endpoints - manages dynamic API configurations from properties
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiEndpointRegistry {
    
    @Value("${fenergo.api.base-url:http://localhost:8081/fenergo/api}")
    private String baseUrl;
    
    private final EndpointConfigurationLoader configLoader;
    private final Map<String, ApiEndpoint> endpoints = new ConcurrentHashMap<>();
    
    /**
     * Register a new API endpoint
     */
    public void registerEndpoint(ApiEndpoint endpoint) {
        endpoints.put(endpoint.getName(), endpoint);
        log.info("Registered API endpoint: {} -> {} {}", 
                endpoint.getName(), endpoint.getMethod(), endpoint.getPath());
    }
    
    /**
     * Get API endpoint by name
     */
    public ApiEndpoint getEndpoint(String name) {
        return endpoints.get(name);
    }
    
    /**
     * Check if endpoint exists
     */
    public boolean hasEndpoint(String name) {
        return endpoints.containsKey(name);
    }
    
    /**
     * Get all registered endpoints
     */
    public Map<String, ApiEndpoint> getAllEndpoints() {
        return Map.copyOf(endpoints);
    }
    
    /**
     * Build full URL for an endpoint
     */
    public String buildUrl(String endpointName) {
        ApiEndpoint endpoint = getEndpoint(endpointName);
        if (endpoint == null) {
            throw new IllegalArgumentException("Endpoint not found: " + endpointName);
        }
        
        String path = endpoint.getPath();
        if (path.startsWith("http")) {
            // Full URL provided
            return path;
        } else {
            // Relative path, prepend base URL
            return baseUrl + path;
        }
    }
    
    /**
     * Initialize endpoints from configuration
     */
    public void initializeDefaultEndpoints() {
        log.info("Initializing endpoints from configuration...");
        
        Map<String, EndpointConfig> configs = configLoader.getAllEndpointConfigs();
        
        if (configs.isEmpty()) {
            log.warn("No endpoint configurations found, using fallback endpoints");
            initializeFallbackEndpoints();
            return;
        }
        
        // Convert EndpointConfig to ApiEndpoint
        configs.forEach((name, config) -> {
            ApiEndpoint endpoint = ApiEndpoint.builder()
                    .name(config.getName())
                    .method(config.getMethod())
                    .path(config.getPath())
                    .description(config.getDescription())
                    .authRequired(config.getAuthRequired())
                    .authScope(config.getAuthScope())
                    .authType(config.getAuthType())
                    .timeoutMs(config.getTimeoutMs())
                    .retryAttempts(config.getRetryAttempts())
                    .defaultHeaders(config.getDefaultHeaders())
                    .defaultQueryParams(config.getDefaultQueryParams())
                    .build();
            
            registerEndpoint(endpoint);
        });
        
        log.info("Initialized {} endpoints from configuration", endpoints.size());
        
        // Log configuration statistics
        Map<String, Object> stats = configLoader.getConfigurationStats();
        log.info("Configuration stats: {}", stats);
    }
    
    /**
     * Fallback endpoints if configuration is not available
     */
    private void initializeFallbackEndpoints() {
        // Basic fallback endpoints
        registerEndpoint(ApiEndpoint.builder()
                .name("HEALTH_CHECK")
                .method("GET")
                .path("/health")
                .description("Health check endpoint")
                .authRequired(false)
                .build());
                
        registerEndpoint(ApiEndpoint.builder()
                .name("SUBMIT_KYC")
                .method("POST")
                .path("/kyc/submit")
                .description("Submit KYC documents")
                .authRequired(true)
                .authScope("fenergo-kyc-write")
                .build());
    }
    
    /**
     * Reload endpoints from configuration
     */
    public void reloadEndpoints() {
        log.info("Reloading endpoints from configuration...");
        endpoints.clear();
        configLoader.reloadConfigurations();
        initializeDefaultEndpoints();
    }
    
    /**
     * Get endpoint configuration statistics
     */
    public Map<String, Object> getEndpointStats() {
        Map<String, Object> stats = configLoader.getConfigurationStats();
        stats.put("registeredEndpoints", endpoints.size());
        stats.put("baseUrl", baseUrl);
        return stats;
    }
}