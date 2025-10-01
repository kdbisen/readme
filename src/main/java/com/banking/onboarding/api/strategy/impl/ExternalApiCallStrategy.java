package com.banking.onboarding.api.strategy.impl;

import com.banking.onboarding.domain.ApiRequest;
import com.banking.onboarding.domain.ApiResponse;
import com.banking.onboarding.domain.ApiType;
import com.banking.onboarding.domain.AuthConfig;
import com.banking.onboarding.api.strategy.ApiCallStrategy;
import com.banking.onboarding.auth.FenergoTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * External API call strategy via Fenergo proxy with token authentication
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalApiCallStrategy implements ApiCallStrategy {
    
    private final RestClient restClient;
    private final FenergoTokenService fenergoTokenService;
    
    @Override
    public CompletableFuture<ApiResponse> execute(ApiRequest request) {
        log.info("[CORRELATION:{}] Executing external API call via Fenergo proxy to: {}", 
                request.getCorrelationId(), request.getAuthConfig().getActualEndpoint());
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                // Get Fenergo token
                String token = fenergoTokenService.getToken(request.getAuthConfig().getAuthScope()).getAccessToken();
                
                HttpHeaders headers = buildHeaders(request, token);
                
                RestClient.RequestBodySpec requestSpec = restClient
                        .method(HttpMethod.valueOf(request.getMethod().name()))
                        .uri(request.getAuthConfig().getProxyUrl())
                        .headers(h -> h.addAll(headers));
                
                // Add body for POST/PUT requests
                if (request.getPayload() != null && 
                    (request.getMethod() == ApiRequest.HttpMethod.POST || 
                     request.getMethod() == ApiRequest.HttpMethod.PUT)) {
                    requestSpec.body(request.getPayload());
                }
                
                ResponseEntity<String> response = requestSpec.retrieve().toEntity(String.class);
                long responseTime = System.currentTimeMillis() - startTime;
                
                log.info("[CORRELATION:{}] External API call via Fenergo proxy completed successfully in {}ms", 
                        request.getCorrelationId(), responseTime);
                
                return ApiResponse.builder()
                        .success(true)
                        .statusCode(response.getStatusCode().value())
                        .statusText(response.getStatusCode().toString())
                        .body(response.getBody())
                        .correlationId(request.getCorrelationId())
                        .timestamp(LocalDateTime.now())
                        .responseTimeMs(responseTime)
                        .apiType(ApiType.EXTERNAL_FENERGO_PROXY)
                        .endpoint(request.getAuthConfig().getActualEndpoint())
                        .build();
                        
            } catch (RestClientException e) {
                long responseTime = System.currentTimeMillis() - startTime;
                
                log.error("[CORRELATION:{}] External API call via Fenergo proxy failed: {}", 
                        request.getCorrelationId(), e.getMessage());
                
                return ApiResponse.builder()
                        .success(false)
                        .statusCode(500)
                        .statusText("HTTP_ERROR")
                        .errorMessage(e.getMessage())
                        .correlationId(request.getCorrelationId())
                        .timestamp(LocalDateTime.now())
                        .responseTimeMs(responseTime)
                        .apiType(ApiType.EXTERNAL_FENERGO_PROXY)
                        .endpoint(request.getAuthConfig().getActualEndpoint())
                        .build();
            }
        });
    }
    
    @Override
    public boolean supports(ApiRequest request) {
        return request.getApiType() == ApiType.EXTERNAL_FENERGO_PROXY &&
               request.getAuthConfig().getAuthType() == AuthConfig.AuthType.FENERGO_TOKEN;
    }
    
    @Override
    public String getStrategyName() {
        return "ExternalApiCallStrategy";
    }
    
    private HttpHeaders buildHeaders(ApiRequest request, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        // Add Fenergo-specific headers
        headers.add("X-Fenergo-Endpoint", request.getAuthConfig().getActualEndpoint());
        headers.add("X-Proxy-Service", "banking-onboarding-service");
        headers.add("X-Request-Source", "external-api-strategy");
        
        if (request.getHeaders() != null) {
            request.getHeaders().forEach(headers::add);
        }
        
        return headers;
    }
}
