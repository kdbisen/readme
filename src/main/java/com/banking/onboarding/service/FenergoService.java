package com.banking.onboarding.service;

import com.banking.onboarding.auth.FenergoTokenService;
import com.banking.onboarding.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fenergo service for external API operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FenergoService {
    
    private final RestClient restClient;
    private final FenergoTokenService fenergoTokenService;
    
    @Value("${fenergo.entity.api.url:https://fenergo.example.com/entity}")
    private String entityCreateEndpoint;
    
    @Value("${fenergo.logic.engine.url:https://fenergo.example.com/journeylogicengine/api/engine/evaluate-journey-schema}")
    private String logicEngineEndpoint;
    
    @Value("${fenergo.journey.command.url:https://fenergo.example.com/api/journey-instance/launch-journey}")
    private String journeyCommandEndpoint;
    
    @Value("${fenergo.tenant.id:default-tenant}")
    private String tenantId;
    
    @Value("${fenergo.auth.scope:fenergo-api}")
    private String fenergoAuthScope;
    
    /**
     * Create entity in Fenergo system
     */
    public CompletableFuture<ApiResponse> createEntity(Map<String, Object> entityData, String correlationId) {
        log.info("[CORRELATION:{}] Creating entity in Fenergo", correlationId);
        
        try {
            // Get Fenergo JWT token
            String token = fenergoTokenService.getToken(fenergoAuthScope).getAccessToken();
            log.debug("[CORRELATION:{}] Using Fenergo JWT token for entity creation", correlationId);
            
            String response = restClient.post()
                    .uri(entityCreateEndpoint)
                    .header("X-Correlation-ID", correlationId)
                    .header("Authorization", "Bearer " + token)
                    .header("X-TENANT-ID", tenantId)
                    .header("Content-Type", "application/json")
                    .body(entityData)
                    .retrieve()
                    .body(String.class);
            
            return CompletableFuture.completedFuture(
                ApiResponse.success(response, entityCreateEndpoint, correlationId)
            );
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Entity creation failed: {}", correlationId, e.getMessage());
            return CompletableFuture.completedFuture(
                ApiResponse.error(500, e.getMessage(), entityCreateEndpoint, correlationId)
            );
        }
    }
    
    /**
     * Evaluate journey schema via Logic Engine
     */
    public CompletableFuture<ApiResponse> evaluateJourneySchema(Map<String, Object> evaluationData, String correlationId) {
        log.info("[CORRELATION:{}] Evaluating journey schema", correlationId);
        
        try {
            // Get Fenergo JWT token
            String token = fenergoTokenService.getToken(fenergoAuthScope).getAccessToken();
            log.debug("[CORRELATION:{}] Using Fenergo JWT token for schema evaluation", correlationId);
            
            String response = restClient.post()
                    .uri(logicEngineEndpoint)
                    .header("X-Correlation-ID", correlationId)
                    .header("Authorization", "Bearer " + token)
                    .header("X-TENANT-ID", tenantId)
                    .header("Content-Type", "application/json")
                    .body(evaluationData)
                    .retrieve()
                    .body(String.class);
            
            return CompletableFuture.completedFuture(
                ApiResponse.success(response, logicEngineEndpoint, correlationId)
            );
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Journey schema evaluation failed: {}", correlationId, e.getMessage());
            return CompletableFuture.completedFuture(
                ApiResponse.error(500, e.getMessage(), logicEngineEndpoint, correlationId)
            );
        }
    }
    
    /**
     * Launch journey via Journey Command API
     */
    public CompletableFuture<ApiResponse> launchJourney(Map<String, Object> launchData, String correlationId) {
        log.info("[CORRELATION:{}] Launching journey", correlationId);
        
        try {
            // Get Fenergo JWT token
            String token = fenergoTokenService.getToken(fenergoAuthScope).getAccessToken();
            log.debug("[CORRELATION:{}] Using Fenergo JWT token for journey launch", correlationId);
            
            String response = restClient.post()
                    .uri(journeyCommandEndpoint)
                    .header("X-Correlation-ID", correlationId)
                    .header("Authorization", "Bearer " + token)
                    .header("X-TENANT-ID", tenantId)
                    .header("Content-Type", "application/json")
                    .body(launchData)
                    .retrieve()
                    .body(String.class);
            
            return CompletableFuture.completedFuture(
                ApiResponse.success(response, journeyCommandEndpoint, correlationId)
            );
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Journey launch failed: {}", correlationId, e.getMessage());
            return CompletableFuture.completedFuture(
                ApiResponse.error(500, e.getMessage(), journeyCommandEndpoint, correlationId)
            );
        }
    }
}