package com.banking.onboarding.service;

import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.model.ProcessStep;
import com.banking.onboarding.repository.OnboardingProcessRepository;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutionEngine;
import com.banking.onboarding.step.StepResult;
import com.banking.onboarding.step.config.StepConfigurationLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Generic Onboarding Flow Service - Uses generic step pattern with flexible data sharing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenericOnboardingFlowService {
    
    private final GenericStepExecutionEngine stepExecutionEngine;
    private final StepConfigurationLoader stepConfigurationLoader;
    private final OnboardingProcessRepository processRepository;
    private final CorrelationIdService correlationIdService;
    private final CorrelationIdStrategyService correlationIdStrategyService;
    
    /**
     * Execute complete onboarding flow using generic step pattern - SYNCHRONOUS
     * Now includes correlation ID strategy handling
     */
    public OnboardingProcess executeCompleteFlow(String xmlData, String requestType, String correlationId) {
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        // Handle correlation ID strategy
        CorrelationIdStrategyService.CorrelationIdStrategyResult strategyResult = 
            correlationIdStrategyService.handleCorrelationIdStrategy(actualCorrelationId, requestType);
        
        if (!strategyResult.isShouldProceed()) {
            log.warn("[CORRELATION:{}] Request rejected due to correlation ID strategy: {}", 
                    actualCorrelationId, strategyResult.getMessage());
            
            // Return the latest existing process or create a rejected process
            return strategyResult.getLatestProcess()
                    .orElse(createRejectedProcess(actualCorrelationId, requestType, strategyResult.getMessage()));
        }
        
        String processId = "PROC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("[CORRELATION:{}] Starting generic onboarding flow with processId: {} - Strategy: {}", 
                actualCorrelationId, processId, strategyResult.getStrategy());
        
        // Create initial process record
        OnboardingProcess process = createInitialProcess(processId, actualCorrelationId, requestType, xmlData);
        processRepository.save(process);
        
        // Load step configurations
        List<com.banking.onboarding.step.StepConfig> stepConfigs = stepConfigurationLoader.loadStepConfigurations();
        
        // Determine execution order based on configuration
        List<String> stepNames;
        if (stepConfigurationLoader.isPriorityBasedExecution()) {
            // Sort steps by priority (lower number = higher priority)
            stepNames = stepConfigs.stream()
                    .sorted((config1, config2) -> {
                        // Get priority from step properties
                        Map<String, Object> props1 = config1.getProperties();
                        Map<String, Object> props2 = config2.getProperties();
                        
                        int priority1 = (Integer) props1.getOrDefault("priority", 99);
                        int priority2 = (Integer) props2.getOrDefault("priority", 99);
                        
                        return Integer.compare(priority1, priority2);
                    })
                    .map(com.banking.onboarding.step.StepConfig::getStepName)
                    .toList();
            
            log.info("[CORRELATION:{}] Using priority-based execution order: {}", actualCorrelationId, stepNames);
        } else {
            // Use order-based execution (original behavior)
            stepNames = stepConfigs.stream()
                    .map(com.banking.onboarding.step.StepConfig::getStepName)
                    .toList();
            
            log.info("[CORRELATION:{}] Using order-based execution order: {}", actualCorrelationId, stepNames);
        }
        
        // Create generic step context - can handle any data type
        GenericStepContext context = GenericStepContext.create(actualCorrelationId, processId, xmlData);
        
        // Execute steps using the generic step execution engine - SYNCHRONOUS
        StepResult<Map<String, Object>> stepResult = stepExecutionEngine.executeSteps(stepNames, context);
        
        if (stepResult.isSuccess()) {
            return finalizeProcessSuccess(process, stepResult.getData(), context);
        } else {
            return finalizeProcessFailure(process, stepResult.getErrorMessage());
        }
    }
    
    /**
     * Execute individual step for testing - SYNCHRONOUS
     */
    public StepResult<Object> executeStep(String stepName, Object inputData, String correlationId) {
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        GenericStepContext context = GenericStepContext.create(actualCorrelationId, 
                "TEST-" + UUID.randomUUID().toString().substring(0, 8), inputData);
        
        return stepExecutionEngine.executeStep(stepName, context);
    }
    
    /**
     * Get process by ID
     */
    public OnboardingProcess getProcessById(String processId) {
        return processRepository.findById(processId)
                .orElseThrow(() -> new com.banking.onboarding.exception.ProcessException(
                        "Process not found with ID: " + processId));
    }
    
    /**
     * Create initial process
     */
    private OnboardingProcess createInitialProcess(String processId, String correlationId, String requestType, String inputData) {
        OnboardingProcess process = new OnboardingProcess();
        process.setProcessId(processId);
        process.setCorrelationId(correlationId);
        process.setRequestType(requestType);
        process.setInputData(inputData);
        process.setStatus(OnboardingProcess.ProcessStatus.IN_PROGRESS);
        process.setCreatedAt(LocalDateTime.now());
        process.setUpdatedAt(LocalDateTime.now());
        process.setSteps(new java.util.ArrayList<>());
        return process;
    }
    
    /**
     * Finalize process on success with generic data sharing
     */
    private OnboardingProcess finalizeProcessSuccess(OnboardingProcess process, Map<String, Object> results, GenericStepContext context) {
        process.setStatus(OnboardingProcess.ProcessStatus.COMPLETED);
        process.setCompletedAt(LocalDateTime.now());
        process.setUpdatedAt(LocalDateTime.now());
        
        // Convert step results to process steps with data type information
        results.forEach((stepName, result) -> {
            ProcessStep step = new ProcessStep();
            step.setStepId(stepName);
            step.setStepName(stepName);
            step.setStatus(ProcessStep.StepStatus.COMPLETED);
            step.setInputData(process.getInputData());
            
            // Store result with type information
            String resultData = result != null ? 
                    String.format("%s [Type: %s]", result.toString(), result.getClass().getSimpleName()) : 
                    "null";
            step.setOutputData(resultData);
            
            step.setStartedAt(LocalDateTime.now());
            step.setCompletedAt(LocalDateTime.now());
            step.setDurationMs(0); // Will be calculated by monitoring service
            process.getSteps().add(step);
            
            log.info("[CORRELATION:{}] Step {} completed with data type: {}", 
                    process.getCorrelationId(), stepName, result != null ? result.getClass().getSimpleName() : "null");
        });
        
        log.info("[CORRELATION:{}] Process {} completed successfully with {} steps", 
                process.getCorrelationId(), process.getProcessId(), results.size());
        
        return processRepository.save(process);
    }
    
    /**
     * Finalize process on failure
     */
    private OnboardingProcess finalizeProcessFailure(OnboardingProcess process, String errorMessage) {
        process.setStatus(OnboardingProcess.ProcessStatus.FAILED);
        process.setErrorMessage(errorMessage);
        process.setCompletedAt(LocalDateTime.now());
        process.setUpdatedAt(LocalDateTime.now());
        
        log.error("[CORRELATION:{}] Process {} failed: {}", process.getCorrelationId(), process.getProcessId(), errorMessage);
        
        return processRepository.save(process);
    }
    
    /**
     * Create rejected process record
     */
    private OnboardingProcess createRejectedProcess(String correlationId, String requestType, String reason) {
        String processId = "PROC-REJECTED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        OnboardingProcess process = new OnboardingProcess();
        process.setProcessId(processId);
        process.setCorrelationId(correlationId);
        process.setRequestType(requestType);
        process.setInputData("Request rejected due to correlation ID strategy");
        process.setStatus(OnboardingProcess.ProcessStatus.CANCELLED);
        process.setErrorMessage("Request rejected: " + reason);
        process.setCreatedAt(LocalDateTime.now());
        process.setUpdatedAt(LocalDateTime.now());
        process.setCompletedAt(LocalDateTime.now());
        process.setSteps(new java.util.ArrayList<>());
        
        return processRepository.save(process);
    }
}
