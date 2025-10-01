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
 * Generic XML to JSON Transformation Step
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenericXmlToJsonTransformationStep implements GenericStepExecutor {
    
    private final TransformationService transformationService;
    
    @Override
    public CompletableFuture<StepResult<Object>> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing generic XML to JSON transformation", context.getCorrelationId());
        
        // Get input data - can be String or any other type
        Object inputData = getInputData(context);
        String xmlData = convertToString(inputData);
        
        if (xmlData == null || xmlData.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    StepResult.failure("No valid XML data found", getStepName(), context.getCorrelationId())
            );
        }
        
        return transformationService.transformXmlToJson(xmlData, context.getCorrelationId())
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        // Store result in context for next steps
                        context.addStepResult(getStepName(), response.getBody());
                        return StepResult.success(response.getBody(), getStepName(), context.getCorrelationId());
                    } else {
                        return StepResult.failure(response.getErrorMessage(), getStepName(), context.getCorrelationId());
                    }
                });
    }
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description("Transform XML data to JSON format via Apigee")
                .retryEnabled(true)
                .maxRetries(3)
                .retryDelayMs(1000)
                .backoffMultiplier(2.0)
                .asyncEnabled(true)
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
