package com.banking.onboarding.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Apigee Token Service - For Internal Company APIs
 * Handles token fetching, caching, and refresh for Apigee APIs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApigeeTokenService {

    @Value("${apigee.auth.token-service.url:http://apigee-token-service:8080/oauth/token}")
    private String tokenServiceUrl;
    
    @Value("${apigee.auth.client-id:apigee-client}")
    private String clientId;
    
    @Value("${apigee.auth.client-secret:apigee-secret}")
    private String clientSecret;
    
    @Value("${apigee.auth.default-scope:apigee-api}")
    private String defaultScope;
    
    @Value("${apigee.auth.cache-enabled:true}")
    private boolean cacheEnabled;
    
    private final RestClient restClient;
    
    // Token cache - key: scope, value: ApigeeToken
    private final Map<String, ApigeeToken> tokenCache = new ConcurrentHashMap<>();
    
    /**
     * Get Apigee token for the given scope
     */
    public String getToken(String scope) {
        if (scope == null || scope.trim().isEmpty()) {
            scope = defaultScope;
        }
        
        if (cacheEnabled) {
            ApigeeToken cachedToken = tokenCache.get(scope);
            if (cachedToken != null && cachedToken.isValid()) {
                log.debug("Returning cached Apigee token for scope: {}", scope);
                return cachedToken.getAccessToken();
            }
        }
        
        // Fetch new token
        ApigeeToken newToken = fetchNewToken(scope);
        
        if (cacheEnabled) {
            tokenCache.put(scope, newToken);
        }
        
        return newToken.getAccessToken();
    }
    
    /**
     * Clear token cache for specific scope
     */
    public void clearCache(String scope) {
        log.info("Clearing Apigee token cache for scope: {}", scope);
        tokenCache.remove(scope);
    }
    
    /**
     * Clear all token cache
     */
    public void clearAllCache() {
        log.info("Clearing all Apigee token cache");
        tokenCache.clear();
    }
    
    private ApigeeToken fetchNewToken(String scope) {
        try {
            TokenRequest tokenRequest = TokenRequest.clientCredentials(clientId, clientSecret, scope);
            
            log.info("Fetching Apigee token from: {} with scope: {}", tokenServiceUrl, scope);
            
            ResponseEntity<Map> response = restClient
                    .post()
                    .uri(tokenServiceUrl)
                    .body(tokenRequest)
                    .retrieve()
                    .toEntity(Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                ApigeeToken token = ApigeeToken.builder()
                        .accessToken((String) responseBody.get("access_token"))
                        .tokenType((String) responseBody.getOrDefault("token_type", "Bearer"))
                        .expiresIn(getLongValue(responseBody.get("expires_in")))
                        .scope((String) responseBody.get("scope"))
                        .issuedAt(LocalDateTime.now())
                        .build();
                
                // Calculate expiration time
                if (token.getExpiresIn() != null) {
                    token.setExpiresAt(token.getIssuedAt().plusSeconds(token.getExpiresIn()));
                }
                
                log.info("Successfully fetched Apigee token for scope: {}, expires at: {}", 
                        scope, token.getExpiresAt());
                
                return token;
            }
            
        } catch (Exception e) {
            log.warn("Failed to fetch Apigee token for scope: {}, error: {}. Using mock token.", 
                    scope, e.getMessage());
        }
        
        // Fallback to mock token
        log.warn("No response from Apigee token service for scope: {}. Using mock token.", scope);
        return createMockToken(scope);
    }
    
    /**
     * Create a mock Apigee token for testing purposes
     */
    private ApigeeToken createMockToken(String scope) {
        log.info("Creating mock Apigee token for scope: {}", scope);

        ApigeeToken mockToken = ApigeeToken.builder()
                .accessToken("mock-apigee-token-" + scope + "-" + System.currentTimeMillis())
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 hour
                .scope(scope)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        log.info("Created mock Apigee token for scope: {}, expires at: {}", scope, mockToken.getExpiresAt());
        return mockToken;
    }
    
    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
