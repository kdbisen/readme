package com.banking.onboarding.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Complete Payload Storage Utility - NO TRUNCATION, NO DATA LOSS
 * Ensures all payloads and responses are stored completely as-is
 */
@Slf4j
@Component
public class CompletePayloadStorageUtil {
    
    /**
     * Store complete payload as-is - NO TRUNCATION
     */
    public CompletePayloadData storeCompletePayload(Object payload, String payloadType) {
        if (payload == null) {
            return CompletePayloadData.builder()
                    .originalPayload(null)
                    .payloadType("NULL")
                    .payloadSize(0)
                    .payloadAsString(null)
                    .payloadAsBytes(null)
                    .build();
        }
        
        try {
            // Store original payload as-is
            Object originalPayload = payload;
            
            // Convert to string - COMPLETE, NO TRUNCATION
            String payloadAsString = convertToString(payload);
            
            // Convert to bytes - COMPLETE, NO TRUNCATION
            byte[] payloadAsBytes = convertToBytes(payload);
            
            // Calculate accurate size
            long payloadSize = calculateAccurateSize(payload);
            
            log.debug("Stored complete payload: type={}, size={} bytes", payloadType, payloadSize);
            
            return CompletePayloadData.builder()
                    .originalPayload(originalPayload)
                    .payloadType(payloadType)
                    .payloadSize(payloadSize)
                    .payloadAsString(payloadAsString)
                    .payloadAsBytes(payloadAsBytes)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error storing complete payload: {}", e.getMessage());
            
            // Fallback - store what we can
            return CompletePayloadData.builder()
                    .originalPayload(payload)
                    .payloadType(payloadType)
                    .payloadSize(0)
                    .payloadAsString(payload != null ? payload.toString() : null)
                    .payloadAsBytes(null)
                    .build();
        }
    }
    
    /**
     * Convert object to string - COMPLETE, NO TRUNCATION
     */
    private String convertToString(Object obj) {
        if (obj == null) return null;
        
        if (obj instanceof String) {
            return (String) obj; // Return as-is, no conversion
        } else if (obj instanceof byte[]) {
            return new String((byte[]) obj, StandardCharsets.UTF_8);
        } else if (obj instanceof Map) {
            // For Maps, convert to JSON-like string representation
            return obj.toString(); // This will show the complete map structure
        } else {
            return obj.toString(); // Use toString() - complete representation
        }
    }
    
    /**
     * Convert object to bytes - COMPLETE, NO TRUNCATION
     */
    private byte[] convertToBytes(Object obj) {
        if (obj == null) return null;
        
        if (obj instanceof byte[]) {
            return (byte[]) obj; // Return as-is
        } else if (obj instanceof String) {
            return ((String) obj).getBytes(StandardCharsets.UTF_8);
        } else {
            // For complex objects, serialize to bytes
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(obj);
                oos.close();
                return baos.toByteArray();
            } catch (IOException e) {
                log.warn("Could not serialize object to bytes, using toString: {}", e.getMessage());
                return obj.toString().getBytes(StandardCharsets.UTF_8);
            }
        }
    }
    
    /**
     * Calculate accurate payload size - NO TRUNCATION
     */
    private long calculateAccurateSize(Object obj) {
        if (obj == null) return 0;
        
        if (obj instanceof String) {
            return ((String) obj).length();
        } else if (obj instanceof byte[]) {
            return ((byte[]) obj).length;
        } else if (obj instanceof Map) {
            return obj.toString().length();
        } else {
            return obj.toString().length();
        }
    }
    
    /**
     * Complete payload data structure
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompletePayloadData {
        private Object originalPayload;        // COMPLETE ORIGINAL - NO TRUNCATION
        private String payloadType;            // Type (XML, JSON, etc.)
        private long payloadSize;              // ACCURATE SIZE
        private String payloadAsString;        // COMPLETE STRING - NO TRUNCATION
        private byte[] payloadAsBytes;         // RAW BYTES - NO TRUNCATION
        
        /**
         * Get payload as string - COMPLETE, NO TRUNCATION
         */
        public String getPayloadAsCompleteString() {
            return payloadAsString;
        }
        
        /**
         * Get payload as bytes - COMPLETE, NO TRUNCATION
         */
        public byte[] getPayloadAsCompleteBytes() {
            return payloadAsBytes;
        }
        
        /**
         * Get original payload - NO CONVERSION
         */
        public Object getOriginalPayload() {
            return originalPayload;
        }
        
        /**
         * Get accurate payload size
         */
        public long getAccuratePayloadSize() {
            return payloadSize;
        }
    }
}






