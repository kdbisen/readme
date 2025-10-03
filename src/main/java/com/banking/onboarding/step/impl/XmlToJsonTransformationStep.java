package com.banking.onboarding.step.impl;

import com.banking.onboarding.constants.OnboardingConstants;
import com.banking.onboarding.enums.OnboardingEnums;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import com.banking.onboarding.step.enhancer.StepContextEnhancer;
import com.banking.onboarding.step.util.DynamicStepLoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * XML to JSON Transformation Step - Dynamic Step Numbering
 * Transforms XML input data to JSON format (simplified for demo)
 * Uses dynamic step numbering instead of hardcoded step numbers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XmlToJsonTransformationStep implements GenericStepExecutor {
    
    private final DynamicStepLoggingUtil stepLoggingUtil;
    private final StepContextEnhancer stepContextEnhancer;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Enhance context with dynamic step numbering
        StepContextEnhancer.EnhancedStepContext enhancedContext = 
            stepContextEnhancer.enhanceContext(context, getStepName());
        
        // Log step start with dynamic step number
        stepLoggingUtil.logStepStart(enhancedContext, "XML to JSON transformation");
        
        // Get input data - can be String or any other type
        Object inputData = getInputData(context);
        String xmlData = convertToString(inputData);
        
        if (xmlData == null || xmlData.trim().isEmpty()) {
            stepLoggingUtil.logStepFailure(enhancedContext, "No valid XML data found");
            return StepResult.failure("No valid XML data found", getStepName(), context.getCorrelationId());
        }
        
        try {
            // Mock transformation - convert XML to JSON-like structure
            String jsonData = mockXmlToJsonTransformation(xmlData);
            
            // Store result in context for next steps
            context.addStepResult(getStepName(), jsonData);
            
            // Log step completion with dynamic step number
            stepLoggingUtil.logStepCompletion(enhancedContext, 
                String.format("XML transformed to JSON: %d chars", jsonData.length()));
            
            // Log data sharing for next step
            stepLoggingUtil.logStepDataSharing(enhancedContext, "JSON", jsonData);
            
            return StepResult.success(jsonData, getStepName(), context.getCorrelationId());
        } catch (Exception e) {
            stepLoggingUtil.logStepFailure(enhancedContext, e.getMessage());
            return StepResult.failure("XML to JSON transformation failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
    }
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description("Transform XML data to JSON format")
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