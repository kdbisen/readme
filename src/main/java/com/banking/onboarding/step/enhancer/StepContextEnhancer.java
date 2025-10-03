package com.banking.onboarding.step.enhancer;

import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.service.DynamicStepNumberingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Step Context Enhancer
 * Enhances step context with dynamic step numbering information
 * Provides step numbers, execution order, and related metadata
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StepContextEnhancer {
    
    private final DynamicStepNumberingService stepNumberingService;
    
    /**
     * Enhance step context with dynamic step numbering information
     */
    public EnhancedStepContext enhanceContext(GenericStepContext context, String stepName) {
        int stepNumber = stepNumberingService.getStepNumber(stepName);
        int totalSteps = stepNumberingService.getTotalSteps();
        
        Map<String, Object> stepMetadata = new HashMap<>();
        stepMetadata.put("stepNumber", stepNumber);
        stepMetadata.put("totalSteps", totalSteps);
        stepMetadata.put("stepNumberText", stepNumberingService.getStepNumberText(stepName));
        stepMetadata.put("stepNumberWithDescription", stepNumberingService.getStepNumberWithDescription(stepName));
        stepMetadata.put("isFirstStep", stepNumberingService.isFirstStep(stepName));
        stepMetadata.put("isLastStep", stepNumberingService.isLastStep(stepName));
        stepMetadata.put("nextStep", stepNumberingService.getNextStep(stepName));
        stepMetadata.put("previousStep", stepNumberingService.getPreviousStep(stepName));
        stepMetadata.put("executionOrder", stepNumberingService.getExecutionOrder());
        
        // Add step metadata to existing context metadata
        Map<String, Object> existingMetadata = context.getMetadata();
        if (existingMetadata == null) {
            existingMetadata = new HashMap<>();
        }
        existingMetadata.putAll(stepMetadata);
        context.setMetadata(existingMetadata);
        
        log.debug("[CORRELATION:{}] Enhanced context for step {} with step number {}", 
                context.getCorrelationId(), stepName, stepNumber);
        
        return new EnhancedStepContext(context, stepName, stepNumber, totalSteps);
    }
    
    /**
     * Enhanced Step Context with step numbering information
     */
    public static class EnhancedStepContext {
        private final GenericStepContext originalContext;
        private final String stepName;
        private final int stepNumber;
        private final int totalSteps;
        
        public EnhancedStepContext(GenericStepContext originalContext, String stepName, int stepNumber, int totalSteps) {
            this.originalContext = originalContext;
            this.stepName = stepName;
            this.stepNumber = stepNumber;
            this.totalSteps = totalSteps;
        }
        
        public GenericStepContext getOriginalContext() {
            return originalContext;
        }
        
        public String getStepName() {
            return stepName;
        }
        
        public int getStepNumber() {
            return stepNumber;
        }
        
        public int getTotalSteps() {
            return totalSteps;
        }
        
        public String getStepNumberText() {
            return String.format("Step %d", stepNumber);
        }
        
        public String getStepNumberWithDescription() {
            return String.format("Step %d: %s", stepNumber, stepName);
        }
        
        public boolean isFirstStep() {
            return stepNumber == 1;
        }
        
        public boolean isLastStep() {
            return stepNumber == totalSteps;
        }
        
        public String getProgressText() {
            return String.format("Step %d of %d", stepNumber, totalSteps);
        }
        
        public double getProgressPercentage() {
            return (double) stepNumber / totalSteps * 100;
        }
        
        public String getCorrelationId() {
            return originalContext.getCorrelationId();
        }
        
        public String getProcessId() {
            return originalContext.getProcessId();
        }
        
        public Object getInputData() {
            return originalContext.getInputData();
        }
        
        public Map<String, Object> getStepResults() {
            return originalContext.getStepResults();
        }
        
        public Map<String, Object> getMetadata() {
            return originalContext.getMetadata();
        }
    }
}



