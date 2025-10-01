package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Simplified Step Model - Individual step tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "steps")
public class Step {
    
    @Id
    private String id;
    
    @Indexed
    @Field("process_id")
    private String processId;
    
    @Indexed
    @Field("correlation_id")
    private String correlationId;
    
    @Field("step_name")
    private String stepName; // APIGEE_TRANSFORM, ENTITY_CREATE, JOURNEY_INFO, etc.
    
    @Field("step_order")
    private int stepOrder; // 1, 2, 3, 4, 5
    
    @Field("status")
    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED
    
    @Field("message")
    private String message;
    
    @Field("error_message")
    private String errorMessage;
    
    @Field("input_data")
    private String inputData;
    
    @Field("output_data")
    private String outputData;
    
    @Field("step_data")
    private Map<String, Object> stepData;
    
    @Field("duration_ms")
    private long durationMs;
    
    @Field("retry_count")
    private int retryCount;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("completed_at")
    private LocalDateTime completedAt;
}
