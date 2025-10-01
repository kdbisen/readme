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
 * Step 1: Create Entity via Fenergo Entity API - SYNCHRONOUS
 * Creates entity in Fenergo system following exact API patterns
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FenergoEntityCreationStep implements GenericStepExecutor {
    
    private final RestClient restClient;
    
    @Value("${fenergo.entity.api.url:https://fenergo.example.com/entity}")
    private String entityApiUrl;
    
    @Value("${fenergo.tenant.id:default-tenant}")
    private String tenantId;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing Step 1: Create Entity via Fenergo Entity API", context.getCorrelationId());
        
        // Get JSON data from previous step
        Object jsonData = getInputData(context);
        if (jsonData == null) {
            return StepResult.failure("No JSON data available from previous step", getStepName(), context.getCorrelationId());
        }
        
        try {
            // Build entity creation payload following Fenergo API spec
            Map<String, Object> entityPayload = buildEntityPayload(jsonData, context);
            
            // Call Fenergo Entity API synchronously
            CompletableFuture<Map> future = CompletableFuture.supplyAsync(() -> {
                return restClient.post()
                        .uri(entityApiUrl)
                        .header("Authorization", "Bearer " + getAuthToken())
                        .header("X-TENANT-ID", tenantId)
                        .header("X-CORRELATION-ID", context.getCorrelationId())
                        .header("Content-Type", "application/json")
                        .body(entityPayload)
                        .retrieve()
                        .body(Map.class);
            });
            
            Map response = future.get(); // Block here
            
            if (response != null && response.containsKey("data")) {
                Map<String, Object> responseData = (Map<String, Object>) response.get("data");
                String entityId = (String) responseData.get("entityId");
                
                if (entityId != null) {
                    // Store entity ID for next steps
                    context.addStepResult(getStepName(), entityId);
                    
                    log.info("[CORRELATION:{}] Step 1 completed successfully. Entity created with ID: {}", 
                            context.getCorrelationId(), entityId);
                    
                    return StepResult.success(entityId, getStepName(), context.getCorrelationId());
                } else {
                    return StepResult.failure("Entity creation succeeded but no entityId returned", getStepName(), context.getCorrelationId());
                }
            } else {
                return StepResult.failure("Invalid response from Fenergo Entity API", getStepName(), context.getCorrelationId());
            }
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Step 1 failed: {}", context.getCorrelationId(), e.getMessage());
            return StepResult.failure("Entity creation failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
    }
    
    /**
     * Build entity payload following Fenergo API specification
     */
    private Map<String, Object> buildEntityPayload(Object jsonData, GenericStepContext context) {
        // Parse JSON data and extract entity information
        // This is a simplified example - in real implementation, you'd parse the JSON properly
        
        return Map.of(
                "data", Map.of(
                        "type", "Company",
                        "targetEntity", "Client",
                        "properties", Map.of(
                                "name", Map.of("type", "Single", "value", "Acme Ltd"),
                                "jurisdiction", Map.of("type", "Single", "value", "US"),
                                "entityType", Map.of("type", "Single", "value", "Company")
                        ),
                        "policyJurisdictions", new String[]{"US"}
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
                .description("Create entity in Fenergo system via Entity API")
                .retryEnabled(true)
                .maxRetries(3)
                .retryDelayMs(2000)
                .backoffMultiplier(2.0)
                .asyncEnabled(false)
                .timeoutMs(60000)
                .dependencies(new String[]{"XML_TO_JSON_TRANSFORMATION"})
                .build();
    }
    
    @Override
    public String getStepName() {
        return "FENERGO_ENTITY_CREATION";
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.hasStepResult("XML_TO_JSON_TRANSFORMATION") || context.getInputData() != null;
    }
}
