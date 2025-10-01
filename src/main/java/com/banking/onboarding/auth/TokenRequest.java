package com.banking.onboarding.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple Token Request model
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
    
    public static TokenRequest clientCredentials(String clientId, String clientSecret, String scope) {
        return TokenRequest.builder()
                .grantType("client_credentials")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope(scope)
                .build();
    }
}
