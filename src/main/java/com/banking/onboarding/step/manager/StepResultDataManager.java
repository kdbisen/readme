package com.banking.onboarding.step.manager;

import com.banking.onboarding.constants.OnboardingConstants;
import com.banking.onboarding.step.config.StepConfigurationData;
import com.banking.onboarding.step.data.FenergoApiData;
import com.banking.onboarding.step.GenericStepContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Step Result Data Manager
 * Manages step results with proper typed data structures instead of raw HashMaps
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StepResultDataManager {
    
    // ===========================================
    // STEP RESULT STORAGE AND RETRIEVAL
    // ===========================================
    
    /**
     * Store entity creation result
     */
    public void storeEntityCreationResult(GenericStepContext context, String entityId) {
        FenergoApiData.EntityCreationResult result = FenergoApiData.EntityCreationResult.create(entityId);
        context.addStepResult(OnboardingConstants.StepNames.FENERGO_ENTITY_CREATION, result);
        
        log.debug("[CORRELATION:{}] Stored entity creation result: {}", context.getCorrelationId(), entityId);
    }
    
    /**
     * Get entity creation result
     */
    public Optional<String> getEntityId(GenericStepContext context) {
        Object result = context.getStepResult(OnboardingConstants.StepNames.FENERGO_ENTITY_CREATION);
        
        if (result instanceof FenergoApiData.EntityCreationResult entityResult) {
            return Optional.of(entityResult.getEntityId());
        } else if (result instanceof String entityId) {
            // Backward compatibility
            return Optional.of(entityId);
        }
        
        return Optional.empty();
    }
    
    /**
     * Store journey schema evaluation result
     */
    public void storeJourneySchemaEvaluationResult(GenericStepContext context, String entityId, String journeySchemaId, Integer version) {
        FenergoApiData.JourneySchemaEvaluationResult result = FenergoApiData.JourneySchemaEvaluationResult.create(entityId, journeySchemaId, version);
        context.addStepResult(OnboardingConstants.StepNames.FENERGO_JOURNEY_SCHEMA_EVALUATION, result);
        
        log.debug("[CORRELATION:{}] Stored journey schema evaluation result: entity={}, schema={}, version={}", 
                context.getCorrelationId(), entityId, journeySchemaId, version);
    }
    
    /**
     * Get journey schema evaluation result
     */
    public Optional<FenergoApiData.JourneySchemaEvaluationResult> getJourneySchemaEvaluationResult(GenericStepContext context) {
        Object result = context.getStepResult(OnboardingConstants.StepNames.FENERGO_JOURNEY_SCHEMA_EVALUATION);
        
        if (result instanceof FenergoApiData.JourneySchemaEvaluationResult schemaResult) {
            return Optional.of(schemaResult);
        } else if (result instanceof Map<?, ?> map) {
            // Backward compatibility - convert Map to typed result
            return convertMapToJourneySchemaResult(map);
        }
        
        return Optional.empty();
    }
    
    /**
     * Store journey launch result
     */
    public void storeJourneyLaunchResult(GenericStepContext context, String entityId, String journeySchemaId, Integer version, String journeyInstanceId) {
        FenergoApiData.JourneyLaunchResult result = FenergoApiData.JourneyLaunchResult.create(entityId, journeySchemaId, version, journeyInstanceId);
        context.addStepResult(OnboardingConstants.StepNames.FENERGO_JOURNEY_LAUNCH, result);
        
        log.debug("[CORRELATION:{}] Stored journey launch result: entity={}, instance={}", 
                context.getCorrelationId(), entityId, journeyInstanceId);
    }
    
    /**
     * Get journey launch result
     */
    public Optional<FenergoApiData.JourneyLaunchResult> getJourneyLaunchResult(GenericStepContext context) {
        Object result = context.getStepResult(OnboardingConstants.StepNames.FENERGO_JOURNEY_LAUNCH);
        
        if (result instanceof FenergoApiData.JourneyLaunchResult launchResult) {
            return Optional.of(launchResult);
        } else if (result instanceof Map<?, ?> map) {
            // Backward compatibility - convert Map to typed result
            return convertMapToJourneyLaunchResult(map);
        }
        
        return Optional.empty();
    }
    
    // ===========================================
    // STEP METADATA MANAGEMENT
    // ===========================================
    
    /**
     * Store step metadata
     */
    public void storeStepMetadata(GenericStepContext context, StepConfigurationData.StepMetadata metadata) {
        context.setMetadata("stepMetadata", metadata);
        
        log.debug("[CORRELATION:{}] Stored step metadata: step={}, number={}", 
                context.getCorrelationId(), metadata.getStepNumberText(), metadata.getStepNumber());
    }
    
    /**
     * Get step metadata
     */
    public Optional<StepConfigurationData.StepMetadata> getStepMetadata(GenericStepContext context) {
        Object metadata = context.getMetadata("stepMetadata");
        
        if (metadata instanceof StepConfigurationData.StepMetadata stepMetadata) {
            return Optional.of(stepMetadata);
        }
        
        return Optional.empty();
    }
    
    /**
     * Store step execution result
     */
    public void storeStepExecutionResult(GenericStepContext context, String stepName, StepConfigurationData.StepExecutionResult result) {
        context.setMetadata("stepExecutionResult_" + stepName, result);
        
        log.debug("[CORRELATION:{}] Stored step execution result: step={}, success={}", 
                context.getCorrelationId(), stepName, result.getSuccess());
    }
    
    /**
     * Get step execution result
     */
    public Optional<StepConfigurationData.StepExecutionResult> getStepExecutionResult(GenericStepContext context, String stepName) {
        Object result = context.getMetadata("stepExecutionResult_" + stepName);
        
        if (result instanceof StepConfigurationData.StepExecutionResult executionResult) {
            return Optional.of(executionResult);
        }
        
        return Optional.empty();
    }
    
    // ===========================================
    // BACKWARD COMPATIBILITY HELPERS
    // ===========================================
    
    /**
     * Convert Map to JourneySchemaEvaluationResult (backward compatibility)
     */
    @SuppressWarnings("unchecked")
    private Optional<FenergoApiData.JourneySchemaEvaluationResult> convertMapToJourneySchemaResult(Map<?, ?> map) {
        try {
            Map<String, Object> stringMap = (Map<String, Object>) map;
            
            return Optional.of(FenergoApiData.JourneySchemaEvaluationResult.builder()
                    .entityId((String) stringMap.get("entityId"))
                    .journeySchemaId((String) stringMap.get("journeySchemaId"))
                    .journeySchemaVersion((Integer) stringMap.get("journeySchemaVersion"))
                    .schemaName((String) stringMap.get("schemaName"))
                    .status((String) stringMap.get("status"))
                    .evaluationResponse(stringMap)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to convert Map to JourneySchemaEvaluationResult: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Convert Map to JourneyLaunchResult (backward compatibility)
     */
    @SuppressWarnings("unchecked")
    private Optional<FenergoApiData.JourneyLaunchResult> convertMapToJourneyLaunchResult(Map<?, ?> map) {
        try {
            Map<String, Object> stringMap = (Map<String, Object>) map;
            
            return Optional.of(FenergoApiData.JourneyLaunchResult.builder()
                    .entityId((String) stringMap.get("entityId"))
                    .journeySchemaId((String) stringMap.get("journeySchemaId"))
                    .journeySchemaVersion((Integer) stringMap.get("journeySchemaVersion"))
                    .journeyInstanceId((String) stringMap.get("journeyInstanceId"))
                    .status((String) stringMap.get("status"))
                    .launchResponse(stringMap)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to convert Map to JourneyLaunchResult: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    // ===========================================
    // UTILITY METHODS
    // ===========================================
    
    /**
     * Check if step result exists
     */
    public boolean hasStepResult(GenericStepContext context, String stepName) {
        return context.hasStepResult(stepName);
    }
    
    /**
     * Get all step results as typed data
     */
    public Map<String, Object> getAllStepResults(GenericStepContext context) {
        return context.getAllStepResults();
    }
    
    /**
     * Clear step result
     */
    public void clearStepResult(GenericStepContext context, String stepName) {
        if (context.getStepResults() != null) {
            context.getStepResults().remove(stepName);
        }
        
        log.debug("[CORRELATION:{}] Cleared step result: {}", context.getCorrelationId(), stepName);
    }
    
    /**
     * Clear all step results
     */
    public void clearAllStepResults(GenericStepContext context) {
        if (context.getStepResults() != null) {
            context.getStepResults().clear();
        }
        
        log.debug("[CORRELATION:{}] Cleared all step results", context.getCorrelationId());
    }
}



