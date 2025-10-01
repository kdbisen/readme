package com.banking.onboarding.service;

import com.banking.onboarding.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
     * Complete onboarding workflow: XML -> JSON (Apigee) -> Fenergo Entity Create (Proxy)
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
                    
                    return fenergoService.createEntity(transformationResponse.getBody(), correlationId);
                });
    }
    
    /**
     * Multi-step Fenergo workflow: Entity Create -> Journey Info -> Journey Initiate
     */
    public CompletableFuture<ApiResponse> processFenergoWorkflow(Object initialData, String correlationId) {
        log.info("[CORRELATION:{}] Starting multi-step Fenergo workflow", correlationId);
        
        return fenergoService.createEntity(initialData, correlationId)
                .thenCompose(entityResponse -> {
                    if (!entityResponse.isSuccess()) {
                        log.error("[CORRELATION:{}] Entity creation failed: {}", 
                                correlationId, entityResponse.getErrorMessage());
                        return CompletableFuture.completedFuture(entityResponse);
                    }
                    
                    log.info("[CORRELATION:{}] Entity created successfully, getting journey info", correlationId);
                    
                    return fenergoService.getJourneyInfo(entityResponse.getBody(), correlationId);
                })
                .thenCompose(journeyInfoResponse -> {
                    if (!journeyInfoResponse.isSuccess()) {
                        log.error("[CORRELATION:{}] Journey info retrieval failed: {}", 
                                correlationId, journeyInfoResponse.getErrorMessage());
                        return CompletableFuture.completedFuture(journeyInfoResponse);
                    }
                    
                    log.info("[CORRELATION:{}] Journey info retrieved successfully, initiating journey", correlationId);
                    
                    return fenergoService.initiateJourney(journeyInfoResponse.getBody(), correlationId);
                });
    }
    
    /**
     * Complete end-to-end workflow: XML -> JSON -> Entity Create -> Journey Info -> Journey Initiate
     */
    public CompletableFuture<ApiResponse> processCompleteWorkflow(String xmlData, String correlationId) {
        log.info("[CORRELATION:{}] Starting complete end-to-end workflow", correlationId);
        
        return transformationService.transformXmlToJson(xmlData, correlationId)
                .thenCompose(transformationResponse -> {
                    if (!transformationResponse.isSuccess()) {
                        return CompletableFuture.completedFuture(transformationResponse);
                    }
                    
                    return fenergoService.createEntity(transformationResponse.getBody(), correlationId);
                })
                .thenCompose(entityResponse -> {
                    if (!entityResponse.isSuccess()) {
                        return CompletableFuture.completedFuture(entityResponse);
                    }
                    
                    return fenergoService.getJourneyInfo(entityResponse.getBody(), correlationId);
                })
                .thenCompose(journeyInfoResponse -> {
                    if (!journeyInfoResponse.isSuccess()) {
                        return CompletableFuture.completedFuture(journeyInfoResponse);
                    }
                    
                    return fenergoService.initiateJourney(journeyInfoResponse.getBody(), correlationId);
                });
    }
}
