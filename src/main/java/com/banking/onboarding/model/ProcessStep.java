package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

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
    
    // Enhanced payload and response tracking - COMPLETE DATA AS-IS
    private String inputPayloadType;      // XML, JSON, etc.
    private String outputResponseType;    // JSON, XML, etc.
    private long inputPayloadSize;        // Size in bytes - ACCURATE SIZE
    private long outputResponseSize;      // Size in bytes - ACCURATE SIZE
    private Map<String, Object> additionalContext; // Additional step context
    private String stackTrace;            // Stack trace if failed
    private String exceptionClass;        // Exception class if failed
    
    // Complete payload storage - NO TRUNCATION
    private Object inputPayloadComplete;     // COMPLETE INPUT - NO TRUNCATION
    private Object outputResponseComplete;   // COMPLETE OUTPUT - NO TRUNCATION
    private String inputPayloadRawString;    // Raw string - complete, untruncated
    private String outputResponseRawString;  // Raw string - complete, untruncated
    private byte[] inputPayloadRawBytes;     // Raw bytes for binary data
    private byte[] outputResponseRawBytes;   // Raw bytes for binary data
    
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
