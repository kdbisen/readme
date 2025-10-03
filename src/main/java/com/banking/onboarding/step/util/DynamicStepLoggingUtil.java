package com.banking.onboarding.step.util;

import com.banking.onboarding.step.enhancer.StepContextEnhancer;
import com.banking.onboarding.step.service.DynamicStepNumberingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Dynamic Step Logging Utility
 * Provides consistent logging with dynamic step numbers
 * Eliminates hardcoded step numbers in log messages
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicStepLoggingUtil {
    
    private final DynamicStepNumberingService stepNumberingService;
    
    /**
     * Log step execution start with dynamic step number
     */
    public void logStepStart(String correlationId, String stepName, String description) {
        int stepNumber = stepNumberingService.getStepNumber(stepName);
        String stepNumberText = stepNumberingService.getStepNumberText(stepName);
        
        log.info("[CORRELATION:{}] Executing {}: {}", correlationId, stepNumberText, description);
    }
    
    /**
     * Log step execution start with enhanced context
     */
    public void logStepStart(StepContextEnhancer.EnhancedStepContext enhancedContext, String description) {
        String stepNumberText = enhancedContext.getStepNumberText();
        String progressText = enhancedContext.getProgressText();
        
        log.info("[CORRELATION:{}] Executing {}: {} ({})", 
                enhancedContext.getCorrelationId(), stepNumberText, description, progressText);
    }
    
    /**
     * Log step completion with dynamic step number
     */
    public void logStepCompletion(String correlationId, String stepName, String resultDescription) {
        int stepNumber = stepNumberingService.getStepNumber(stepName);
        String stepNumberText = stepNumberingService.getStepNumberText(stepName);
        boolean isLastStep = stepNumberingService.isLastStep(stepName);
        
        if (isLastStep) {
            log.info("[CORRELATION:{}] {} completed successfully. {} (Final Step)", 
                    correlationId, stepNumberText, resultDescription);
        } else {
            log.info("[CORRELATION:{}] {} completed successfully. {}", 
                    correlationId, stepNumberText, resultDescription);
        }
    }
    
    /**
     * Log step completion with enhanced context
     */
    public void logStepCompletion(StepContextEnhancer.EnhancedStepContext enhancedContext, String resultDescription) {
        String stepNumberText = enhancedContext.getStepNumberText();
        String progressText = enhancedContext.getProgressText();
        
        if (enhancedContext.isLastStep()) {
            log.info("[CORRELATION:{}] {} completed successfully. {} (Final Step - {})", 
                    enhancedContext.getCorrelationId(), stepNumberText, resultDescription, progressText);
        } else {
            log.info("[CORRELATION:{}] {} completed successfully. {} ({})", 
                    enhancedContext.getCorrelationId(), stepNumberText, resultDescription, progressText);
        }
    }
    
    /**
     * Log step failure with dynamic step number
     */
    public void logStepFailure(String correlationId, String stepName, String errorMessage) {
        int stepNumber = stepNumberingService.getStepNumber(stepName);
        String stepNumberText = stepNumberingService.getStepNumberText(stepName);
        
        log.error("[CORRELATION:{}] {} failed: {}", correlationId, stepNumberText, errorMessage);
    }
    
    /**
     * Log step failure with enhanced context
     */
    public void logStepFailure(StepContextEnhancer.EnhancedStepContext enhancedContext, String errorMessage) {
        String stepNumberText = enhancedContext.getStepNumberText();
        String progressText = enhancedContext.getProgressText();
        
        log.error("[CORRELATION:{}] {} failed: {} ({})", 
                enhancedContext.getCorrelationId(), stepNumberText, errorMessage, progressText);
    }
    
    /**
     * Log step progress with dynamic step number
     */
    public void logStepProgress(String correlationId, String stepName, String progressMessage) {
        int stepNumber = stepNumberingService.getStepNumber(stepName);
        String stepNumberText = stepNumberingService.getStepNumberText(stepName);
        
        log.debug("[CORRELATION:{}] {} - {}", correlationId, stepNumberText, progressMessage);
    }
    
    /**
     * Log step progress with enhanced context
     */
    public void logStepProgress(StepContextEnhancer.EnhancedStepContext enhancedContext, String progressMessage) {
        String stepNumberText = enhancedContext.getStepNumberText();
        
        log.debug("[CORRELATION:{}] {} - {}", 
                enhancedContext.getCorrelationId(), stepNumberText, progressMessage);
    }
    
    /**
     * Log step data sharing with dynamic step number
     */
    public void logStepDataSharing(String correlationId, String stepName, String dataType, Object data) {
        int stepNumber = stepNumberingService.getStepNumber(stepName);
        String stepNumberText = stepNumberingService.getStepNumberText(stepName);
        String nextStep = stepNumberingService.getNextStep(stepName);
        
        if (nextStep != null) {
            log.info("[CORRELATION:{}] {} shared {} data for next step: {}", 
                    correlationId, stepNumberText, dataType, nextStep);
        } else {
            log.info("[CORRELATION:{}] {} shared {} data (Final Step)", 
                    correlationId, stepNumberText, dataType);
        }
    }
    
    /**
     * Log step data sharing with enhanced context
     */
    public void logStepDataSharing(StepContextEnhancer.EnhancedStepContext enhancedContext, String dataType, Object data) {
        String stepNumberText = enhancedContext.getStepNumberText();
        String nextStep = stepNumberingService.getNextStep(enhancedContext.getStepName());
        
        if (nextStep != null) {
            log.info("[CORRELATION:{}] {} shared {} data for next step: {}", 
                    enhancedContext.getCorrelationId(), stepNumberText, dataType, nextStep);
        } else {
            log.info("[CORRELATION:{}] {} shared {} data (Final Step)", 
                    enhancedContext.getCorrelationId(), stepNumberText, dataType);
        }
    }
    
    /**
     * Log overall process progress
     */
    public void logProcessProgress(String correlationId, String currentStepName, int completedSteps, int totalSteps) {
        int currentStepNumber = stepNumberingService.getStepNumber(currentStepName);
        double progressPercentage = (double) completedSteps / totalSteps * 100;
        
        log.info("[CORRELATION:{}] Process Progress: {}/{} steps completed ({}%) - Currently executing: {}", 
                correlationId, completedSteps, totalSteps, String.format("%.1f", progressPercentage), 
                stepNumberingService.getStepNumberText(currentStepName));
    }
    
    /**
     * Log execution order summary
     */
    public void logExecutionOrderSummary(String correlationId) {
        String executionOrderSummary = stepNumberingService.getExecutionOrderSummary();
        log.info("[CORRELATION:{}] {}", correlationId, executionOrderSummary);
    }
    
    /**
     * Log step dependency information
     */
    public void logStepDependencies(String correlationId, String stepName) {
        String previousStep = stepNumberingService.getPreviousStep(stepName);
        String nextStep = stepNumberingService.getNextStep(stepName);
        int stepNumber = stepNumberingService.getStepNumber(stepName);
        
        log.debug("[CORRELATION:{}] Step {} dependencies - Previous: {}, Next: {}", 
                correlationId, stepNumber, 
                previousStep != null ? stepNumberingService.getStepNumberText(previousStep) : "None",
                nextStep != null ? stepNumberingService.getStepNumberText(nextStep) : "None");
    }
}


