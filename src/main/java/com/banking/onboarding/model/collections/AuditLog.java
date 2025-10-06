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
 * Audit Log Collection - Comprehensive audit trail
 * Collection: audit_logs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
@CompoundIndex(def = "{'correlationId': 1, 'eventType': 1}")
@CompoundIndex(def = "{'processId': 1, 'timestamp': 1}")
@CompoundIndex(def = "{'userId': 1, 'timestamp': 1}")
public class AuditLog {
    
    @Id
    private String id;
    
    @Indexed
    private String processId;
    
    @Indexed
    private String correlationId;
    
    private String eventType;           // PROCESS_START, STEP_EXECUTE, PROCESS_COMPLETE, etc.
    private String eventCategory;      // PROCESS, STEP, SYSTEM, SECURITY, etc.
    private String eventAction;        // CREATE, UPDATE, DELETE, EXECUTE, etc.
    
    // Event details
    private String eventDescription;
    private String eventSource;        // SYSTEM, USER, API, SCHEDULER, etc.
    private String eventTarget;        // Target system or component
    
    // User context
    private String userId;
    private String userName;
    private String userRole;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    
    // Event data
    private Map<String, Object> eventData;
    private Map<String, Object> beforeState;
    private Map<String, Object> afterState;
    private Map<String, Object> eventContext;
    
    // Timestamps
    private LocalDateTime timestamp;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;
    private long eventDurationMs;
    
    // Event result
    private String eventResult;        // SUCCESS, FAILURE, WARNING, etc.
    private String eventMessage;
    private String errorCode;
    private String errorMessage;
    
    // Security context
    private String securityLevel;     // PUBLIC, INTERNAL, CONFIDENTIAL, SECRET
    private String complianceFlags;   // GDPR, SOX, PCI, etc.
    private boolean isSensitiveData;
    private String dataClassification;
    
    // System context
    private String systemVersion;
    private String environment;       // DEV, TEST, STAGING, PROD
    private String region;
    private String availabilityZone;
}
