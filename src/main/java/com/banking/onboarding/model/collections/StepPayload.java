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
 * Step Payload Collection - Complete input/output data for each step
 * Collection: step_payloads
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "step_payloads")
@CompoundIndex(def = "{'stepId': 1, 'payloadType': 1}")
@CompoundIndex(def = "{'processId': 1, 'stepName': 1}")
public class StepPayload {
    
    @Id
    private String id;
    
    @Indexed
    private String stepId;
    
    @Indexed
    private String processId;
    
    @Indexed
    private String correlationId;
    
    private String stepName;
    private PayloadType payloadType;
    
    // Payload data - COMPLETE AS-IS
    private Object payloadData;           // Complete payload object
    private String payloadRawString;      // Raw string representation
    private byte[] payloadRawBytes;       // Raw bytes for binary data
    
    // Payload metadata
    private String contentType;           // application/json, application/xml, etc.
    private String encoding;             // UTF-8, etc.
    private long payloadSizeBytes;       // Accurate size in bytes
    private String payloadHash;          // Hash for integrity verification
    
    // Payload structure info
    private String payloadFormat;        // JSON, XML, BINARY, TEXT
    private String payloadSchema;        // Schema version or reference
    private Map<String, Object> payloadMetadata; // Additional metadata
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Data integrity
    private boolean isCompressed;
    private String compressionType;
    private boolean isEncrypted;
    private String encryptionType;
    
    public enum PayloadType {
        INPUT, OUTPUT, INTERMEDIATE, ERROR_RESPONSE, RETRY_INPUT
    }
}
