package com.banking.onboarding.bridge;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * API endpoint configuration for the bridge system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiEndpoint {
    
    private String name;
    private String method; // GET, POST, PUT, DELETE
    private String path;
    private Map<String, Object> defaultHeaders;
    private Map<String, Object> defaultQueryParams;
    private Integer timeoutMs;
    private Integer retryAttempts;
    private String description;
    
    // Authentication configuration
    private Boolean authRequired;
    private String authScope;
    private String authType; // JWT, API_KEY, BASIC
    
    public static ApiEndpointBuilder builder() {
        return new ApiEndpointBuilder()
                .defaultHeaders(new HashMap<>())
                .defaultQueryParams(new HashMap<>())
                .timeoutMs(30000)
                .retryAttempts(3)
                .authRequired(true) // Default to requiring auth
                .authType("JWT");
    }
}
