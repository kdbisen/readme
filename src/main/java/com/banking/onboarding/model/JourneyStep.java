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
public class JourneyStep {
    private String stepName;
    private String stepStatus;
    private String stepMessage;
    private JourneyStepTiming timing;
    private Map<String, Object> outputData;
    private Map<String, Object> fenergoResponse;
    private JourneyStepErrorInfo errorInfo;
    private JourneyStepRetryInfo retryInfo;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneyStepTiming {
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
    public static class JourneyStepErrorInfo {
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
    public static class JourneyStepRetryInfo {
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
