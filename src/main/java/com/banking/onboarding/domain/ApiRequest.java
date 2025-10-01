package com.banking.onboarding.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Domain model for API request
 */
@Value
@Builder
public class ApiRequest {
    String endpoint;
    HttpMethod method;
    Object payload;
    Map<String, String> headers;
    Map<String, String> queryParams;
    ApiType apiType;
    AuthConfig authConfig;
    String correlationId;
    LocalDateTime timestamp;
    
    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH
    }
}
