package com.banking.onboarding.api.strategy.impl;

import com.banking.onboarding.domain.ApiRequest;
import com.banking.onboarding.domain.ApiResponse;
import com.banking.onboarding.domain.ApiType;
import com.banking.onboarding.domain.AuthConfig;
import com.banking.onboarding.api.strategy.ApiCallStrategy;
import com.banking.onboarding.auth.ApigeeTokenService;
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
 * Internal API call strategy via Apigee with token authentication
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InternalApiCallStrategy implements ApiCallStrategy {
    
    private final RestClient restClient;
    private final ApigeeTokenService apigeeTokenService;
    
    @Override
    public CompletableFuture<ApiResponse> execute(ApiRequest request) {
        log.info("[CORRELATION:{}] Executing internal API call via Apigee to: {}", 
                request.getCorrelationId(), request.getEndpoint());
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                // Get Apigee token
                String token = apigeeTokenService.getToken(request.getAuthConfig().getAuthScope());
                
                HttpHeaders headers = buildHeaders(request, token);
                
                RestClient.RequestBodySpec requestSpec = restClient
                        .method(HttpMethod.valueOf(request.getMethod().name()))
                        .uri(request.getEndpoint())
                        .headers(h -> h.addAll(headers));
                
                // Add body for POST/PUT requests
                if (request.getPayload() != null && 
                    (request.getMethod() == ApiRequest.HttpMethod.POST || 
                     request.getMethod() == ApiRequest.HttpMethod.PUT)) {
                    requestSpec.body(request.getPayload());
                }
                
                ResponseEntity<String> response = requestSpec.retrieve().toEntity(String.class);
                long responseTime = System.currentTimeMillis() - startTime;
                
                log.info("[CORRELATION:{}] Internal API call via Apigee completed successfully in {}ms", 
                        request.getCorrelationId(), responseTime);
                
                return ApiResponse.builder()
                        .success(true)
                        .statusCode(response.getStatusCode().value())
                        .statusText(response.getStatusCode().toString())
                        .body(response.getBody())
                        .correlationId(request.getCorrelationId())
                        .timestamp(LocalDateTime.now())
                        .responseTimeMs(responseTime)
                        .apiType(ApiType.INTERNAL_APIGEE)
                        .endpoint(request.getEndpoint())
                        .build();
                        
            } catch (RestClientException e) {
                long responseTime = System.currentTimeMillis() - startTime;
                
                log.error("[CORRELATION:{}] Internal API call via Apigee failed: {}", 
                        request.getCorrelationId(), e.getMessage());
                
                return ApiResponse.builder()
                        .success(false)
                        .statusCode(500)
                        .statusText("HTTP_ERROR")
                        .errorMessage(e.getMessage())
                        .correlationId(request.getCorrelationId())
                        .timestamp(LocalDateTime.now())
                        .responseTimeMs(responseTime)
                        .apiType(ApiType.INTERNAL_APIGEE)
                        .endpoint(request.getEndpoint())
                        .build();
            }
        });
    }
    
    @Override
    public boolean supports(ApiRequest request) {
        return request.getApiType() == ApiType.INTERNAL_APIGEE &&
               request.getAuthConfig().getAuthType() == AuthConfig.AuthType.APIGEE_TOKEN;
    }
    
    @Override
    public String getStrategyName() {
        return "InternalApiCallStrategy";
    }
    
    private HttpHeaders buildHeaders(ApiRequest request, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        if (request.getHeaders() != null) {
            request.getHeaders().forEach(headers::add);
        }
        
        return headers;
    }
}
