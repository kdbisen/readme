package com.banking.onboarding.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
    
    private final WebClient webClient;
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
            
            Map<String, Object> response = webClient
                    .post()
                    .uri(tokenServiceUrl)
                    .bodyValue(tokenRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null) {
                JwtToken token = JwtToken.builder()
                        .accessToken((String) response.get("access_token"))
                        .tokenType((String) response.getOrDefault("token_type", "Bearer"))
                        .expiresIn(getLongValue(response.get("expires_in")))
                        .scope((String) response.get("scope"))
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
            
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch token for scope: {}, status: {}, response: {}", 
                    scope, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error fetching token for scope: {}", scope, e);
        }
        
        return null;
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
