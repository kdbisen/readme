package com.banking.onboarding.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
public class FenergoEntityService {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FenergoEntityResponse {
        private boolean success;
        private String entityId;
        private String clientId;
        private String errorMessage;
        private long duration;
        private Map<String, Object> entityData;
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getEntityId() {
            return entityId;
        }
        
        public String getClientId() {
            return clientId;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public Map<String, Object> getEntityData() {
            return entityData;
        }
    }
    
    public FenergoEntityResponse createEntity(String jsonPayload, String processId) {
        // Mock implementation - replace with actual Fenergo API call
        return FenergoEntityResponse.builder()
                .success(true)
                .entityId("ENTITY-" + System.currentTimeMillis())
                .clientId("CLIENT-" + System.currentTimeMillis())
                .errorMessage(null)
                .duration(2000L)
                .entityData(Map.of("processId", processId, "payload", jsonPayload))
                .build();
    }
}