package com.banking.onboarding.domain;

import lombok.Builder;
import lombok.Value;

/**
 * Authentication configuration for API calls
 */
@Value
@Builder
public class AuthConfig {
    AuthType authType;
    String authScope;
    String tokenServiceUrl;
    String clientId;
    String clientSecret;
    String proxyUrl;
    String actualEndpoint;
    
    public enum AuthType {
        NONE,
        APIGEE_TOKEN,
        FENERGO_TOKEN,
        CUSTOM_TOKEN
    }
    
    public static AuthConfig none() {
        return AuthConfig.builder()
                .authType(AuthType.NONE)
                .build();
    }
    
    public static AuthConfig apigee(String authScope) {
        return AuthConfig.builder()
                .authType(AuthType.APIGEE_TOKEN)
                .authScope(authScope)
                .build();
    }
    
    public static AuthConfig fenergo(String authScope, String proxyUrl, String actualEndpoint) {
        return AuthConfig.builder()
                .authType(AuthType.FENERGO_TOKEN)
                .authScope(authScope)
                .proxyUrl(proxyUrl)
                .actualEndpoint(actualEndpoint)
                .build();
    }
}
