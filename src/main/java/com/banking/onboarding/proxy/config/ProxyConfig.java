package com.banking.onboarding.proxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration for proxy service
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {
    
    /**
     * Proxy URL configuration
     */
    private String baseUrl = "http://localhost:8081";
    
    /**
     * Proxy health check endpoint
     */
    private String healthEndpoint = "/health";
    
    /**
     * Proxy timeout configuration
     */
    private TimeoutConfig timeout = new TimeoutConfig();
    
    /**
     * Fenergo endpoint mappings
     */
    private Map<String, FenergoEndpointConfig> endpoints;
    
    /**
     * Authentication configuration
     */
    private AuthConfig auth = new AuthConfig();
    
    @Data
    public static class TimeoutConfig {
        private int connect = 5000;
        private int read = 30000;
        private int write = 30000;
    }
    
    @Data
    public static class FenergoEndpointConfig {
        private String url;
        private String method;
        private String authType;
        private String authScope;
        private int timeout;
        private int retries;
    }
    
    @Data
    public static class AuthConfig {
        private String defaultType = "JWT";
        private String defaultScope = "fenergo-api";
        private boolean enabled = true;
    }
    
    /**
     * Get Fenergo endpoint configuration
     */
    public FenergoEndpointConfig getEndpointConfig(String endpointName) {
        return endpoints != null ? endpoints.get(endpointName) : null;
    }
    
    /**
     * Get full proxy URL for health check
     */
    public String getHealthCheckUrl() {
        return baseUrl + healthEndpoint;
    }
    
    /**
     * Get full proxy URL for API calls
     */
    public String getProxyUrl() {
        return baseUrl;
    }
}
