package com.banking.onboarding.bridge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified Bridge Response for all external API calls
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BridgeResponse {
    
    private boolean success;
    private int statusCode;
    private String statusText;
    private String body;
    private Map<String, String> headers;
    private String errorMessage;
    private String correlationId;
    private LocalDateTime timestamp;
    private long responseTimeMs;
    private ApiProvider apiProvider;
    private String endpoint;
    
    // Success response factory
    public static BridgeResponse success(String body, String endpoint, ApiProvider apiProvider, String correlationId) {
        return BridgeResponse.builder()
                .success(true)
                .statusCode(200)
                .statusText("OK")
                .body(body)
                .endpoint(endpoint)
                .apiProvider(apiProvider)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Error response factory
    public static BridgeResponse error(int statusCode, String errorMessage, String endpoint, ApiProvider apiProvider, String correlationId) {
        return BridgeResponse.builder()
                .success(false)
                .statusCode(statusCode)
                .statusText("ERROR")
                .errorMessage(errorMessage)
                .endpoint(endpoint)
                .apiProvider(apiProvider)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
