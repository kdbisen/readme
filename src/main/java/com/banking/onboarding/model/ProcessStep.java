package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStep {
    private StepName stepName;
    private StepStatus stepStatus;
    private String stepMessage;
    private ProcessStepTiming timing;
    private Map<String, Object> outputData;
    private ProcessStepErrorInfo errorInfo;
    private ProcessStepRetryInfo retryInfo;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessStepTiming {
        private Long startTime;
        private Long endTime;
        private Long duration;
        private Long queueTime;
        private Long processingTime;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessStepErrorInfo {
        private Boolean hasError;
        private String errorCode;
        private String errorMessage;
        private String errorType;
        private LocalDateTime errorTimestamp;
        private String stackTrace;
        private Map<String, Object> errorContext;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessStepRetryInfo {
        private Integer maxRetries;
        private Integer currentRetry;
        private String retryReason;
        private Long retryDelay;
        private java.util.List<RetryAttempt> retryHistory;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryAttempt {
        private Integer attemptNumber;
        private LocalDateTime attemptTime;
        private String attemptReason;
        private String attemptResult;
    }
}
