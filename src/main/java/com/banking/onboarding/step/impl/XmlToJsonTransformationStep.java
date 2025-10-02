package com.banking.onboarding.step.impl;

import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Step 0: XML to JSON Transformation - Mock Implementation
 * Transforms XML input data to JSON format (simplified for demo)
 */
@Slf4j
@Component
public class XmlToJsonTransformationStep implements GenericStepExecutor {
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing Step 0: XML to JSON transformation", context.getCorrelationId());
        
        // Get input data - can be String or any other type
        Object inputData = getInputData(context);
        String xmlData = convertToString(inputData);
        
        if (xmlData == null || xmlData.trim().isEmpty()) {
            return StepResult.failure("No valid XML data found", getStepName(), context.getCorrelationId());
        }
        
        try {
            // Mock transformation - convert XML to JSON-like structure
            String jsonData = mockXmlToJsonTransformation(xmlData);
            
            // Store result in context for next steps
            context.addStepResult(getStepName(), jsonData);
            
            log.info("[CORRELATION:{}] Step 0 completed successfully. XML transformed to JSON: {} chars", 
                    context.getCorrelationId(), jsonData.length());
            
            return StepResult.success(jsonData, getStepName(), context.getCorrelationId());
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Step 0 failed: {}", context.getCorrelationId(), e.getMessage());
            return StepResult.failure("XML to JSON transformation failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
    }
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description("Transform XML data to JSON format")
                .retryEnabled(true)
                .maxRetries(3)
                .retryDelayMs(1000)
                .backoffMultiplier(2.0)
                .asyncEnabled(false)
                .timeoutMs(30000)
                .build();
    }
    
    @Override
    public String getStepName() {
        return "XML_TO_JSON_TRANSFORMATION";
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        Object inputData = getInputData(context);
        String xmlData = convertToString(inputData);
        return xmlData != null && !xmlData.trim().isEmpty();
    }
    
    /**
     * Convert any input data to String
     */
    private String convertToString(Object inputData) {
        if (inputData == null) return null;
        if (inputData instanceof String) return (String) inputData;
        return inputData.toString();
    }
    
    /**
     * Mock XML to JSON transformation
     */
    private String mockXmlToJsonTransformation(String xmlData) {
        // Simple mock transformation for demo purposes
        return String.format("""
            {
                "transformedFromXml": true,
                "originalXmlLength": %d,
                "transformationTimestamp": "%s",
                "data": {
                    "xmlContent": "%s",
                    "status": "transformed"
                }
            }
            """, xmlData.length(), java.time.Instant.now().toString(), xmlData.replace("\"", "\\\""));
    }
}