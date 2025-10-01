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
 * Simplified Log Model - Error and audit logs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "logs")
public class Log {
    
    @Id
    private String id;
    
    @Indexed
    @Field("process_id")
    private String processId;
    
    @Indexed
    @Field("correlation_id")
    private String correlationId;
    
    @Field("log_type")
    private String logType; // ERROR, INFO, AUDIT, API_CALL
    
    @Field("level")
    private String level; // ERROR, WARN, INFO, DEBUG
    
    @Field("message")
    private String message;
    
    @Field("error_details")
    private String errorDetails;
    
    @Field("stack_trace")
    private String stackTrace;
    
    @Field("context_data")
    private Map<String, Object> contextData;
    
    @Field("service_name")
    private String serviceName;
    
    @Field("endpoint")
    private String endpoint;
    
    @Field("http_method")
    private String httpMethod;
    
    @Field("response_code")
    private Integer responseCode;
    
    @Field("duration_ms")
    private Long durationMs;
    
    @Field("timestamp")
    private LocalDateTime timestamp;
}
