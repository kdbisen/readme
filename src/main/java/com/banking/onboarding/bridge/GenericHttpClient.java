package com.banking.onboarding.bridge;

import com.banking.onboarding.auth.JwtToken;
import com.banking.onboarding.auth.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic HTTP client for API bridge operations - Using WebClient (BEST MODERN ALTERNATIVE)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenericHttpClient {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    private final JwtTokenService jwtTokenService;
    
    /**
     * Execute API request using WebClient with automatic JWT token handling
     */
    public ApiResponse execute(ApiRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[CORRELATION:{}] WebClient executing {} request to: {}", 
                    request.getCorrelationId(), request.getMethod(), request.getEndpoint());
            
            // Build WebClient request
            WebClient.RequestBodySpec requestSpec = webClient
                    .method(HttpMethod.valueOf(request.getMethod()))
                    .uri(request.getEndpoint(), uriBuilder -> {
                        if (request.getQueryParams() != null) {
                            request.getQueryParams().forEach(uriBuilder::queryParam);
                        }
                        return uriBuilder.build();
                    })
                    .contentType(MediaType.APPLICATION_JSON);
            
            // Add headers
            if (request.getHeaders() != null) {
                request.getHeaders().forEach((key, value) -> 
                        requestSpec.header(key, String.valueOf(value)));
            }
            
            // Add JWT token if authentication is required
            if (request.getAuthRequired() != null && request.getAuthRequired()) {
                JwtToken token = jwtTokenService.getToken(request.getAuthScope());
                if (token != null && token.isValid()) {
                    String authHeader = token.getTokenType() + " " + token.getAccessToken();
                    requestSpec.header("Authorization", authHeader);
                    log.debug("[CORRELATION:{}] Added JWT token to request", request.getCorrelationId());
                } else {
                    log.warn("[CORRELATION:{}] Failed to get valid JWT token for scope: {}", 
                            request.getCorrelationId(), request.getAuthScope());
                }
            }
            
            // Add body for POST/PUT requests
            if (request.getPayload() != null && 
                (request.getMethod().equals("POST") || request.getMethod().equals("PUT"))) {
                requestSpec.bodyValue(request.getPayload());
            }
            
            // Execute with retry and timeout
            String responseBody = requestSpec
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(request.getTimeoutMs()))
                    .retryWhen(Retry.fixedDelay(request.getRetryAttempts(), Duration.ofMillis(1000)))
                    .block();
            
            long responseTime = System.currentTimeMillis() - startTime;
            Object responseData = parseResponse(responseBody);
            
            log.info("[CORRELATION:{}] WebClient request completed successfully in {}ms", 
                    request.getCorrelationId(), responseTime);
            
            return ApiResponse.builder()
                    .success(true)
                    .statusCode(200)
                    .statusText("OK")
                    .data(responseData)
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .responseTimeMs(responseTime)
                    .build();
                    
        } catch (WebClientResponseException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.error("[CORRELATION:{}] WebClient HTTP error {}: {}", 
                    request.getCorrelationId(), e.getStatusCode(), e.getResponseBodyAsString());
            
            return ApiResponse.builder()
                    .success(false)
                    .statusCode(e.getStatusCode().value())
                    .statusText(e.getStatusText())
                    .errorMessage(e.getResponseBodyAsString())
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .responseTimeMs(responseTime)
                    .build();
                    
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.error("[CORRELATION:{}] WebClient unexpected error: {}", 
                    request.getCorrelationId(), e.getMessage(), e);
            
            return ApiResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .statusText("INTERNAL_ERROR")
                    .errorMessage(e.getMessage())
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .responseTimeMs(responseTime)
                    .build();
        }
    }
    
    private Object parseResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, Object.class);
        } catch (Exception e) {
            log.warn("Failed to parse JSON response, returning as string: {}", e.getMessage());
            return responseBody;
        }
    }
}
