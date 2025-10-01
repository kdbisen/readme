package com.banking.onboarding.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * DTO for API request
 */
@Data
@Builder
public class ApiRequestDto {
    
    private String endpoint;
    private HttpMethod method;
    private Object payload;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private String authScope;
    
    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH
    }
}
