package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Process Step Model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStep {
    
    private String stepId;
    private String stepName;
    private StepStatus status;
    private String inputData;
    private String outputData;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private long durationMs;
    
    public enum StepStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        SKIPPED
    }
    
    public enum StepName {
        STEP_1_XML_TO_JSON_TRANSFORMATION("XML to JSON Transformation"),
        STEP_2_FENERGO_ENTITY_CREATE("Fenergo Entity Creation"),
        STEP_3_FENERGO_JOURNEY_INFO("Fenergo Journey Information"),
        STEP_4_FENERGO_JOURNEY_INITIATE("Fenergo Journey Initiation"),
        STEP_5_FENERGO_JOURNEY_DETAILS("Fenergo Journey Details");
        
        private final String description;
        
        StepName(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
