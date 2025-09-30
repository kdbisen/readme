package com.banking.onboarding.model;

import com.banking.onboarding.model.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response model for process entity initiation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessEntityResponse {
    private String processId;
    private String correlationId;
    private String status;
    private String message;
    private RequestType requestType;
    private Long timestamp;
    private LocalDateTime createdAt;
}

/**
 * Response model for process status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStatusResponse {
    private String processId;
    private String correlationId;
    private String status;
    private String currentStep;
    private String message;
    private Long timestamp;
    private LocalDateTime lastUpdated;
    private List<ProcessStep> completedSteps;
    private ProcessStep currentStepDetails;
    private Map<String, Object> metadata;
}

/**
 * Response model for journey details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyDetailsResponse {
    private String processId;
    private String correlationId;
    private String journeyId;
    private String journeyStatus;
    private String clientId;
    private String entityId;
    private List<JourneyStep> journeySteps;
    private Map<String, Object> journeyData;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
}

/**
 * Process step details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStep {
    private String stepName;
    private String stepStatus;
    private String stepMessage;
    private Long startTime;
    private Long endTime;
    private Long duration;
    private Map<String, Object> stepData;
    private String errorMessage;
    private Integer retryCount;
}

/**
 * Journey step details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyStep {
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
}
