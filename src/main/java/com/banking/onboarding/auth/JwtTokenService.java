package com.banking.onboarding.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * JWT Token Service - Handles token fetching, caching, and refresh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {
    
    @Value("${auth.token-service.url:http://localhost:8080/auth/token}")
    private String tokenServiceUrl;
    
    @Value("${auth.token-service.client-id:banking-onboarding-service}")
    private String clientId;
    
    @Value("${auth.token-service.client-secret:secret}")
    private String clientSecret;
    
    @Value("${auth.token-service.default-scope:fenergo-api}")
    private String defaultScope;
    
    @Value("${auth.token-service.cache-enabled:true}")
    private boolean cacheEnabled;
    
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    
    // Token cache - key: scope, value: JwtToken
    private final Map<String, JwtToken> tokenCache = new ConcurrentHashMap<>();
    
    /**
     * Get JWT token for default scope
     */
    public JwtToken getToken() {
        return getToken(defaultScope);
    }
    
    /**
     * Get JWT token for specific scope
     */
    public JwtToken getToken(String scope) {
        if (!cacheEnabled) {
            return fetchNewToken(scope);
        }
        
        JwtToken cachedToken = tokenCache.get(scope);
        if (cachedToken != null && cachedToken.isValid()) {
            log.debug("Using cached token for scope: {}", scope);
            return cachedToken;
        }
        
        log.info("Fetching new token for scope: {}", scope);
        JwtToken newToken = fetchNewToken(scope);
        if (newToken != null && newToken.isValid()) {
            tokenCache.put(scope, newToken);
        }
        
        return newToken;
    }
    
    /**
     * Get token for specific endpoint configuration
     */
    public JwtToken getTokenForEndpoint(String endpointName, Map<String, Object> endpointConfig) {
        String scope = (String) endpointConfig.getOrDefault("authScope", defaultScope);
        boolean authRequired = (Boolean) endpointConfig.getOrDefault("authRequired", true);
        
        if (!authRequired) {
            log.debug("Authentication not required for endpoint: {}", endpointName);
            return null;
        }
        
        return getToken(scope);
    }
    
    /**
     * Force refresh token for specific scope
     */
    public JwtToken refreshToken(String scope) {
        log.info("Force refreshing token for scope: {}", scope);
        JwtToken newToken = fetchNewToken(scope);
        if (newToken != null && newToken.isValid()) {
            tokenCache.put(scope, newToken);
        }
        return newToken;
    }
    
    /**
     * Clear token cache
     */
    public void clearCache() {
        log.info("Clearing token cache");
        tokenCache.clear();
    }
    
    /**
     * Clear token cache for specific scope
     */
    public void clearCache(String scope) {
        log.info("Clearing token cache for scope: {}", scope);
        tokenCache.remove(scope);
    }
    
    private JwtToken fetchNewToken(String scope) {
        try {
            TokenRequest tokenRequest = TokenRequest.clientCredentials(clientId, clientSecret, scope);
            
            log.info("Fetching token from: {} with scope: {}", tokenServiceUrl, scope);
            
            ResponseEntity<Map> response = restClient
                    .post()
                    .uri(tokenServiceUrl)
                    .body(tokenRequest)
                    .retrieve()
                    .toEntity(Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                JwtToken token = JwtToken.builder()
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
                
                log.info("Successfully fetched token for scope: {}, expires at: {}", 
                        scope, token.getExpiresAt());
                
                return token;
            }
            
        } catch (RestClientException e) {
            log.warn("Failed to fetch token from external service for scope: {}, error: {}. Using mock token.", 
                    scope, e.getMessage());
            return createMockToken(scope);
        } catch (Exception e) {
            log.warn("Unexpected error fetching token for scope: {}. Using mock token.", scope, e);
            return createMockToken(scope);
        }
        
        // Fallback to mock token if no response
        log.warn("No response from token service for scope: {}. Using mock token.", scope);
        return createMockToken(scope);
    }
    
    /**
     * Create a mock JWT token for testing purposes
     */
    private JwtToken createMockToken(String scope) {
        log.info("Creating mock JWT token for scope: {}", scope);
        
        JwtToken mockToken = JwtToken.builder()
                .accessToken("mock-jwt-token-" + scope + "-" + System.currentTimeMillis())
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 hour
                .scope(scope)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        
        log.info("Created mock token for scope: {}, expires at: {}", scope, mockToken.getExpiresAt());
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
