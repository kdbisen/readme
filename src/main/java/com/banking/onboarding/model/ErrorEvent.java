package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error Event Model - Stores all errors and exceptions in the database
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "error_events")
public class ErrorEvent {

    @Id
    private String id;

    @Indexed
    @Field("error_type")
    private String errorType; // e.g., APPLICATION_ERROR, VALIDATION_ERROR, EXTERNAL_SERVICE_ERROR

    @Field("error_message")
    private String errorMessage;

    @Indexed
    @Field("correlation_id")
    private String correlationId;

    @Indexed
    @Field("trace_id")
    private String traceId;

    @Field("service_name")
    private String serviceName;

    @Field("method_name")
    private String methodName;

    @Field("exception_type")
    private String exceptionType; // e.g., NullPointerException, IllegalArgumentException

    @Field("stack_trace")
    private String stackTrace;

    @Field("context_data")
    private Map<String, Object> contextData; // Additional context information

    @Field("timestamp")
    private LocalDateTime timestamp;

    @Field("environment")
    private String environment; // e.g., dev, test, prod

    @Field("version")
    private String version; // Application version

    @Field("severity")
    private String severity; // e.g., ERROR, WARN, INFO

    @Field("resolved")
    private boolean resolved; // Whether the error has been resolved

    @Field("resolved_at")
    private LocalDateTime resolvedAt;

    @Field("resolved_by")
    private String resolvedBy; // Who resolved the error

    @Field("resolution_notes")
    private String resolutionNotes;

    @Version
    private Long version;
}
