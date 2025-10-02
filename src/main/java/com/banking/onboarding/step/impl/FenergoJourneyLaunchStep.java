package com.banking.onboarding.step.impl;

import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Step 3: Launch Journey via Fenergo Journey Command API - SYNCHRONOUS
 * Launches the journey using Direct Launch approach for simplicity
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FenergoJourneyLaunchStep implements GenericStepExecutor {
    
    private final RestClient restClient;
    
    @Value("${fenergo.journey.command.url:https://fenergo.example.com/api/journey-instance/launch-journey}")
    private String journeyCommandUrl;
    
    @Value("${fenergo.tenant.id:default-tenant}")
    private String tenantId;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing Step 3: Launch Journey via Journey Command API", context.getCorrelationId());
        
        // Get schema information from previous step
        Map<String, Object> schemaInfo = context.getStepResult("FENERGO_JOURNEY_SCHEMA_EVALUATION", Map.class);
        if (schemaInfo == null) {
            return StepResult.failure("No schema information available from previous step", getStepName(), context.getCorrelationId());
        }
        
        String entityId = (String) schemaInfo.get("entityId");
        String journeySchemaId = (String) schemaInfo.get("journeySchemaId");
        Integer version = (Integer) schemaInfo.get("journeySchemaVersion");
        
        if (entityId == null || journeySchemaId == null) {
            return StepResult.failure("Missing required entityId or journeySchemaId", getStepName(), context.getCorrelationId());
        }
        
        try {
            // Build launch payload following Fenergo Journey Command API spec
            Map<String, Object> launchPayload = buildLaunchPayload(entityId, journeySchemaId, version, context);
            
            // Call Journey Command API synchronously
            CompletableFuture<Map> future = CompletableFuture.supplyAsync(() -> {
                return restClient.post()
                        .uri(journeyCommandUrl)
                        .header("Authorization", "Bearer " + getAuthToken())
                        .header("X-TENANT-ID", tenantId)
                        .header("X-CORRELATION-ID", context.getCorrelationId())
                        .header("Content-Type", "application/json")
                        .body(launchPayload)
                        .retrieve()
                        .body(Map.class);
            });
            
            Map response = future.get(); // Block here
            
            if (response != null) {
                // Store journey launch information
                Map<String, Object> launchInfo = Map.of(
                        "entityId", entityId,
                        "journeySchemaId", journeySchemaId,
                        "journeySchemaVersion", version,
                        "launchResponse", response,
                        "status", "LAUNCHED"
                );
                
                context.addStepResult(getStepName(), launchInfo);
                
                log.info("[CORRELATION:{}] Step 3 completed successfully. Journey launched for entity: {} with schema: {}", 
                        context.getCorrelationId(), entityId, journeySchemaId);
                
                return StepResult.success(launchInfo, getStepName(), context.getCorrelationId());
            } else {
                return StepResult.failure("No response from Fenergo Journey Command API", getStepName(), context.getCorrelationId());
            }
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Step 3 failed: {}", context.getCorrelationId(), e.getMessage());
            return StepResult.failure("Journey launch failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
    }
    
    /**
     * Build launch payload following Fenergo Journey Command API specification
     * Using Direct Launch approach for simplicity
     */
    private Map<String, Object> buildLaunchPayload(String entityId, String journeySchemaId, Integer version, GenericStepContext context) {
        return Map.of(
                "data", Map.of(
                        "entityId", entityId,
                        "journeyType", "Client Onboarding",
                        "journeySchemaId", journeySchemaId,
                        "journeySchemaVersionNumber", version,
                        "jurisdictions", new String[]{"US"},
                        "accessLayers", Map.of(
                                "internal", true,
                                "external", false
                        )
                )
        );
    }
    
    /**
     * Get authentication token (simplified - in real implementation, use proper token service)
     */
    private String getAuthToken() {
        // In real implementation, this would call your token service
        return "mock-fenergo-token";
    }
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description("Launch journey via Fenergo Journey Command API")
                .retryEnabled(true)
                .maxRetries(3)
                .retryDelayMs(3000)
                .backoffMultiplier(2.0)
                .asyncEnabled(false)
                .timeoutMs(60000)
                .dependencies(new String[]{"FENERGO_JOURNEY_SCHEMA_EVALUATION"})
                .build();
    }
    
    @Override
    public String getStepName() {
        return "FENERGO_JOURNEY_LAUNCH";
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.hasStepResult("FENERGO_JOURNEY_SCHEMA_EVALUATION");
    }
}

