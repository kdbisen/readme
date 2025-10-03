package com.banking.onboarding.step.data;

import com.banking.onboarding.constants.OnboardingConstants;
import com.banking.onboarding.enums.OnboardingEnums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data structures for Fenergo API payloads and responses
 * Replaces hardcoded HashMap usage with proper typed classes
 */
public class FenergoApiData {

    // ===========================================
    // ENTITY CREATION PAYLOAD
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityCreationPayload {
        private EntityData data;
        
        public static EntityCreationPayload createDefault() {
            return EntityCreationPayload.builder()
                    .data(EntityData.createDefault())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityData {
        private String type;
        private String targetEntity;
        private Map<String, EntityProperty> properties;
        private String[] policyJurisdictions;
        
        public static EntityData createDefault() {
            return EntityData.builder()
                    .type(OnboardingConstants.EntityTypes.COMPANY)
                    .targetEntity(OnboardingConstants.EntityTypes.CLIENT)
                    .properties(Map.of(
                            "name", EntityProperty.createSingle(OnboardingConstants.DefaultValues.DEFAULT_ENTITY_NAME),
                            "jurisdiction", EntityProperty.createSingle(OnboardingConstants.DefaultValues.DEFAULT_JURISDICTION),
                            "entityType", EntityProperty.createSingle(OnboardingConstants.DefaultValues.DEFAULT_ENTITY_TYPE)
                    ))
                    .policyJurisdictions(new String[]{OnboardingConstants.DefaultValues.DEFAULT_JURISDICTION})
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityProperty {
        private String type;
        private String value;
        
        public static EntityProperty createSingle(String value) {
            return EntityProperty.builder()
                    .type(OnboardingConstants.PropertyTypes.SINGLE)
                    .value(value)
                    .build();
        }
        
        public static EntityProperty createMultiple(String value) {
            return EntityProperty.builder()
                    .type(OnboardingConstants.PropertyTypes.MULTIPLE)
                    .value(value)
                    .build();
        }
    }
    
    // ===========================================
    // ENTITY CREATION RESPONSE
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityCreationResponse {
        private EntityResponseData data;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityResponseData {
        private String entityId;
        private String status;
        private String message;
    }
    
    // ===========================================
    // JOURNEY SCHEMA EVALUATION PAYLOAD
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneySchemaEvaluationPayload {
        private JourneyEvaluationData data;
        
        public static JourneySchemaEvaluationPayload create(String entityId) {
            return JourneySchemaEvaluationPayload.builder()
                    .data(JourneyEvaluationData.create(entityId))
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneyEvaluationData {
        private String entityId;
        private String journeyType;
        private String[] jurisdictions;
        
        public static JourneyEvaluationData create(String entityId) {
            return JourneyEvaluationData.builder()
                    .entityId(entityId)
                    .journeyType(OnboardingConstants.JourneyTypes.CLIENT_ONBOARDING)
                    .jurisdictions(new String[]{OnboardingConstants.DefaultValues.DEFAULT_JURISDICTION})
                    .build();
        }
    }
    
    // ===========================================
    // JOURNEY SCHEMA EVALUATION RESPONSE
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneySchemaEvaluationResponse {
        private List<JourneySchema> data;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneySchema {
        private String journeySchemaId;
        private Integer journeySchemaVersion;
        private String name;
        private String description;
        private String status;
        private Integer priority;
        private Map<String, Object> metadata;
        
        public static JourneySchema fromMap(Map<String, Object> map) {
            return JourneySchema.builder()
                    .journeySchemaId((String) map.get("journeySchemaId"))
                    .journeySchemaVersion((Integer) map.get("journeySchemaVersion"))
                    .name((String) map.get("name"))
                    .description((String) map.get("description"))
                    .status((String) map.get("status"))
                    .priority((Integer) map.getOrDefault("priority", 0))
                    .metadata(map)
                    .build();
        }
    }
    
    // ===========================================
    // JOURNEY LAUNCH PAYLOAD
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneyLaunchPayload {
        private JourneyLaunchData data;
        
        public static JourneyLaunchPayload create(String entityId, String journeySchemaId, Integer version) {
            return JourneyLaunchPayload.builder()
                    .data(JourneyLaunchData.create(entityId, journeySchemaId, version))
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneyLaunchData {
        private String entityId;
        private String journeyType;
        private String journeySchemaId;
        private Integer journeySchemaVersionNumber;
        private String[] jurisdictions;
        private AccessLayers accessLayers;
        
        public static JourneyLaunchData create(String entityId, String journeySchemaId, Integer version) {
            return JourneyLaunchData.builder()
                    .entityId(entityId)
                    .journeyType(OnboardingConstants.JourneyTypes.CLIENT_ONBOARDING)
                    .journeySchemaId(journeySchemaId)
                    .journeySchemaVersionNumber(version)
                    .jurisdictions(new String[]{OnboardingConstants.DefaultValues.DEFAULT_JURISDICTION})
                    .accessLayers(AccessLayers.createDefault())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessLayers {
        private boolean internal;
        private boolean external;
        
        public static AccessLayers createDefault() {
            return AccessLayers.builder()
                    .internal(true)
                    .external(false)
                    .build();
        }
    }
    
    // ===========================================
    // JOURNEY LAUNCH RESPONSE
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneyLaunchResponse {
        private String journeyInstanceId;
        private String status;
        private String message;
        private Map<String, Object> metadata;
    }
    
    // ===========================================
    // STEP RESULT DATA STRUCTURES
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityCreationResult {
        private String entityId;
        private String status;
        private String message;
        private Map<String, Object> metadata;
        
        public static EntityCreationResult create(String entityId) {
            return EntityCreationResult.builder()
                    .entityId(entityId)
                    .status("CREATED")
                    .message("Entity created successfully")
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneySchemaEvaluationResult {
        private String entityId;
        private String journeySchemaId;
        private Integer journeySchemaVersion;
        private String schemaName;
        private String status;
        private Map<String, Object> evaluationResponse;
        
        public static JourneySchemaEvaluationResult create(String entityId, String journeySchemaId, Integer version) {
            return JourneySchemaEvaluationResult.builder()
                    .entityId(entityId)
                    .journeySchemaId(journeySchemaId)
                    .journeySchemaVersion(version)
                    .status("EVALUATED")
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneyLaunchResult {
        private String entityId;
        private String journeySchemaId;
        private Integer journeySchemaVersion;
        private String journeyInstanceId;
        private String status;
        private Map<String, Object> launchResponse;
        
        public static JourneyLaunchResult create(String entityId, String journeySchemaId, Integer version, String journeyInstanceId) {
            return JourneyLaunchResult.builder()
                    .entityId(entityId)
                    .journeySchemaId(journeySchemaId)
                    .journeySchemaVersion(version)
                    .journeyInstanceId(journeyInstanceId)
                    .status("LAUNCHED")
                    .build();
        }
    }
}



