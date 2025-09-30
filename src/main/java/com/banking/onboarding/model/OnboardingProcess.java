package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "onboarding_processes")
public class OnboardingProcess {
    
    @Id
    private String id;
    
    @Field("process_id")
    private String processId;
    
    @Field("correlation_id")
    private String correlationId;
    
    @Field("request_type")
    private RequestType requestType;
    
    @Field("status")
    private ProcessStatus status;
    
    @Field("original_xml")
    private String originalXml;
    
    @Field("transformed_json")
    private Map<String, Object> transformedJson;
    
    @Field("entity_data")
    private EntityData entityData;
    
    @Field("fenergo_response")
    private Map<String, Object> fenergoResponse;
    
    @Field("error_message")
    private String errorMessage;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("completed_at")
    private LocalDateTime completedAt;
    
    public enum ProcessStatus {
        RECEIVED,
        TRANSFORMING,
        VALIDATING,
        PROCESSING_FENERGO,
        COMPLETED,
        FAILED
    }
}
