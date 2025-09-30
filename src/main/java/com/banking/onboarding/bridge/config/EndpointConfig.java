package com.banking.onboarding.bridge.config;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * Configuration model for API endpoints loaded from properties
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointConfig {
    
    private String name;
    private String method;
    private String path;
    private String description;
    private Boolean authRequired;
    private String authScope;
    private String authType;
    private Integer timeoutMs;
    private Integer retryAttempts;
    private Map<String, Object> defaultHeaders;
    private Map<String, Object> defaultQueryParams;
    
    public static EndpointConfigBuilder builder() {
        return new EndpointConfigBuilder()
                .defaultHeaders(new HashMap<>())
                .defaultQueryParams(new HashMap<>())
                .authRequired(true)
                .authType("JWT")
                .timeoutMs(30000)
                .retryAttempts(3);
    }
}
