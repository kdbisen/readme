package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Main process tracking document
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fenergo_processes")
public class FenergoProcess {
    
    @Id
    private String processId;
    private String correlationId;
    private String status;
    private String currentStep;
    private String message;
    private RequestType requestType;
    private String originalPayload;
    private Map<String, Object> metadata;
    private List<ProcessStep> completedSteps;
    private ProcessStep currentStepDetails;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private String errorMessage;
    private Integer retryCount;
    private Boolean isCompleted;
    private Boolean hasError;
}

/**
 * Individual step tracking document
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fenergo_process_steps")
public class FenergoProcessStep {
    
    @Id
    private String stepId;
    private String processId;
    private String correlationId;
    private String stepName;
    private String stepStatus;
    private String stepMessage;
    private Long startTime;
    private Long endTime;
    private Long duration;
    private Map<String, Object> stepData;
    private String errorMessage;
    private Integer retryCount;
    private String fenergoResponse;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
}

/**
 * Journey tracking document
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fenergo_journeys")
public class FenergoJourney {
    
    @Id
    private String journeyId;
    private String processId;
    private String correlationId;
    private String clientId;
    private String entityId;
    private String journeyStatus;
    private List<JourneyStep> journeySteps;
    private Map<String, Object> journeyData;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private String errorMessage;
    private Boolean isCompleted;
    private Boolean hasError;
}

/**
 * Journey step tracking document
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fenergo_journey_steps")
public class FenergoJourneyStep {
    
    @Id
    private String stepId;
    private String journeyId;
    private String processId;
    private String correlationId;
    private String stepName;
    private String stepStatus;
    private String stepMessage;
    private Long startTime;
    private Long endTime;
    private Long duration;
    private Map<String, Object> stepData;
    private String errorMessage;
    private Integer retryCount;
    private String fenergoResponse;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
}

/**
 * Step names enum
 */
public enum StepName {
    APIGEE_TRANSFORMATION("APIGEE_TRANSFORMATION"),
    FENERGO_ENTITY_CREATE("FENERGO_ENTITY_CREATE"),
    FENERGO_JOURNEY_INFO("FENERGO_JOURNEY_INFO"),
    FENERGO_JOURNEY_INITIATE("FENERGO_JOURNEY_INITIATE"),
    FENERGO_JOURNEY_DETAILS("FENERGO_JOURNEY_DETAILS"),
    COMPLETION("COMPLETION");
    
    private final String value;
    
    StepName(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}

/**
 * Step status enum
 */
public enum StepStatus {
    PENDING("PENDING"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    RETRYING("RETRYING"),
    SKIPPED("SKIPPED");
    
    private final String value;
    
    StepStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}

/**
 * Process status enum
 */
public enum ProcessStatus {
    RECEIVED("RECEIVED"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");
    
    private final String value;
    
    ProcessStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
