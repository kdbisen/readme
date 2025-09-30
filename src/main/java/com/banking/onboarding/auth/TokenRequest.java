package com.banking.onboarding.auth;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * Token request for authentication service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {
    
    private String grantType;
    private String clientId;
    private String clientSecret;
    private String scope;
    private String audience;
    private Map<String, Object> additionalClaims;
    
    public static TokenRequest clientCredentials(String clientId, String clientSecret, String scope) {
        return TokenRequest.builder()
                .grantType("client_credentials")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope(scope)
                .build();
    }
}
