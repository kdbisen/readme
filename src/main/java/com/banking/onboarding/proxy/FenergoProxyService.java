package com.banking.onboarding.proxy;

import com.banking.onboarding.auth.FenergoTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Proxy service for Fenergo API calls with header-based routing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FenergoProxyService {
    
    private final RestClient restClient;
    private final FenergoTokenService fenergoTokenService;
    
    /**
     * Make a proxy call to Fenergo API
     * 
     * @param proxyUrl The proxy URL to call
     * @param fenergoEndpoint The full Fenergo endpoint URL (passed in header)
     * @param method HTTP method
     * @param payload Request payload
     * @param authType Authentication type (JWT or OCIN)
     * @param authScope Authentication scope
     * @return Proxy response
     */
    public ProxyResponse callFenergoApi(String proxyUrl, String fenergoEndpoint, 
                                       HttpMethod method, Object payload, 
                                       String authType, String authScope) {
        
        log.info("Making proxy call to: {} for Fenergo endpoint: {}", proxyUrl, fenergoEndpoint);
        
        try {
            // Build headers
            HttpHeaders headers = buildHeaders(fenergoEndpoint, authType, authScope);
            
            // Make the call
            ResponseEntity<String> response = restClient
                    .method(method)
                    .uri(proxyUrl)
                    .headers(h -> h.addAll(headers))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload != null ? payload : "")
                    .retrieve()
                    .toEntity(String.class);
            
            String responseBody = response.getBody();
            int statusCode = response.getStatusCode().value();
            
            log.info("Proxy call successful to: {} - Status: {}", proxyUrl, statusCode);
            return ProxyResponse.success(responseBody, fenergoEndpoint);
            
        } catch (RestClientException e) {
            log.error("Proxy call failed to: {} - Error: {}", proxyUrl, e.getMessage());
            return ProxyResponse.error(500, "Proxy call failed", fenergoEndpoint, e.getMessage());
            
        } catch (Exception e) {
            log.error("Unexpected error in proxy call to: {}", proxyUrl, e);
            return ProxyResponse.error(500, "Internal Server Error", fenergoEndpoint, e.getMessage());
        }
    }
    
    /**
     * Build headers for proxy call
     */
    private HttpHeaders buildHeaders(String fenergoEndpoint, String authType, String authScope) {
        HttpHeaders headers = new HttpHeaders();
        
        // Add Fenergo endpoint URL in header
        headers.add("X-Fenergo-Endpoint", fenergoEndpoint);
        
        // Add authentication token
        if ("JWT".equalsIgnoreCase(authType)) {
            String token = getJwtToken(authScope);
            headers.add("Authorization", "Bearer " + token);
        } else if ("OCIN".equalsIgnoreCase(authType)) {
            String token = getOcinToken(authScope);
            headers.add("Authorization", "OCIN " + token);
        }
        
        // Add proxy-specific headers
        headers.add("X-Proxy-Service", "banking-onboarding-service");
        headers.add("X-Request-Source", "fenergo-proxy");
        
        return headers;
    }
    
    /**
     * Get JWT token for authentication
     */
    private String getJwtToken(String scope) {
        try {
            return fenergoTokenService.getToken(scope).getAccessToken();
        } catch (Exception e) {
            log.error("Failed to get JWT token for scope: {}", scope, e);
            throw new ProxyException("Failed to get JWT token", e);
        }
    }
    
    /**
     * Get OCIN token for authentication
     */
    private String getOcinToken(String scope) {
        try {
            // For now, use JWT service - can be extended for OCIN
            return fenergoTokenService.getToken(scope).getAccessToken();
        } catch (Exception e) {
            log.error("Failed to get OCIN token for scope: {}", scope, e);
            throw new ProxyException("Failed to get OCIN token", e);
        }
    }
    
    /**
     * Check if proxy is available
     */
    public boolean isProxyAvailable(String proxyUrl) {
        try {
            ResponseEntity<String> response = restClient
                    .get()
                    .uri(proxyUrl + "/health")
                    .retrieve()
                    .toEntity(String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Proxy not available at: {}", proxyUrl);
            return false;
        }
    }
}
