package com.banking.onboarding.step.builder;

import com.banking.onboarding.constants.OnboardingConstants;
import com.banking.onboarding.step.data.FenergoApiData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Payload Builders for API requests
 * Replaces hardcoded Map.of() calls with proper builder pattern
 */
@Slf4j
@Component
public class ApiPayloadBuilder {
    
    // ===========================================
    // ENTITY CREATION PAYLOAD BUILDER
    // ===========================================
    
    public FenergoApiData.EntityCreationPayload buildEntityCreationPayload(Object jsonData, String correlationId) {
        log.debug("[CORRELATION:{}] Building entity creation payload", correlationId);
        
        // In a real implementation, you would parse the JSON data and extract entity information
        // For now, we'll use default values
        return FenergoApiData.EntityCreationPayload.createDefault();
    }
    
    public FenergoApiData.EntityCreationPayload buildEntityCreationPayload(String entityName, String jurisdiction, String entityType) {
        FenergoApiData.EntityData entityData = FenergoApiData.EntityData.builder()
                .type(entityType)
                .targetEntity(OnboardingConstants.EntityTypes.CLIENT)
                .properties(java.util.Map.of(
                        "name", FenergoApiData.EntityProperty.createSingle(entityName),
                        "jurisdiction", FenergoApiData.EntityProperty.createSingle(jurisdiction),
                        "entityType", FenergoApiData.EntityProperty.createSingle(entityType)
                ))
                .policyJurisdictions(new String[]{jurisdiction})
                .build();
        
        return FenergoApiData.EntityCreationPayload.builder()
                .data(entityData)
                .build();
    }
    
    // ===========================================
    // JOURNEY SCHEMA EVALUATION PAYLOAD BUILDER
    // ===========================================
    
    public FenergoApiData.JourneySchemaEvaluationPayload buildJourneySchemaEvaluationPayload(String entityId, String correlationId) {
        log.debug("[CORRELATION:{}] Building journey schema evaluation payload for entity: {}", correlationId, entityId);
        
        return FenergoApiData.JourneySchemaEvaluationPayload.create(entityId);
    }
    
    public FenergoApiData.JourneySchemaEvaluationPayload buildJourneySchemaEvaluationPayload(String entityId, String journeyType, String[] jurisdictions) {
        FenergoApiData.JourneyEvaluationData evaluationData = FenergoApiData.JourneyEvaluationData.builder()
                .entityId(entityId)
                .journeyType(journeyType)
                .jurisdictions(jurisdictions)
                .build();
        
        return FenergoApiData.JourneySchemaEvaluationPayload.builder()
                .data(evaluationData)
                .build();
    }
    
    // ===========================================
    // JOURNEY LAUNCH PAYLOAD BUILDER
    // ===========================================
    
    public FenergoApiData.JourneyLaunchPayload buildJourneyLaunchPayload(String entityId, String journeySchemaId, Integer version, String correlationId) {
        log.debug("[CORRELATION:{}] Building journey launch payload for entity: {}, schema: {}", correlationId, entityId, journeySchemaId);
        
        return FenergoApiData.JourneyLaunchPayload.create(entityId, journeySchemaId, version);
    }
    
    public FenergoApiData.JourneyLaunchPayload buildJourneyLaunchPayload(String entityId, String journeySchemaId, Integer version, String[] jurisdictions, boolean internal, boolean external) {
        FenergoApiData.AccessLayers accessLayers = FenergoApiData.AccessLayers.builder()
                .internal(internal)
                .external(external)
                .build();
        
        FenergoApiData.JourneyLaunchData launchData = FenergoApiData.JourneyLaunchData.builder()
                .entityId(entityId)
                .journeyType(OnboardingConstants.JourneyTypes.CLIENT_ONBOARDING)
                .journeySchemaId(journeySchemaId)
                .journeySchemaVersionNumber(version)
                .jurisdictions(jurisdictions)
                .accessLayers(accessLayers)
                .build();
        
        return FenergoApiData.JourneyLaunchPayload.builder()
                .data(launchData)
                .build();
    }
    
    // ===========================================
    // RESPONSE PARSER HELPERS
    // ===========================================
    
    public FenergoApiData.EntityCreationResponse parseEntityCreationResponse(java.util.Map<String, Object> response) {
        if (response == null || !response.containsKey("data")) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> data = (java.util.Map<String, Object>) response.get("data");
        
        return FenergoApiData.EntityCreationResponse.builder()
                .data(FenergoApiData.EntityResponseData.builder()
                        .entityId((String) data.get("entityId"))
                        .status((String) data.get("status"))
                        .message((String) data.get("message"))
                        .build())
                .build();
    }
    
    public FenergoApiData.JourneySchemaEvaluationResponse parseJourneySchemaEvaluationResponse(java.util.Map<String, Object> response) {
        if (response == null || !response.containsKey("data")) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> data = (java.util.List<java.util.Map<String, Object>>) response.get("data");
        
        java.util.List<FenergoApiData.JourneySchema> schemas = data.stream()
                .map(FenergoApiData.JourneySchema::fromMap)
                .toList();
        
        return FenergoApiData.JourneySchemaEvaluationResponse.builder()
                .data(schemas)
                .build();
    }
    
    public FenergoApiData.JourneyLaunchResponse parseJourneyLaunchResponse(java.util.Map<String, Object> response) {
        if (response == null) {
            return null;
        }
        
        return FenergoApiData.JourneyLaunchResponse.builder()
                .journeyInstanceId((String) response.get("journeyInstanceId"))
                .status((String) response.get("status"))
                .message((String) response.get("message"))
                .metadata(response)
                .build();
    }
    
    // ===========================================
    // STEP RESULT BUILDERS
    // ===========================================
    
    public FenergoApiData.EntityCreationResult buildEntityCreationResult(String entityId, String correlationId) {
        log.debug("[CORRELATION:{}] Building entity creation result for entity: {}", correlationId, entityId);
        
        return FenergoApiData.EntityCreationResult.create(entityId);
    }
    
    public FenergoApiData.JourneySchemaEvaluationResult buildJourneySchemaEvaluationResult(String entityId, String journeySchemaId, Integer version, String correlationId) {
        log.debug("[CORRELATION:{}] Building journey schema evaluation result for entity: {}, schema: {}", correlationId, entityId, journeySchemaId);
        
        return FenergoApiData.JourneySchemaEvaluationResult.create(entityId, journeySchemaId, version);
    }
    
    public FenergoApiData.JourneyLaunchResult buildJourneyLaunchResult(String entityId, String journeySchemaId, Integer version, String journeyInstanceId, String correlationId) {
        log.debug("[CORRELATION:{}] Building journey launch result for entity: {}, instance: {}", correlationId, entityId, journeyInstanceId);
        
        return FenergoApiData.JourneyLaunchResult.create(entityId, journeySchemaId, version, journeyInstanceId);
    }
}


