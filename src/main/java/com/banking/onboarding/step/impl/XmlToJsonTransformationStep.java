package com.banking.onboarding.step.impl;

import com.banking.onboarding.service.TransformationService;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Step 0: XML to JSON Transformation via Apigee (Internal API) - SYNCHRONOUS
 * Transforms XML input data to JSON format using internal Apigee service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XmlToJsonTransformationStep implements GenericStepExecutor {
    
    private final TransformationService transformationService;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing Step 0: XML to JSON transformation via Apigee", context.getCorrelationId());
        
        // Get input data - can be String or any other type
        Object inputData = getInputData(context);
        String xmlData = convertToString(inputData);
        
        if (xmlData == null || xmlData.trim().isEmpty()) {
            return StepResult.failure("No valid XML data found", getStepName(), context.getCorrelationId());
        }
        
        try {
            // Execute transformation synchronously by blocking on CompletableFuture
            CompletableFuture<com.banking.onboarding.domain.ApiResponse> future = 
                    transformationService.transformXmlToJson(xmlData, context.getCorrelationId());
            
            com.banking.onboarding.domain.ApiResponse response = future.get(); // Block here
            
            if (response.isSuccess()) {
                // Store result in context for next steps
                context.addStepResult(getStepName(), response.getBody());
                
                log.info("[CORRELATION:{}] Step 0 completed successfully. XML transformed to JSON: {} chars", 
                        context.getCorrelationId(), 
                        response.getBody() != null ? response.getBody().toString().length() : 0);
                
                return StepResult.success(response.getBody(), getStepName(), context.getCorrelationId());
            } else {
                return StepResult.failure(response.getErrorMessage(), getStepName(), context.getCorrelationId());
            }
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Step 0 failed: {}", context.getCorrelationId(), e.getMessage());
            return StepResult.failure("XML to JSON transformation failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
    }
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description("Transform XML data to JSON format via internal Apigee service")
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
}
