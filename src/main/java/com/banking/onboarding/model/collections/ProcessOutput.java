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
 * Process Output Collection - Final output data for processes
 * Collection: process_outputs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "process_outputs")
@CompoundIndex(def = "{'processId': 1, 'outputType': 1}")
@CompoundIndex(def = "{'correlationId': 1, 'outputStatus': 1}")
public class ProcessOutput {
    
    @Id
    private String id;
    
    @Indexed
    private String processId;
    
    @Indexed
    private String correlationId;
    
    private String outputType;           // SUCCESS, ERROR, PARTIAL, etc.
    private String outputStatus;        // COMPLETED, FAILED, CANCELLED, etc.
    
    // Output data - COMPLETE AS-IS
    private Object outputData;          // Complete output object
    private String outputRawString;     // Raw string representation
    private byte[] outputRawBytes;      // Raw bytes for binary data
    
    // Output metadata
    private String contentType;         // application/json, application/xml, etc.
    private String encoding;            // UTF-8, etc.
    private long outputSizeBytes;       // Accurate size in bytes
    private String outputHash;          // Hash for integrity verification
    
    // Output structure info
    private String outputFormat;        // JSON, XML, BINARY, TEXT
    private String outputSchema;        // Schema version or reference
    private Map<String, Object> outputMetadata; // Additional metadata
    
    // Result information
    private String resultCode;          // SUCCESS, ERROR, WARNING, etc.
    private String resultMessage;       // Human-readable result message
    private Map<String, Object> resultSummary; // Summary of results
    
    // Destination information
    private String destinationSystem;   // Target system name
    private String destinationEndpoint; // API endpoint or file path
    private Map<String, String> destinationHeaders; // HTTP headers if applicable
    
    // Timestamps
    private LocalDateTime generatedAt;
    private LocalDateTime deliveredAt;
    
    // Data integrity
    private boolean isCompressed;
    private String compressionType;
    private boolean isEncrypted;
    private String encryptionType;
    private String signature;          // Digital signature if applicable
    
    // Delivery status
    private boolean isDelivered;
    private String deliveryStatus;
    private String deliveryError;
    private int deliveryAttempts;
}
