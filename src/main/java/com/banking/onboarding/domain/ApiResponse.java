package com.banking.onboarding.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Domain model for API response
 */
@Value
@Builder
public class ApiResponse {
    boolean success;
    int statusCode;
    String statusText;
    String body;
    Map<String, String> headers;
    String errorMessage;
    String correlationId;
    LocalDateTime timestamp;
    long responseTimeMs;
    String endpoint;
    
    public static ApiResponse success(String body, String endpoint, String correlationId) {
        return ApiResponse.builder()
                .success(true)
                .statusCode(200)
                .statusText("OK")
                .body(body)
                .endpoint(endpoint)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ApiResponse error(int statusCode, String errorMessage, String endpoint, String correlationId) {
        return ApiResponse.builder()
                .success(false)
                .statusCode(statusCode)
                .statusText("ERROR")
                .errorMessage(errorMessage)
                .endpoint(endpoint)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
