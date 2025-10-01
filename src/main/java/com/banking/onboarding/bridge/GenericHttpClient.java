package com.banking.onboarding.bridge;

import com.banking.onboarding.auth.JwtToken;
import com.banking.onboarding.auth.FenergoTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic HTTP client for API bridge operations - Using RestClient
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenericHttpClient {
    
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    
    private final FenergoTokenService fenergoTokenService;
    
    /**
     * Execute API request using RestClient with automatic JWT token handling
     */
    public ApiResponse execute(ApiRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[CORRELATION:{}] RestClient executing {} request to: {}", 
                    request.getCorrelationId(), request.getMethod(), request.getEndpoint());
            
            // Build RestClient request with query parameters
            String endpoint = request.getEndpoint();
            if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
                StringBuilder uriBuilder = new StringBuilder(endpoint);
                uriBuilder.append("?");
                request.getQueryParams().forEach((key, value) -> 
                        uriBuilder.append(key).append("=").append(value).append("&"));
                endpoint = uriBuilder.toString().replaceAll("&$", "");
            }
            
            RestClient.RequestBodySpec requestSpec = restClient
                    .method(HttpMethod.valueOf(request.getMethod()))
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON);
            
            // Add headers
            if (request.getHeaders() != null) {
                request.getHeaders().forEach((key, value) -> 
                        requestSpec.header(key, String.valueOf(value)));
            }
            
            // Add JWT token if authentication is required
            if (request.getAuthRequired() != null && request.getAuthRequired()) {
                JwtToken token = fenergoTokenService.getToken(request.getAuthScope());
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
                requestSpec.body(request.getPayload());
            }
            
            // Execute request
            ResponseEntity<String> response = requestSpec
                    .retrieve()
                    .toEntity(String.class);
            
            long responseTime = System.currentTimeMillis() - startTime;
            Object responseData = parseResponse(response.getBody());
            
            log.info("[CORRELATION:{}] RestClient request completed successfully in {}ms", 
                    request.getCorrelationId(), responseTime);
            
            return ApiResponse.builder()
                    .success(true)
                    .statusCode(response.getStatusCode().value())
                    .statusText(response.getStatusCode().toString())
                    .data(responseData)
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .responseTimeMs(responseTime)
                    .build();
                    
        } catch (RestClientException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.error("[CORRELATION:{}] RestClient HTTP error: {}", 
                    request.getCorrelationId(), e.getMessage());
            
            return ApiResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .statusText("HTTP_ERROR")
                    .errorMessage(e.getMessage())
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .responseTimeMs(responseTime)
                    .build();
                    
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.error("[CORRELATION:{}] RestClient unexpected error: {}", 
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
