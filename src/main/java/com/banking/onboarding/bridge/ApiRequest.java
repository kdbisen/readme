package com.banking.onboarding.bridge;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * Generic API request for the bridge system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequest {
    
    private String endpoint;
    private String method; // GET, POST, PUT, DELETE
    private Map<String, Object> headers;
    private Map<String, Object> queryParams;
    private Object payload;
    private String correlationId;
    private Integer timeoutMs;
    private Integer retryAttempts;
    
    // Authentication fields
    private Boolean authRequired;
    private String authScope;
    private String authType;
    
    public static ApiRequestBuilder builder() {
        return new ApiRequestBuilder()
                .headers(new HashMap<>())
                .queryParams(new HashMap<>())
                .timeoutMs(30000)
                .retryAttempts(3);
    }
}
