package com.banking.onboarding.step;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Generic Step Context - Contains all data needed for step execution
 * Now includes payload and response storage for complete traceability
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericStepContext {
    
    private String correlationId;
    private String processId;
    private Object inputData;
    private Map<String, Object> stepResults;
    private Map<String, Object> metadata;
    private LocalDateTime startedAt;
    
    // Enhanced payload and response tracking
    private Map<String, StepPayloadResponse> stepPayloadResponses;
    
    /**
     * Step payload and response data structure - STORES COMPLETE DATA AS-IS
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepPayloadResponse {
        private String stepName;
        private Object inputPayload;           // COMPLETE INPUT - NO TRUNCATION
        private Object outputResponse;         // COMPLETE OUTPUT - NO TRUNCATION
        private String inputPayloadType;       // XML, JSON, etc.
        private String outputResponseType;     // JSON, XML, etc.
        private long durationMs;
        private boolean success;
        private String errorMessage;
        private LocalDateTime timestamp;
        private Map<String, Object> additionalContext;
        
        // Raw payload storage - NO CONVERSION, NO TRUNCATION
        private byte[] inputPayloadRaw;        // Raw bytes for binary data
        private byte[] outputResponseRaw;     // Raw bytes for binary data
        private String inputPayloadRawString; // Raw string - complete, untruncated
        private String outputResponseRawString; // Raw string - complete, untruncated
        
        /**
         * Get input payload as string - COMPLETE, NO TRUNCATION
         */
        public String getInputPayloadAsString() {
            if (inputPayloadRawString != null) {
                return inputPayloadRawString; // Return stored raw string
            }
            return inputPayload != null ? inputPayload.toString() : null;
        }
        
        /**
         * Get output response as string - COMPLETE, NO TRUNCATION
         */
        public String getOutputResponseAsString() {
            if (outputResponseRawString != null) {
                return outputResponseRawString; // Return stored raw string
            }
            return outputResponse != null ? outputResponse.toString() : null;
        }
        
        /**
         * Get input payload as raw bytes
         */
        public byte[] getInputPayloadAsBytes() {
            return inputPayloadRaw;
        }
        
        /**
         * Get output response as raw bytes
         */
        public byte[] getOutputResponseAsBytes() {
            return outputResponseRaw;
        }
        
        /**
         * Get input payload size - ACCURATE SIZE
         */
        public long getInputPayloadSize() {
            if (inputPayloadRaw != null) {
                return inputPayloadRaw.length;
            }
            String payload = getInputPayloadAsString();
            return payload != null ? payload.length() : 0;
        }
        
        /**
         * Get output response size - ACCURATE SIZE
         */
        public long getOutputResponseSize() {
            if (outputResponseRaw != null) {
                return outputResponseRaw.length;
            }
            String response = getOutputResponseAsString();
            return response != null ? response.length() : 0;
        }
        
        /**
         * Get input payload as original object - NO CONVERSION
         */
        public Object getInputPayloadOriginal() {
            return inputPayload;
        }
        
        /**
         * Get output response as original object - NO CONVERSION
         */
        public Object getOutputResponseOriginal() {
            return outputResponse;
        }
    }
    
    /**
     * Get result from any previous step by name
     */
    public Object getStepResult(String stepName) {
        return stepResults != null ? stepResults.get(stepName) : null;
    }
    
    /**
     * Get result from previous step with type casting
     */
    @SuppressWarnings("unchecked")
    public <T> T getStepResult(String stepName, Class<T> resultType) {
        Object result = getStepResult(stepName);
        if (result != null && resultType.isAssignableFrom(result.getClass())) {
            return (T) result;
        }
        return null;
    }
    
    /**
     * Add result from current step
     */
    public void addStepResult(String stepName, Object result) {
        if (stepResults == null) {
            stepResults = new HashMap<>();
        }
        stepResults.put(stepName, result);
    }
    
    /**
     * Get metadata value
     */
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
    
    /**
     * Set metadata value
     */
    public void setMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
    
    /**
     * Check if step result exists
     */
    public boolean hasStepResult(String stepName) {
        return stepResults != null && stepResults.containsKey(stepName);
    }
    
    /**
     * Get all step results
     */
    public Map<String, Object> getAllStepResults() {
        return stepResults != null ? new HashMap<>(stepResults) : new HashMap<>();
    }
    
    // ===========================================
    // PAYLOAD AND RESPONSE MANAGEMENT METHODS
    // ===========================================
    
    /**
     * Store step payload and response - COMPLETE DATA AS-IS, NO TRUNCATION
     */
    public void storeStepPayloadResponse(String stepName, Object inputPayload, Object outputResponse,
                                       String inputPayloadType, String outputResponseType,
                                       long durationMs, boolean success, String errorMessage,
                                       Map<String, Object> additionalContext) {
        if (stepPayloadResponses == null) {
            stepPayloadResponses = new HashMap<>();
        }
        
        // Store complete payload and response as-is
        StepPayloadResponse payloadResponse = StepPayloadResponse.builder()
                .stepName(stepName)
                .inputPayload(inputPayload)                    // COMPLETE INPUT - NO TRUNCATION
                .outputResponse(outputResponse)                // COMPLETE OUTPUT - NO TRUNCATION
                .inputPayloadType(inputPayloadType)
                .outputResponseType(outputResponseType)
                .durationMs(durationMs)
                .success(success)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .additionalContext(additionalContext != null ? additionalContext : new HashMap<>())
                // Store raw data for complete preservation
                .inputPayloadRawString(convertToString(inputPayload))      // COMPLETE STRING
                .outputResponseRawString(convertToString(outputResponse))   // COMPLETE STRING
                .inputPayloadRaw(convertToBytes(inputPayload))             // RAW BYTES
                .outputResponseRaw(convertToBytes(outputResponse))          // RAW BYTES
                .build();
        
        stepPayloadResponses.put(stepName, payloadResponse);
    }
    
    /**
     * Convert object to string - COMPLETE, NO TRUNCATION
     */
    private String convertToString(Object obj) {
        if (obj == null) return null;
        
        if (obj instanceof String) {
            return (String) obj; // Return as-is, no conversion
        } else if (obj instanceof byte[]) {
            return new String((byte[]) obj); // Convert bytes to string
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
            return ((String) obj).getBytes(); // Convert string to bytes
        } else {
            return obj.toString().getBytes(); // Convert to string then bytes
        }
    }
    
    /**
     * Store successful step payload and response
     */
    public void storeSuccessfulStepPayloadResponse(String stepName, Object inputPayload, Object outputResponse,
                                                  String inputPayloadType, String outputResponseType,
                                                  long durationMs, Map<String, Object> additionalContext) {
        storeStepPayloadResponse(stepName, inputPayload, outputResponse, inputPayloadType, 
                               outputResponseType, durationMs, true, null, additionalContext);
    }
    
    /**
     * Store failed step payload and response
     */
    public void storeFailedStepPayloadResponse(String stepName, Object inputPayload, Object outputResponse,
                                             String inputPayloadType, String outputResponseType,
                                             long durationMs, String errorMessage, Map<String, Object> additionalContext) {
        storeStepPayloadResponse(stepName, inputPayload, outputResponse, inputPayloadType, 
                               outputResponseType, durationMs, false, errorMessage, additionalContext);
    }
    
    /**
     * Get step payload and response by step name
     */
    public StepPayloadResponse getStepPayloadResponse(String stepName) {
        return stepPayloadResponses != null ? stepPayloadResponses.get(stepName) : null;
    }
    
    /**
     * Get all step payload and responses
     */
    public Map<String, StepPayloadResponse> getAllStepPayloadResponses() {
        return stepPayloadResponses != null ? new HashMap<>(stepPayloadResponses) : new HashMap<>();
    }
    
    /**
     * Check if step payload and response exists
     */
    public boolean hasStepPayloadResponse(String stepName) {
        return stepPayloadResponses != null && stepPayloadResponses.containsKey(stepName);
    }
    
    /**
     * Get step input payload by step name
     */
    public Object getStepInputPayload(String stepName) {
        StepPayloadResponse payloadResponse = getStepPayloadResponse(stepName);
        return payloadResponse != null ? payloadResponse.getInputPayload() : null;
    }
    
    /**
     * Get step output response by step name
     */
    public Object getStepOutputResponse(String stepName) {
        StepPayloadResponse payloadResponse = getStepPayloadResponse(stepName);
        return payloadResponse != null ? payloadResponse.getOutputResponse() : null;
    }
    
    /**
     * Get step input payload as string
     */
    public String getStepInputPayloadAsString(String stepName) {
        StepPayloadResponse payloadResponse = getStepPayloadResponse(stepName);
        return payloadResponse != null ? payloadResponse.getInputPayloadAsString() : null;
    }
    
    /**
     * Get step output response as string
     */
    public String getStepOutputResponseAsString(String stepName) {
        StepPayloadResponse payloadResponse = getStepPayloadResponse(stepName);
        return payloadResponse != null ? payloadResponse.getOutputResponseAsString() : null;
    }
    
    /**
     * Get step execution summary
     */
    public Map<String, Object> getStepExecutionSummary(String stepName) {
        StepPayloadResponse payloadResponse = getStepPayloadResponse(stepName);
        if (payloadResponse == null) {
            return Map.of("exists", false);
        }
        
        return Map.of(
            "stepName", payloadResponse.getStepName(),
            "success", payloadResponse.isSuccess(),
            "durationMs", payloadResponse.getDurationMs(),
            "inputPayloadSize", payloadResponse.getInputPayloadSize(),
            "outputResponseSize", payloadResponse.getOutputResponseSize(),
            "inputPayloadType", payloadResponse.getInputPayloadType(),
            "outputResponseType", payloadResponse.getOutputResponseType(),
            "timestamp", payloadResponse.getTimestamp(),
            "errorMessage", payloadResponse.getErrorMessage()
        );
    }
    
    /**
     * Get complete execution trace
     */
    public Map<String, Object> getCompleteExecutionTrace() {
        Map<String, Object> trace = new HashMap<>();
        trace.put("correlationId", correlationId);
        trace.put("processId", processId);
        trace.put("startedAt", startedAt);
        trace.put("totalSteps", stepPayloadResponses != null ? stepPayloadResponses.size() : 0);
        
        if (stepPayloadResponses != null) {
            Map<String, Object> stepTraces = new HashMap<>();
            stepPayloadResponses.forEach((stepName, payloadResponse) -> {
                stepTraces.put(stepName, Map.of(
                    "success", payloadResponse.isSuccess(),
                    "durationMs", payloadResponse.getDurationMs(),
                    "inputPayloadSize", payloadResponse.getInputPayloadSize(),
                    "outputResponseSize", payloadResponse.getOutputResponseSize(),
                    "inputPayloadType", payloadResponse.getInputPayloadType(),
                    "outputResponseType", payloadResponse.getOutputResponseType(),
                    "timestamp", payloadResponse.getTimestamp(),
                    "errorMessage", payloadResponse.getErrorMessage()
                ));
            });
            trace.put("stepTraces", stepTraces);
        }
        
        return trace;
    }
    
    /**
     * Create context from initial input
     */
    public static GenericStepContext create(String correlationId, String processId, Object inputData) {
        return GenericStepContext.builder()
                .correlationId(correlationId)
                .processId(processId)
                .inputData(inputData)
                .stepResults(new HashMap<>())
                .metadata(new HashMap<>())
                .stepPayloadResponses(new HashMap<>())
                .startedAt(LocalDateTime.now())
                .build();
    }
}
