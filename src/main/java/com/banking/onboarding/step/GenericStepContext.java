package com.banking.onboarding.step;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic Step Context - Contains all data needed for step execution
 * Supports any data type through generic Object storage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericStepContext {
    
    private String correlationId;
    private String processId;
    private Object inputData;
    private Map<String, Object> stepResults;
    private Map<String, Object> metadata;
    private LocalDateTime startedAt;
    
    /**
     * Get result from any previous step by name
     */
    public Object getStepResult(String stepName) {
        return stepResults != null ? stepResults.get(stepName) : null;
    }
    
    /**
     * Get result from previous step with type casting
     */
    @SuppressWarnings("unchecked")
    public <T> T getStepResult(String stepName, Class<T> resultType) {
        Object result = getStepResult(stepName);
        if (result != null && resultType.isAssignableFrom(result.getClass())) {
            return (T) result;
        }
        return null;
    }
    
    /**
     * Add result from current step
     */
    public void addStepResult(String stepName, Object result) {
        if (stepResults == null) {
            stepResults = new HashMap<>();
        }
        stepResults.put(stepName, result);
    }
    
    /**
     * Get metadata value
     */
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
    
    /**
     * Set metadata value
     */
    public void setMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
    
    /**
     * Check if step result exists
     */
    public boolean hasStepResult(String stepName) {
        return stepResults != null && stepResults.containsKey(stepName);
    }
    
    /**
     * Get all step results
     */
    public Map<String, Object> getAllStepResults() {
        return stepResults != null ? new HashMap<>(stepResults) : new HashMap<>();
    }
    
    /**
     * Create context from initial input
     */
    public static GenericStepContext create(String correlationId, String processId, Object inputData) {
        return GenericStepContext.builder()
                .correlationId(correlationId)
                .processId(processId)
                .inputData(inputData)
                .stepResults(new HashMap<>())
                .metadata(new HashMap<>())
                .startedAt(LocalDateTime.now())
                .build();
    }
}
