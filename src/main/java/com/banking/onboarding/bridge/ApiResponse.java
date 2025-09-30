package com.banking.onboarding.bridge;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic API response from the bridge system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    
    private boolean success;
    private int statusCode;
    private String statusText;
    private Object data;
    private Map<String, String> headers;
    private String errorMessage;
    private String correlationId;
    private LocalDateTime timestamp;
    private long responseTimeMs;
    
    public static ApiResponse success(Object data, int statusCode) {
        return ApiResponse.builder()
                .success(true)
                .statusCode(statusCode)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ApiResponse error(String errorMessage, int statusCode) {
        return ApiResponse.builder()
                .success(false)
                .statusCode(statusCode)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
