package com.banking.onboarding.api.strategy.impl;

import com.banking.onboarding.domain.ApiRequest;
import com.banking.onboarding.domain.ApiResponse;
import com.banking.onboarding.domain.ApiType;
import com.banking.onboarding.api.strategy.ApiCallStrategy;
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
 * Direct API call strategy - no authentication required
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DirectApiCallStrategy implements ApiCallStrategy {
    
    private final RestClient restClient;
    
    @Override
    public CompletableFuture<ApiResponse> execute(ApiRequest request) {
        log.info("[CORRELATION:{}] Executing direct API call to: {}", 
                request.getCorrelationId(), request.getEndpoint());
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                HttpHeaders headers = buildHeaders(request);
                
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
                
                log.info("[CORRELATION:{}] Direct API call completed successfully in {}ms", 
                        request.getCorrelationId(), responseTime);
                
                return ApiResponse.builder()
                        .success(true)
                        .statusCode(response.getStatusCode().value())
                        .statusText(response.getStatusCode().toString())
                        .body(response.getBody())
                        .correlationId(request.getCorrelationId())
                        .timestamp(LocalDateTime.now())
                        .responseTimeMs(responseTime)
                        .apiType(ApiType.DIRECT)
                        .endpoint(request.getEndpoint())
                        .build();
                        
            } catch (RestClientException e) {
                long responseTime = System.currentTimeMillis() - startTime;
                
                log.error("[CORRELATION:{}] Direct API call failed: {}", 
                        request.getCorrelationId(), e.getMessage());
                
                return ApiResponse.builder()
                        .success(false)
                        .statusCode(500)
                        .statusText("HTTP_ERROR")
                        .errorMessage(e.getMessage())
                        .correlationId(request.getCorrelationId())
                        .timestamp(LocalDateTime.now())
                        .responseTimeMs(responseTime)
                        .apiType(ApiType.DIRECT)
                        .endpoint(request.getEndpoint())
                        .build();
            }
        });
    }
    
    @Override
    public boolean supports(ApiRequest request) {
        return request.getApiType() == ApiType.DIRECT;
    }
    
    @Override
    public String getStrategyName() {
        return "DirectApiCallStrategy";
    }
    
    private HttpHeaders buildHeaders(ApiRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (request.getHeaders() != null) {
            request.getHeaders().forEach(headers::add);
        }
        
        return headers;
    }
}
