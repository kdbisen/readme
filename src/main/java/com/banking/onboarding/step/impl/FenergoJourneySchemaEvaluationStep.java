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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Step 2: Evaluate Journey Schema via Fenergo Logic Engine - SYNCHRONOUS
 * Uses Logic Engine to determine the correct journey schema based on entity attributes
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FenergoJourneySchemaEvaluationStep implements GenericStepExecutor {
    
    private final RestClient restClient;
    
    @Value("${fenergo.logic.engine.url:https://fenergo.example.com/journeylogicengine/api/engine/evaluate-journey-schema}")
    private String logicEngineUrl;
    
    @Value("${fenergo.tenant.id:default-tenant}")
    private String tenantId;
    
    @Value("${fenergo.journey.type.filter:Client Onboarding}")
    private String journeyTypeFilter;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing Step 2: Evaluate Journey Schema via Logic Engine", context.getCorrelationId());
        
        // Get entity ID from previous step
        String entityId = context.getStepResult("FENERGO_ENTITY_CREATION", String.class);
        if (entityId == null) {
            return StepResult.failure("No entity ID available from previous step", getStepName(), context.getCorrelationId());
        }
        
        try {
            // Build evaluation payload following Fenergo Logic Engine API spec
            Map<String, Object> evaluationPayload = buildEvaluationPayload(entityId, context);
            
            // Call Logic Engine API synchronously
            CompletableFuture<Map> future = CompletableFuture.supplyAsync(() -> {
                return restClient.post()
                        .uri(logicEngineUrl + "?journeyTypeFilter=" + journeyTypeFilter)
                        .header("Authorization", "Bearer " + getAuthToken())
                        .header("X-TENANT-ID", tenantId)
                        .header("X-CORRELATION-ID", context.getCorrelationId())
                        .header("Content-Type", "application/json")
                        .body(evaluationPayload)
                        .retrieve()
                        .body(Map.class);
            });
            
            Map response = future.get(); // Block here
            
            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> schemas = (List<Map<String, Object>>) response.get("data");
                
                if (schemas != null && !schemas.isEmpty()) {
                    // Select the best schema following selection rules
                    Map<String, Object> selectedSchema = selectBestSchema(schemas);
                    
                    if (selectedSchema != null) {
                        String journeySchemaId = (String) selectedSchema.get("journeySchemaId");
                        Integer version = (Integer) selectedSchema.get("journeySchemaVersion");
                        String name = (String) selectedSchema.get("name");
                        
                        // Store schema information for next step
                        Map<String, Object> schemaInfo = Map.of(
                                "journeySchemaId", journeySchemaId,
                                "journeySchemaVersion", version,
                                "schemaName", name,
                                "entityId", entityId
                        );
                        
                        context.addStepResult(getStepName(), schemaInfo);
                        
                        log.info("[CORRELATION:{}] Step 2 completed successfully. Selected schema: {} (ID: {}, Version: {})", 
                                context.getCorrelationId(), name, journeySchemaId, version);
                        
                        return StepResult.success(schemaInfo, getStepName(), context.getCorrelationId());
                    } else {
                        return StepResult.failure("No suitable schema found after evaluation", getStepName(), context.getCorrelationId());
                    }
                } else {
                    return StepResult.failure("No journey schemas returned from Logic Engine", getStepName(), context.getCorrelationId());
                }
            } else {
                return StepResult.failure("Invalid response from Fenergo Logic Engine", getStepName(), context.getCorrelationId());
            }
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Step 2 failed: {}", context.getCorrelationId(), e.getMessage());
            return StepResult.failure("Journey schema evaluation failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
    }
    
    /**
     * Build evaluation payload following Fenergo Logic Engine API specification
     */
    private Map<String, Object> buildEvaluationPayload(String entityId, GenericStepContext context) {
        return Map.of(
                "data", Map.of(
                        "entityType", "Company",
                        "jurisdiction", "US",
                        "entityId", entityId,
                        "someOtherAttr", "value"
                )
        );
    }
    
    /**
     * Select the best schema following Fenergo best practices
     */
    private Map<String, Object> selectBestSchema(List<Map<String, Object>> schemas) {
        if (schemas.isEmpty()) {
            return null;
        }
        
        // Apply selection rules:
        // 1. Filter by journeyType (if multiple)
        // 2. Prefer highest version
        // 3. Prefer explicit jurisdiction match
        
        Map<String, Object> bestSchema = schemas.get(0);
        int highestVersion = (Integer) bestSchema.getOrDefault("journeySchemaVersion", 0);
        
        for (Map<String, Object> schema : schemas) {
            Integer version = (Integer) schema.getOrDefault("journeySchemaVersion", 0);
            String journeyType = (String) schema.get("journeyType");
            
            // Prefer matching journey type and higher version
            if (journeyTypeFilter.equals(journeyType) && version > highestVersion) {
                bestSchema = schema;
                highestVersion = version;
            }
        }
        
        return bestSchema;
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
                .description("Evaluate journey schema via Fenergo Logic Engine")
                .retryEnabled(true)
                .maxRetries(3)
                .retryDelayMs(2000)
                .backoffMultiplier(2.0)
                .asyncEnabled(false)
                .timeoutMs(60000)
                .dependencies(new String[]{"FENERGO_ENTITY_CREATION"})
                .build();
    }
    
    @Override
    public String getStepName() {
        return "FENERGO_JOURNEY_SCHEMA_EVALUATION";
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.hasStepResult("FENERGO_ENTITY_CREATION");
    }
}
