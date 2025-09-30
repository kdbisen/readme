package com.banking.onboarding.bridge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for API endpoints - manages dynamic API configurations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiEndpointRegistry {
    
    @Value("${fenergo.api.base-url:http://localhost:8081/fenergo/api}")
    private String baseUrl;
    
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
        return baseUrl + endpoint.getPath();
    }
    
    /**
     * Initialize default Fenergo endpoints
     */
    public void initializeDefaultEndpoints() {
        // Entity Management - All require authentication
        registerEndpoint(ApiEndpoint.builder()
                .name("CREATE_ENTITY")
                .method("POST")
                .path("/entities")
                .description("Create new entity")
                .authRequired(true)
                .authScope("fenergo-entity-write")
                .build());
                
        registerEndpoint(ApiEndpoint.builder()
                .name("GET_ENTITY")
                .method("GET")
                .path("/entities/{entityId}")
                .description("Get entity by ID")
                .authRequired(true)
                .authScope("fenergo-entity-read")
                .build());
                
        registerEndpoint(ApiEndpoint.builder()
                .name("UPDATE_ENTITY")
                .method("PUT")
                .path("/entities/{entityId}")
                .description("Update entity")
                .authRequired(true)
                .authScope("fenergo-entity-write")
                .build());
                
        registerEndpoint(ApiEndpoint.builder()
                .name("DELETE_ENTITY")
                .method("DELETE")
                .path("/entities/{entityId}")
                .description("Delete entity")
                .authRequired(true)
                .authScope("fenergo-entity-write")
                .build());
        
        // KYC Operations - Mixed auth requirements
        registerEndpoint(ApiEndpoint.builder()
                .name("SUBMIT_KYC")
                .method("POST")
                .path("/kyc/submit")
                .description("Submit KYC documents")
                .authRequired(true)
                .authScope("fenergo-kyc-write")
                .build());
                
        registerEndpoint(ApiEndpoint.builder()
                .name("GET_KYC_STATUS")
                .method("GET")
                .path("/kyc/{entityId}/status")
                .description("Get KYC status")
                .authRequired(true)
                .authScope("fenergo-kyc-read")
                .build());
        
        // Compliance Operations - All require auth
        registerEndpoint(ApiEndpoint.builder()
                .name("RUN_COMPLIANCE_CHECK")
                .method("POST")
                .path("/compliance/check")
                .description("Run compliance check")
                .authRequired(true)
                .authScope("fenergo-compliance-write")
                .build());
                
        registerEndpoint(ApiEndpoint.builder()
                .name("GET_COMPLIANCE_RESULTS")
                .method("GET")
                .path("/compliance/{entityId}/results")
                .description("Get compliance results")
                .authRequired(true)
                .authScope("fenergo-compliance-read")
                .build());
        
        // Risk Assessment - All require auth
        registerEndpoint(ApiEndpoint.builder()
                .name("ASSESS_RISK")
                .method("POST")
                .path("/risk/assess")
                .description("Assess entity risk")
                .authRequired(true)
                .authScope("fenergo-risk-write")
                .build());
                
        registerEndpoint(ApiEndpoint.builder()
                .name("GET_RISK_PROFILE")
                .method("GET")
                .path("/risk/{entityId}/profile")
                .description("Get risk profile")
                .authRequired(true)
                .authScope("fenergo-risk-read")
                .build());
        
        // Public endpoints - No auth required
        registerEndpoint(ApiEndpoint.builder()
                .name("HEALTH_CHECK")
                .method("GET")
                .path("/health")
                .description("Health check endpoint")
                .authRequired(false)
                .build());
                
        registerEndpoint(ApiEndpoint.builder()
                .name("API_INFO")
                .method("GET")
                .path("/api/info")
                .description("API information")
                .authRequired(false)
                .build());
        
        log.info("Initialized {} default Fenergo API endpoints", endpoints.size());
    }
}
