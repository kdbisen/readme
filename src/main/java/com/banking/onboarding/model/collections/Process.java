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
 * Process Collection - Core process information
 * Collection: processes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "processes")
@CompoundIndex(def = "{'correlationId': 1, 'requestType': 1}")
@CompoundIndex(def = "{'status': 1, 'createdAt': 1}")
public class Process {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String processId;
    
    @Indexed
    private String correlationId;
    
    @Indexed
    private String requestType;
    
    private ProcessStatus status;
    private String errorMessage;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    
    // Process metadata
    private String tenantId;
    private String userId;
    private String clientId;
    private String applicationId;
    
    // Process statistics
    private long totalExecutionTimeMs;
    private int totalStepsExecuted;
    private int successfulSteps;
    private int failedSteps;
    
    // Process configuration
    private Map<String, Object> processConfig;
    private String priority;
    private String businessUnit;
    
    public enum ProcessStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED, RETRYING
    }
}
