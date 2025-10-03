package com.banking.onboarding.step.impl;

import com.banking.onboarding.constants.OnboardingConstants;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import com.banking.onboarding.step.enhancer.StepContextEnhancer;
import com.banking.onboarding.step.util.DynamicStepLoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fenergo Journey Launch Step - Dynamic Step Numbering
 * Launches the journey using Direct Launch approach for simplicity
 * Uses dynamic step numbering instead of hardcoded step numbers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FenergoJourneyLaunchStep implements GenericStepExecutor {
    
    private final RestClient restClient;
    private final DynamicStepLoggingUtil stepLoggingUtil;
    private final StepContextEnhancer stepContextEnhancer;
    
    @Value("${fenergo.journey.command.url:https://fenergo.example.com/api/journey-instance/launch-journey}")
    private String journeyCommandUrl;
    
    @Value("${fenergo.tenant.id:default-tenant}")
    private String tenantId;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Enhance context with dynamic step numbering
        StepContextEnhancer.EnhancedStepContext enhancedContext = 
            stepContextEnhancer.enhanceContext(context, getStepName());
        
        // Log step start with dynamic step number
        stepLoggingUtil.logStepStart(enhancedContext, "Launch Journey via Journey Command API");
        
        // Get schema information from previous step
        Map<String, Object> schemaInfo = context.getStepResult(OnboardingConstants.StepNames.FENERGO_JOURNEY_SCHEMA_EVALUATION, Map.class);
        if (schemaInfo == null) {
            stepLoggingUtil.logStepFailure(enhancedContext, OnboardingConstants.Messages.NO_SCHEMA_INFO_AVAILABLE);
            return StepResult.failure(OnboardingConstants.Messages.NO_SCHEMA_INFO_AVAILABLE, getStepName(), context.getCorrelationId());
        }
        
        String entityId = (String) schemaInfo.get("entityId");
        String journeySchemaId = (String) schemaInfo.get("journeySchemaId");
        Integer version = (Integer) schemaInfo.get("journeySchemaVersion");
        
        if (entityId == null || journeySchemaId == null) {
            stepLoggingUtil.logStepFailure(enhancedContext, OnboardingConstants.Messages.MISSING_ENTITY_ID_OR_SCHEMA);
            return StepResult.failure(OnboardingConstants.Messages.MISSING_ENTITY_ID_OR_SCHEMA, getStepName(), context.getCorrelationId());
        }
        
        try {
            // Build launch payload following Fenergo Journey Command API spec
            Map<String, Object> launchPayload = buildLaunchPayload(entityId, journeySchemaId, version, context);
            
            // Call Journey Command API synchronously
            CompletableFuture<Map> future = CompletableFuture.supplyAsync(() -> {
                return restClient.post()
                        .uri(journeyCommandUrl)
                        .header(OnboardingConstants.HttpHeaders.AUTHORIZATION, OnboardingConstants.HttpHeaders.BEARER_PREFIX + getAuthToken())
                        .header(OnboardingConstants.HttpHeaders.X_TENANT_ID, tenantId)
                        .header(OnboardingConstants.HttpHeaders.X_CORRELATION_ID, context.getCorrelationId())
                        .header(OnboardingConstants.HttpHeaders.CONTENT_TYPE, OnboardingConstants.ContentTypes.APPLICATION_JSON)
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
                
                // Log step completion with dynamic step number
                stepLoggingUtil.logStepCompletion(enhancedContext, 
                    String.format("Journey launched for entity: %s with schema: %s", entityId, journeySchemaId));
                
                // Log data sharing (this is the final step)
                stepLoggingUtil.logStepDataSharing(enhancedContext, "Journey Launch Info", launchInfo);
                
                return StepResult.success(launchInfo, getStepName(), context.getCorrelationId());
            } else {
                stepLoggingUtil.logStepFailure(enhancedContext, OnboardingConstants.Messages.NO_JOURNEY_RESPONSE);
                return StepResult.failure(OnboardingConstants.Messages.NO_JOURNEY_RESPONSE, getStepName(), context.getCorrelationId());
            }
            
        } catch (Exception e) {
            stepLoggingUtil.logStepFailure(enhancedContext, e.getMessage());
            return StepResult.failure(OnboardingConstants.Messages.JOURNEY_LAUNCH_FAILED + e.getMessage(), getStepName(), context.getCorrelationId());
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
                        "journeyType", OnboardingConstants.JourneyTypes.CLIENT_ONBOARDING,
                        "journeySchemaId", journeySchemaId,
                        "journeySchemaVersionNumber", version,
                        "jurisdictions", new String[]{OnboardingConstants.DefaultValues.DEFAULT_JURISDICTION},
                        "accessLayers", Map.of(
                                OnboardingConstants.AccessLayers.INTERNAL, true,
                                OnboardingConstants.AccessLayers.EXTERNAL, false
                        )
                )
        );
    }
    
    /**
     * Get authentication token (simplified - in real implementation, use proper token service)
     */
    private String getAuthToken() {
        // In real implementation, this would call your token service
        return OnboardingConstants.MockTokens.FENERGO_TOKEN;
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
                .dependencies(new String[]{OnboardingConstants.StepNames.FENERGO_JOURNEY_SCHEMA_EVALUATION})
                .build();
    }
    
    @Override
    public String getStepName() {
        return OnboardingConstants.StepNames.FENERGO_JOURNEY_LAUNCH;
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.hasStepResult(OnboardingConstants.StepNames.FENERGO_JOURNEY_SCHEMA_EVALUATION);
    }
}


