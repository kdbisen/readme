package com.banking.onboarding.step;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Step Result - Contains the result of step execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepResult<T> {
    
    private boolean success;
    private T data;
    private String errorMessage;
    private String stepName;
    private String correlationId;
    private LocalDateTime completedAt;
    private long durationMs;
    private Map<String, Object> metadata;
    
    /**
     * Create success result
     */
    public static <T> StepResult<T> success(T data, String stepName, String correlationId) {
        return StepResult.<T>builder()
                .success(true)
                .data(data)
                .stepName(stepName)
                .correlationId(correlationId)
                .completedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Create failure result
     */
    public static <T> StepResult<T> failure(String errorMessage, String stepName, String correlationId) {
        return StepResult.<T>builder()
                .success(false)
                .errorMessage(errorMessage)
                .stepName(stepName)
                .correlationId(correlationId)
                .completedAt(LocalDateTime.now())
                .build();
    }
}
