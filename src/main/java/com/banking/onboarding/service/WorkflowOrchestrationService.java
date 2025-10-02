package com.banking.onboarding.service;

import com.banking.onboarding.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Orchestration service for complex multi-step workflows
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowOrchestrationService {
    
    private final TransformationService transformationService;
    private final FenergoService fenergoService;
    
    /**
     * Complete onboarding workflow: XML -> JSON (Apigee) -> Fenergo Entity Create
     */
    public CompletableFuture<ApiResponse> processOnboardingWorkflow(String xmlData, String correlationId) {
        log.info("[CORRELATION:{}] Starting complete onboarding workflow", correlationId);
        
        return transformationService.transformXmlToJson(xmlData, correlationId)
                .thenCompose(transformationResponse -> {
                    if (!transformationResponse.isSuccess()) {
                        log.error("[CORRELATION:{}] XML to JSON transformation failed: {}", 
                                correlationId, transformationResponse.getErrorMessage());
                        return CompletableFuture.completedFuture(transformationResponse);
                    }
                    
                    log.info("[CORRELATION:{}] XML to JSON transformation successful, proceeding to Fenergo", correlationId);
                    
                    // Convert response body to Map for entity creation
                    Map<String, Object> entityData = Map.of("data", transformationResponse.getBody());
                    return fenergoService.createEntity(entityData, correlationId);
                });
    }
    
    /**
     * Multi-step Fenergo workflow: Entity Create -> Journey Schema Evaluation -> Journey Launch
     */
    public CompletableFuture<ApiResponse> processFenergoWorkflow(Map<String, Object> initialData, String correlationId) {
        log.info("[CORRELATION:{}] Starting multi-step Fenergo workflow", correlationId);
        
        return fenergoService.createEntity(initialData, correlationId)
                .thenCompose(entityResponse -> {
                    if (!entityResponse.isSuccess()) {
                        log.error("[CORRELATION:{}] Entity creation failed: {}", 
                                correlationId, entityResponse.getErrorMessage());
                        return CompletableFuture.completedFuture(entityResponse);
                    }
                    
                    log.info("[CORRELATION:{}] Entity created successfully, evaluating journey schema", correlationId);
                    
                    // Create evaluation data
                    Map<String, Object> evaluationData = Map.of(
                        "entityId", "extracted-from-response", // This would be extracted from entityResponse
                        "data", entityResponse.getBody()
                    );
                    return fenergoService.evaluateJourneySchema(evaluationData, correlationId);
                })
                .thenCompose(evaluationResponse -> {
                    if (!evaluationResponse.isSuccess()) {
                        log.error("[CORRELATION:{}] Journey schema evaluation failed: {}", 
                                correlationId, evaluationResponse.getErrorMessage());
                        return CompletableFuture.completedFuture(evaluationResponse);
                    }
                    
                    log.info("[CORRELATION:{}] Journey schema evaluated successfully, launching journey", correlationId);
                    
                    // Create launch data
                    Map<String, Object> launchData = Map.of(
                        "entityId", "extracted-from-response",
                        "journeySchemaId", "extracted-from-evaluation",
                        "data", evaluationResponse.getBody()
                    );
                    return fenergoService.launchJourney(launchData, correlationId);
                });
    }
    
    /**
     * Complete end-to-end workflow: XML -> JSON -> Entity Create -> Journey Schema Evaluation -> Journey Launch
     */
    public CompletableFuture<ApiResponse> processCompleteWorkflow(String xmlData, String correlationId) {
        log.info("[CORRELATION:{}] Starting complete end-to-end workflow", correlationId);
        
        return transformationService.transformXmlToJson(xmlData, correlationId)
                .thenCompose(transformationResponse -> {
                    if (!transformationResponse.isSuccess()) {
                        return CompletableFuture.completedFuture(transformationResponse);
                    }
                    
                    Map<String, Object> entityData = Map.of("data", transformationResponse.getBody());
                    return fenergoService.createEntity(entityData, correlationId);
                })
                .thenCompose(entityResponse -> {
                    if (!entityResponse.isSuccess()) {
                        return CompletableFuture.completedFuture(entityResponse);
                    }
                    
                    Map<String, Object> evaluationData = Map.of(
                        "entityId", "extracted-from-response",
                        "data", entityResponse.getBody()
                    );
                    return fenergoService.evaluateJourneySchema(evaluationData, correlationId);
                })
                .thenCompose(evaluationResponse -> {
                    if (!evaluationResponse.isSuccess()) {
                        return CompletableFuture.completedFuture(evaluationResponse);
                    }
                    
                    Map<String, Object> launchData = Map.of(
                        "entityId", "extracted-from-response",
                        "journeySchemaId", "extracted-from-evaluation",
                        "data", evaluationResponse.getBody()
                    );
                    return fenergoService.launchJourney(launchData, correlationId);
                });
    }
}