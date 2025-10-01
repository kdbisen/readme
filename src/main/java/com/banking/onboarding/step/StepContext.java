package com.banking.onboarding.step;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Step Context - Contains all data needed for step execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepContext<T> {
    
    private String correlationId;
    private String processId;
    private T inputData;
    private Map<String, Object> metadata;
    private LocalDateTime startedAt;
    private Map<String, Object> previousResults;
    
    /**
     * Get result from previous step
     */
    @SuppressWarnings("unchecked")
    public <R> R getPreviousResult(String stepName, Class<R> resultType) {
        Object result = previousResults.get(stepName);
        if (result != null && resultType.isAssignableFrom(result.getClass())) {
            return (R) result;
        }
        return null;
    }
    
    /**
     * Add result to context
     */
    public void addResult(String stepName, Object result) {
        if (previousResults != null) {
            previousResults.put(stepName, result);
        }
    }
}
