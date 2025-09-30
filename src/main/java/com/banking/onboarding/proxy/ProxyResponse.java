package com.banking.onboarding.proxy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from proxy service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyResponse {
    
    private boolean success;
    private int statusCode;
    private String responseBody;
    private String fenergoEndpoint;
    private String errorMessage;
    private long responseTimeMs;
    
    /**
     * Create successful response
     */
    public static ProxyResponse success(String responseBody, String fenergoEndpoint) {
        return ProxyResponse.builder()
                .success(true)
                .statusCode(200)
                .responseBody(responseBody)
                .fenergoEndpoint(fenergoEndpoint)
                .responseTimeMs(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create error response
     */
    public static ProxyResponse error(int statusCode, String responseBody, 
                                    String fenergoEndpoint, String errorMessage) {
        return ProxyResponse.builder()
                .success(false)
                .statusCode(statusCode)
                .responseBody(responseBody)
                .fenergoEndpoint(fenergoEndpoint)
                .errorMessage(errorMessage)
                .responseTimeMs(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Check if response is successful
     */
    public boolean isSuccess() {
        return success && statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Get error message or response body
     */
    public String getMessage() {
        return errorMessage != null ? errorMessage : responseBody;
    }
}
