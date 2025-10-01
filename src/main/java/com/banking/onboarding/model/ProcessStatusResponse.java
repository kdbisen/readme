package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private List<StepInfo> steps;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepInfo {
        private String stepName;
        private int stepOrder;
        private String status;
        private String message;
        private long durationMs;
        private LocalDateTime completedAt;
    }
}