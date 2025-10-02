package com.banking.onboarding.service;

import com.banking.onboarding.auth.ApigeeTokenService;
import com.banking.onboarding.circuitbreaker.CircuitBreaker;
import com.banking.onboarding.domain.ApiResponse;
import com.banking.onboarding.retry.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Transformation service for data format conversions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransformationService {
    
    private final RestClient restClient;
    private final ApigeeTokenService apigeeTokenService;
    private final CircuitBreaker circuitBreaker;
    private final RetryService retryService;
    
    @Value("${apigee.transformation.endpoint:https://apigee-transformation-service.com/api/v1/transform}")
    private String transformationEndpoint;
    
    @Value("${apigee.transformation.auth-scope:transformation-api}")
    private String transformationAuthScope;
    
    /**
     * Transform XML to JSON via Apigee transformation service
     */
    public CompletableFuture<ApiResponse> transformXmlToJson(String xmlData, String correlationId) {
        log.info("[CORRELATION:{}] Initiating XML to JSON transformation", correlationId);
        
        Map<String, Object> payload = Map.of("inputData", xmlData);
        
        try {
            // Use circuit breaker and retry for resilient API calls
            String response = circuitBreaker.execute("apigee", () -> 
                retryService.executeWithRetry(() -> {
                    // Get Apigee token for transformation API
                    String token = apigeeTokenService.getToken(transformationAuthScope);
                    log.debug("[CORRELATION:{}] Using Apigee token for transformation", correlationId);
                    
                    return restClient.post()
                            .uri(transformationEndpoint)
                            .header("X-Correlation-ID", correlationId)
                            .header("Authorization", "Bearer " + token)
                            .header("X-Service-Type", "transformation")
                            .header("X-Input-Format", "XML")
                            .header("X-Output-Format", "JSON")
                            .body(payload)
                            .retrieve()
                            .body(String.class);
                }, "Apigee XML to JSON transformation")
            );
            
            return CompletableFuture.completedFuture(
                ApiResponse.success(response, transformationEndpoint, correlationId)
            );
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] XML to JSON transformation failed: {}", correlationId, e.getMessage());
            return CompletableFuture.completedFuture(
                ApiResponse.error(500, e.getMessage(), transformationEndpoint, correlationId)
            );
        }
    }
    
    /**
     * Transform JSON to XML via Apigee transformation service
     */
    public CompletableFuture<ApiResponse> transformJsonToXml(String jsonData, String correlationId) {
        log.info("[CORRELATION:{}] Initiating JSON to XML transformation", correlationId);
        
        Map<String, Object> payload = Map.of("inputData", jsonData);
        
        try {
            // Get Apigee token for transformation API
            String token = apigeeTokenService.getToken(transformationAuthScope);
            log.debug("[CORRELATION:{}] Using Apigee token for transformation", correlationId);
            
            String response = restClient.post()
                    .uri(transformationEndpoint)
                    .header("X-Correlation-ID", correlationId)
                    .header("Authorization", "Bearer " + token)
                    .header("X-Service-Type", "transformation")
                    .header("X-Input-Format", "JSON")
                    .header("X-Output-Format", "XML")
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            
            return CompletableFuture.completedFuture(
                ApiResponse.success(response, transformationEndpoint, correlationId)
            );
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] JSON to XML transformation failed: {}", correlationId, e.getMessage());
            return CompletableFuture.completedFuture(
                ApiResponse.error(500, e.getMessage(), transformationEndpoint, correlationId)
            );
        }
    }
}