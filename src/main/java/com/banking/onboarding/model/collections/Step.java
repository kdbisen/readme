package com.banking.onboarding.model.collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Step Collection - Individual step execution details
 * Collection: steps
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "steps")
@CompoundIndex(def = "{'processId': 1, 'stepOrder': 1}")
@CompoundIndex(def = "{'stepName': 1, 'status': 1}")
@CompoundIndex(def = "{'correlationId': 1, 'stepName': 1}")
public class Step {
    
    @Id
    private String id;
    
    @Indexed
    private String processId;
    
    @Indexed
    private String correlationId;
    
    private String stepId;
    private String stepName;
    private int stepOrder;
    private StepStatus status;
    
    // Step execution details
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private long durationMs;
    
    // Step configuration
    private Map<String, Object> stepConfig;
    private String[] dependencies;
    private int priority;
    private String description;
    
    // Step results
    private String errorMessage;
    private String exceptionClass;
    private String stackTrace;
    
    // Step metadata
    private String executorClass;
    private String executionMode; // SYNC, ASYNC, RETRY
    private int retryCount;
    private int maxRetries;
    
    // Performance metrics
    private long memoryUsedBytes;
    private long cpuTimeMs;
    private String executionEnvironment;
    
    public enum StepStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED, RETRYING, TIMEOUT
    }
}
