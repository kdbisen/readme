package com.banking.onboarding.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Audit Event Model
 * Stores audit trail information for compliance and security
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_events")
public class AuditEvent {
    
    @Id
    private String id;
    
    private String eventId;
    private AuditService.AuditEventType eventType;
    private String processId;
    private String correlationId;
    private String userId;
    private String stepName;
    private LocalDateTime timestamp;
    private boolean success;
    private Long durationMs;
    private Map<String, Object> details;
    
    // Indexed fields for efficient querying
    private String eventTypeStr; // For MongoDB queries
    private String processIdStr; // For MongoDB queries
    private String userIdStr;    // For MongoDB queries
    private String dateStr;      // For date-based queries (YYYY-MM-DD)
    
    // Auto-populate indexed fields
    public void setEventType(AuditService.AuditEventType eventType) {
        this.eventType = eventType;
        this.eventTypeStr = eventType != null ? eventType.toString() : null;
    }
    
    public void setProcessId(String processId) {
        this.processId = processId;
        this.processIdStr = processId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
        this.userIdStr = userId;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        this.dateStr = timestamp != null ? timestamp.toLocalDate().toString() : null;
    }
}
