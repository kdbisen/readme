package com.banking.onboarding.bridge;

import com.banking.onboarding.auth.ApigeeTokenService;
import com.banking.onboarding.auth.FenergoTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Unified Generic Bridge Service
 * Handles all external API calls through appropriate proxies and token services
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenericBridgeService {
    
    private final RestClient restClient;
    private final ApigeeTokenService apigeeTokenService;
    private final FenergoTokenService fenergoTokenService;
    
    /**
     * Execute API request through appropriate proxy and token service (Async)
     */
    @Async("bridgeTaskExecutor")
    public CompletableFuture<BridgeResponse> execute(BridgeRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[CORRELATION:{}] Executing async {} request to {} via {} proxy", 
                    request.getCorrelationId(), request.getMethod(), request.getEndpoint(), 
                    request.getApiProvider().getCode());
            
            // Get appropriate token based on API provider
            String token = getTokenForProvider(request.getApiProvider(), request.getAuthScope());
            
            // Build request based on API provider
            ResponseEntity<String> response = buildAndExecuteRequest(request, token);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.info("[CORRELATION:{}] Async request completed successfully in {}ms", 
                    request.getCorrelationId(), responseTime);
            
            BridgeResponse bridgeResponse = BridgeResponse.builder()
                    .success(true)
                    .statusCode(response.getStatusCode().value())
                    .statusText(response.getStatusCode().toString())
                    .body(response.getBody())
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .responseTimeMs(responseTime)
                    .apiProvider(request.getApiProvider())
                    .endpoint(request.getEndpoint())
                    .build();
            
            return CompletableFuture.completedFuture(bridgeResponse);
                    
        } catch (RestClientException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.error("[CORRELATION:{}] HTTP error in async request: {}", request.getCorrelationId(), e.getMessage());
            
            BridgeResponse bridgeResponse = BridgeResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .statusText("HTTP_ERROR")
                    .errorMessage(e.getMessage())
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .responseTimeMs(responseTime)
                    .apiProvider(request.getApiProvider())
                    .endpoint(request.getEndpoint())
                    .build();
            
            return CompletableFuture.completedFuture(bridgeResponse);
                    
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.error("[CORRELATION:{}] Unexpected error in async request: {}", request.getCorrelationId(), e.getMessage(), e);
            
            BridgeResponse bridgeResponse = BridgeResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .statusText("INTERNAL_ERROR")
                    .errorMessage(e.getMessage())
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .responseTimeMs(responseTime)
                    .apiProvider(request.getApiProvider())
                    .endpoint(request.getEndpoint())
                    .build();
            
            return CompletableFuture.completedFuture(bridgeResponse);
        }
    }
    
    /**
     * Get token from appropriate token service based on API provider
     */
    private String getTokenForProvider(ApiProvider apiProvider, String authScope) {
        switch (apiProvider) {
            case APIGEE:
                return apigeeTokenService.getToken(authScope);
            case FENERGO:
                return fenergoTokenService.getToken(authScope).getAccessToken();
            default:
                throw new IllegalArgumentException("Unsupported API provider: " + apiProvider);
        }
    }
    
    /**
     * Build and execute request based on API provider
     */
    private ResponseEntity<String> buildAndExecuteRequest(BridgeRequest request, String token) {
        switch (request.getApiProvider()) {
            case APIGEE:
                return buildApigeeRequest(request, token);
            case FENERGO:
                return buildFenergoRequest(request, token);
            default:
                throw new IllegalArgumentException("Unsupported API provider: " + request.getApiProvider());
        }
    }
    
    /**
     * Build request for Apigee (Internal Company APIs)
     */
    private ResponseEntity<String> buildApigeeRequest(BridgeRequest request, String token) {
        // For Apigee, we call the endpoint directly with Apigee token
        String endpoint = request.getEndpoint();
        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            endpoint = buildUriWithQueryParams(endpoint, request.getQueryParams());
        }
        
        RestClient.RequestBodySpec requestSpec = restClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token);
        
        // Add custom headers
        if (request.getHeaders() != null) {
            request.getHeaders().forEach(requestSpec::header);
        }
        
        // Add body for POST/PUT requests
        if (request.getPayload() != null && 
            (request.getMethod().equals("POST") || request.getMethod().equals("PUT"))) {
            requestSpec.body(request.getPayload());
        }
        
        return requestSpec.retrieve().toEntity(String.class);
    }
    
    /**
     * Build request for Fenergo (External Company APIs via proxy)
     */
    private ResponseEntity<String> buildFenergoRequest(BridgeRequest request, String token) {
        // For Fenergo, we call the proxy URL with Fenergo endpoint in header
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Fenergo-Endpoint", request.getActualEndpoint());
        headers.add("Authorization", "Bearer " + token);
        headers.add("X-Proxy-Service", "banking-onboarding-service");
        headers.add("X-Request-Source", "fenergo-bridge");
        
        // Add custom headers
        if (request.getHeaders() != null) {
            request.getHeaders().forEach(headers::add);
        }
        
        RestClient.RequestBodySpec requestSpec = restClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(request.getProxyUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(headers));
        
        // Add body for POST/PUT requests
        if (request.getPayload() != null && 
            (request.getMethod().equals("POST") || request.getMethod().equals("PUT"))) {
            requestSpec.body(request.getPayload());
        }
        
        return requestSpec.retrieve().toEntity(String.class);
    }
    
    /**
     * Build URI with query parameters
     */
    private String buildUriWithQueryParams(String endpoint, Map<String, String> queryParams) {
        StringBuilder uriBuilder = new StringBuilder(endpoint);
        uriBuilder.append("?");
        queryParams.forEach((key, value) -> 
                uriBuilder.append(key).append("=").append(value).append("&"));
        return uriBuilder.toString().replaceAll("&$", "");
    }
}
