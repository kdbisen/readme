package com.banking.onboarding.step.impl;

import com.banking.onboarding.constants.OnboardingConstants;
import com.banking.onboarding.enums.OnboardingEnums;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import com.banking.onboarding.step.builder.ApiPayloadBuilder;
import com.banking.onboarding.step.data.FenergoApiData;
import com.banking.onboarding.step.enhancer.StepContextEnhancer;
import com.banking.onboarding.step.manager.StepResultDataManager;
import com.banking.onboarding.step.util.DynamicStepLoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fenergo Entity Creation Step - Dynamic Step Numbering
 * Creates entity in Fenergo system following exact API patterns
 * Uses dynamic step numbering instead of hardcoded step numbers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FenergoEntityCreationStep implements GenericStepExecutor {
    
    private final RestClient restClient;
    private final DynamicStepLoggingUtil stepLoggingUtil;
    private final StepContextEnhancer stepContextEnhancer;
    private final ApiPayloadBuilder payloadBuilder;
    private final StepResultDataManager resultDataManager;
    
    @Value("${fenergo.entity.api.url:https://fenergo.example.com/entity}")
    private String entityApiUrl;
    
    @Value("${fenergo.tenant.id:default-tenant}")
    private String tenantId;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Enhance context with dynamic step numbering
        StepContextEnhancer.EnhancedStepContext enhancedContext = 
            stepContextEnhancer.enhanceContext(context, getStepName());
        
        // Log step start with dynamic step number
        stepLoggingUtil.logStepStart(enhancedContext, "Create Entity via Fenergo Entity API");
        
        // Get JSON data from previous step
        Object jsonData = getInputData(context);
        if (jsonData == null) {
            stepLoggingUtil.logStepFailure(enhancedContext, OnboardingConstants.Messages.NO_JSON_DATA_AVAILABLE);
            return StepResult.failure(OnboardingConstants.Messages.NO_JSON_DATA_AVAILABLE, getStepName(), context.getCorrelationId());
        }
        
        try {
            // Build entity creation payload using proper data structures
            FenergoApiData.EntityCreationPayload entityPayload = payloadBuilder.buildEntityCreationPayload(jsonData, context.getCorrelationId());
            
            // Call Fenergo Entity API synchronously
            CompletableFuture<Map> future = CompletableFuture.supplyAsync(() -> {
                return restClient.post()
                        .uri(entityApiUrl)
                        .header(OnboardingConstants.HttpHeaders.AUTHORIZATION, OnboardingConstants.HttpHeaders.BEARER_PREFIX + getAuthToken())
                        .header(OnboardingConstants.HttpHeaders.X_TENANT_ID, tenantId)
                        .header(OnboardingConstants.HttpHeaders.X_CORRELATION_ID, context.getCorrelationId())
                        .header(OnboardingConstants.HttpHeaders.CONTENT_TYPE, OnboardingConstants.ContentTypes.APPLICATION_JSON)
                        .body(entityPayload)
                        .retrieve()
                        .body(Map.class);
            });
            
            Map response = future.get(); // Block here
            
            // Parse response using proper data structures
            FenergoApiData.EntityCreationResponse entityResponse = payloadBuilder.parseEntityCreationResponse(response);
            
            if (entityResponse != null && entityResponse.getData() != null) {
                String entityId = entityResponse.getData().getEntityId();
                
                if (entityId != null) {
                    // Store entity creation result using proper data structures
                    resultDataManager.storeEntityCreationResult(context, entityId);
                    
                    // Log step completion with dynamic step number
                    stepLoggingUtil.logStepCompletion(enhancedContext, 
                        String.format("Entity created with ID: %s", entityId));
                    
                    // Log data sharing for next step
                    stepLoggingUtil.logStepDataSharing(enhancedContext, "Entity ID", entityId);
                    
                    return StepResult.success(entityId, getStepName(), context.getCorrelationId());
                } else {
                    stepLoggingUtil.logStepFailure(enhancedContext, OnboardingConstants.Messages.ENTITY_CREATION_NO_ID);
                    return StepResult.failure(OnboardingConstants.Messages.ENTITY_CREATION_NO_ID, getStepName(), context.getCorrelationId());
                }
            } else {
                stepLoggingUtil.logStepFailure(enhancedContext, OnboardingConstants.Messages.INVALID_FENERGO_RESPONSE);
                return StepResult.failure(OnboardingConstants.Messages.INVALID_FENERGO_RESPONSE, getStepName(), context.getCorrelationId());
            }
            
        } catch (Exception e) {
            stepLoggingUtil.logStepFailure(enhancedContext, e.getMessage());
            return StepResult.failure(OnboardingConstants.Messages.ENTITY_CREATION_FAILED + e.getMessage(), getStepName(), context.getCorrelationId());
        }
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
                .description("Create entity in Fenergo system via Entity API")
                .dependencies(new String[]{OnboardingConstants.StepNames.XML_TO_JSON_TRANSFORMATION})
                .build();
    }
    
    @Override
    public String getStepName() {
        return OnboardingConstants.StepNames.FENERGO_ENTITY_CREATION;
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.hasStepResult(OnboardingConstants.StepNames.XML_TO_JSON_TRANSFORMATION) || context.getInputData() != null;
    }
}


