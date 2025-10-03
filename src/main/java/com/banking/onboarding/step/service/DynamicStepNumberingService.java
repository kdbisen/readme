package com.banking.onboarding.step.service;

import com.banking.onboarding.constants.OnboardingConstants;
import com.banking.onboarding.step.config.StepConfigurationLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamic Step Numbering Service
 * Automatically determines step numbers based on execution order
 * Eliminates hardcoded step numbers and makes the system future-proof
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicStepNumberingService {
    
    private final StepConfigurationLoader stepConfigurationLoader;
    
    // Cache for step numbers to avoid recalculation
    private final Map<String, Integer> stepNumberCache = new ConcurrentHashMap<>();
    
    // Cache for execution order to avoid recalculation
    private volatile List<String> cachedExecutionOrder = null;
    
    /**
     * Get the dynamic step number for a given step name
     * Returns the position in the execution order (1-based)
     */
    public int getStepNumber(String stepName) {
        // Check cache first
        Integer cachedNumber = stepNumberCache.get(stepName);
        if (cachedNumber != null) {
            return cachedNumber;
        }
        
        // Calculate step number based on execution order
        List<String> executionOrder = getExecutionOrder();
        int stepNumber = executionOrder.indexOf(stepName) + 1; // 1-based numbering
        
        if (stepNumber == 0) {
            // Step not found in execution order, use priority-based numbering
            stepNumber = getPriorityBasedStepNumber(stepName);
            log.warn("Step {} not found in execution order, using priority-based number: {}", stepName, stepNumber);
        }
        
        // Cache the result
        stepNumberCache.put(stepName, stepNumber);
        
        log.debug("Step {} assigned dynamic number: {}", stepName, stepNumber);
        return stepNumber;
    }
    
    /**
     * Get the execution order of steps
     */
    public List<String> getExecutionOrder() {
        if (cachedExecutionOrder != null) {
            return cachedExecutionOrder;
        }
        
        List<com.banking.onboarding.step.StepConfig> stepConfigs = stepConfigurationLoader.loadStepConfigurations();
        
        if (stepConfigurationLoader.isPriorityBasedExecution()) {
            // Sort by priority (lower number = higher priority = earlier execution)
            cachedExecutionOrder = stepConfigs.stream()
                    .sorted((config1, config2) -> {
                        Map<String, Object> props1 = config1.getProperties();
                        Map<String, Object> props2 = config2.getProperties();
                        
                        int priority1 = (Integer) props1.getOrDefault("priority", 99);
                        int priority2 = (Integer) props2.getOrDefault("priority", 99);
                        
                        return Integer.compare(priority1, priority2);
                    })
                    .map(com.banking.onboarding.step.StepConfig::getStepName)
                    .toList();
            
            log.debug("Using priority-based execution order: {}", cachedExecutionOrder);
        } else {
            // Use order-based execution (original behavior)
            cachedExecutionOrder = stepConfigs.stream()
                    .map(com.banking.onboarding.step.StepConfig::getStepName)
                    .toList();
            
            log.debug("Using order-based execution order: {}", cachedExecutionOrder);
        }
        
        return cachedExecutionOrder;
    }
    
    /**
     * Get priority-based step number as fallback
     */
    private int getPriorityBasedStepNumber(String stepName) {
        var stepDefinition = stepConfigurationLoader.getStepDefinition(stepName);
        if (stepDefinition != null) {
            return stepDefinition.priority;
        }
        
        // Default fallback
        log.warn("No step definition found for {}, using default number 99", stepName);
        return 99;
    }
    
    /**
     * Get step number with descriptive text
     * Returns formatted string like "Step 1" or "Step 2"
     */
    public String getStepNumberText(String stepName) {
        int stepNumber = getStepNumber(stepName);
        return String.format("Step %d", stepNumber);
    }
    
    /**
     * Get step number with description
     * Returns formatted string like "Step 1: XML to JSON Transformation"
     */
    public String getStepNumberWithDescription(String stepName) {
        int stepNumber = getStepNumber(stepName);
        var stepDefinition = stepConfigurationLoader.getStepDefinition(stepName);
        
        if (stepDefinition != null) {
            return String.format("Step %d: %s", stepNumber, stepDefinition.description);
        }
        
        return String.format("Step %d: %s", stepNumber, stepName);
    }
    
    /**
     * Get total number of steps in execution order
     */
    public int getTotalSteps() {
        return getExecutionOrder().size();
    }
    
    /**
     * Check if a step is the first step
     */
    public boolean isFirstStep(String stepName) {
        return getStepNumber(stepName) == 1;
    }
    
    /**
     * Check if a step is the last step
     */
    public boolean isLastStep(String stepName) {
        return getStepNumber(stepName) == getTotalSteps();
    }
    
    /**
     * Get the next step in execution order
     */
    public String getNextStep(String currentStepName) {
        List<String> executionOrder = getExecutionOrder();
        int currentIndex = executionOrder.indexOf(currentStepName);
        
        if (currentIndex >= 0 && currentIndex < executionOrder.size() - 1) {
            return executionOrder.get(currentIndex + 1);
        }
        
        return null; // No next step
    }
    
    /**
     * Get the previous step in execution order
     */
    public String getPreviousStep(String currentStepName) {
        List<String> executionOrder = getExecutionOrder();
        int currentIndex = executionOrder.indexOf(currentStepName);
        
        if (currentIndex > 0) {
            return executionOrder.get(currentIndex - 1);
        }
        
        return null; // No previous step
    }
    
    /**
     * Clear cache (useful for testing or when step configuration changes)
     */
    public void clearCache() {
        stepNumberCache.clear();
        cachedExecutionOrder = null;
        log.info("Step numbering cache cleared");
    }
    
    /**
     * Refresh cache (recalculate step numbers)
     */
    public void refreshCache() {
        clearCache();
        log.info("Step numbering cache refreshed");
    }
    
    /**
     * Get execution order summary for logging
     */
    public String getExecutionOrderSummary() {
        List<String> executionOrder = getExecutionOrder();
        StringBuilder summary = new StringBuilder();
        summary.append("Execution Order: ");
        
        for (int i = 0; i < executionOrder.size(); i++) {
            String stepName = executionOrder.get(i);
            int stepNumber = i + 1;
            summary.append(String.format("%d.%s", stepNumber, stepName));
            
            if (i < executionOrder.size() - 1) {
                summary.append(" -> ");
            }
        }
        
        return summary.toString();
    }
    
    /**
     * Log execution order for debugging
     */
    public void logExecutionOrder() {
        log.info("=== DYNAMIC STEP EXECUTION ORDER ===");
        log.info("Total Steps: {}", getTotalSteps());
        log.info("Execution Order: {}", getExecutionOrderSummary());
        
        List<String> executionOrder = getExecutionOrder();
        for (int i = 0; i < executionOrder.size(); i++) {
            String stepName = executionOrder.get(i);
            int stepNumber = i + 1;
            var stepDefinition = stepConfigurationLoader.getStepDefinition(stepName);
            
            if (stepDefinition != null) {
                log.info("  {}: {} (Priority: {}, Category: {}, Critical: {})", 
                        stepNumber, stepName, stepDefinition.priority, 
                        stepDefinition.category, stepDefinition.critical);
            } else {
                log.info("  {}: {} (No definition found)", stepNumber, stepName);
            }
        }
        log.info("=====================================");
    }
}


