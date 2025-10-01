package com.banking.onboarding.bridge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified Bridge Request for all external API calls
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BridgeRequest {
    
    private String endpoint;
    private String method;
    private Object payload;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private ApiProvider apiProvider;
    private String authScope;
    private String correlationId;
    private Long timeoutMs;
    private Integer retryAttempts;
    
    // Proxy-specific fields (for external APIs like Fenergo)
    private String proxyUrl;
    private String actualEndpoint; // The real endpoint behind the proxy
    
    // Apigee-specific fields (for internal APIs)
    private boolean useApigeeProxy;
    private String apigeeEndpoint;
}
