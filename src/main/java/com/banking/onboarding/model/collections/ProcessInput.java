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
 * Process Input Collection - Original input data for processes
 * Collection: process_inputs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "process_inputs")
@CompoundIndex(def = "{'processId': 1, 'inputType': 1}")
@CompoundIndex(def = "{'correlationId': 1, 'inputSource': 1}")
public class ProcessInput {
    
    @Id
    private String id;
    
    @Indexed
    private String processId;
    
    @Indexed
    private String correlationId;
    
    private String inputType;            // XML, JSON, BINARY, etc.
    private String inputSource;          // API, FILE, STREAM, etc.
    
    // Input data - COMPLETE AS-IS
    private Object inputData;            // Complete input object
    private String inputRawString;      // Raw string representation
    private byte[] inputRawBytes;        // Raw bytes for binary data
    
    // Input metadata
    private String contentType;          // application/xml, application/json, etc.
    private String encoding;             // UTF-8, etc.
    private long inputSizeBytes;        // Accurate size in bytes
    private String inputHash;           // Hash for integrity verification
    
    // Input structure info
    private String inputFormat;         // JSON, XML, BINARY, TEXT
    private String inputSchema;         // Schema version or reference
    private Map<String, Object> inputMetadata; // Additional metadata
    
    // Source information
    private String sourceSystem;        // External system name
    private String sourceVersion;       // Source system version
    private String sourceEndpoint;      // API endpoint or file path
    private Map<String, String> sourceHeaders; // HTTP headers if applicable
    
    // Timestamps
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    
    // Data integrity
    private boolean isCompressed;
    private String compressionType;
    private boolean isEncrypted;
    private String encryptionType;
    private String signature;           // Digital signature if applicable
}
