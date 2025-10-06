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
 * Error Event Collection - Detailed error tracking
 * Collection: error_events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "error_events")
@CompoundIndex(def = "{'correlationId': 1, 'errorType': 1}")
@CompoundIndex(def = "{'processId': 1, 'stepId': 1}")
@CompoundIndex(def = "{'errorSeverity': 1, 'timestamp': 1}")
public class ErrorEvent {
    
    @Id
    private String id;
    
    @Indexed
    private String processId;
    
    @Indexed
    private String stepId;
    
    @Indexed
    private String correlationId;
    
    private String errorType;          // VALIDATION_ERROR, SYSTEM_ERROR, EXTERNAL_API_ERROR, etc.
    private String errorCategory;      // BUSINESS, TECHNICAL, INFRASTRUCTURE, etc.
    private String errorSeverity;     // LOW, MEDIUM, HIGH, CRITICAL
    
    // Error details
    private String errorCode;
    private String errorMessage;
    private String errorDescription;
    private String exceptionClass;
    private String stackTrace;
    
    // Error context
    private String errorSource;        // STEP_EXECUTION, API_CALL, DATABASE, etc.
    private String errorComponent;     // Component where error occurred
    private String errorMethod;       // Method where error occurred
    private int errorLineNumber;      // Line number if available
    
    // Error data
    private Map<String, Object> errorContext;
    private Map<String, Object> errorData;
    private Object errorPayload;      // Payload that caused error
    private String errorPayloadType;  // Type of error payload
    
    // Recovery information
    private boolean recoverable;
    private String recoveryAction;
    private int retryCount;
    private int maxRetries;
    private String retryStrategy;
    
    // Resolution information
    private boolean resolved;
    private String resolutionAction;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    
    // Timestamps
    private LocalDateTime timestamp;
    private LocalDateTime firstOccurrence;
    private LocalDateTime lastOccurrence;
    
    // Impact assessment
    private String impactLevel;        // LOW, MEDIUM, HIGH, CRITICAL
    private String businessImpact;
    private String technicalImpact;
    private boolean userFacing;
    private int affectedUsers;
    
    // System context
    private String systemVersion;
    private String environment;
    private String region;
    private Map<String, Object> systemContext;
}
