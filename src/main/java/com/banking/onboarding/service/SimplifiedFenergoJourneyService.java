package com.banking.onboarding.service;

import com.banking.onboarding.model.*;
import com.banking.onboarding.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Simplified Fenergo Journey Orchestration Service
 * 
 * Flow:
 * 1. Apigee XML to JSON transformation
 * 2. Fenergo Entity Create via proxy
 * 3. Fenergo Journey Info retrieval
 * 4. Fenergo Journey Initiate
 * 5. Fenergo Journey Details retrieval
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimplifiedFenergoJourneyService {

    private final ApigeeService apigeeService;
    private final FenergoEntityService fenergoEntityService;
    private final FenergoJourneyService fenergoJourneyService;
    private final ProcessRepository processRepository;
    private final StepRepository stepRepository;
    private final CorrelationIdService correlationIdService;

    /**
     * Process Fenergo journey asynchronously
     */
    @Async("functionalTaskExecutor")
    public void processFenergoJourneyAsync(String payload, RequestType requestType, 
                                         String processId, String correlationId) {
        
        log.info("[CORRELATION:{}] Starting simplified Fenergo journey - ProcessId: {}, RequestType: {}", 
                correlationId, processId, requestType);
        
        // Create main process record
        OnboardingProcess process = createProcess(processId, correlationId, requestType, payload);
        
        try {
            // Step 1: Apigee XML to JSON transformation
            Step step1 = createStep(processId, correlationId, "APIGEE_TRANSFORM", 1);
            ApigeeService.ApigeeResponse apigeeResponse = apigeeService.transformXmlToJson(payload, processId);
            updateStep(step1, apigeeResponse.isSuccess() ? "COMPLETED" : "FAILED", 
                      apigeeResponse.getErrorMessage(), apigeeResponse.getDuration(), apigeeResponse.getJsonPayload());
            
            if (!apigeeResponse.isSuccess()) {
                handleProcessFailure(processId, correlationId, "Apigee transformation failed", apigeeResponse.getErrorMessage());
                return;
            }
            
            // Step 2: Fenergo Entity Create
            Step step2 = createStep(processId, correlationId, "ENTITY_CREATE", 2);
            FenergoEntityService.FenergoEntityResponse entityResponse = fenergoEntityService.createEntity(
                    apigeeResponse.getJsonPayload(), processId);
            updateStep(step2, entityResponse.isSuccess() ? "COMPLETED" : "FAILED", 
                      entityResponse.getErrorMessage(), entityResponse.getDuration(), 
                      Map.of("entityId", entityResponse.getEntityId(), "clientId", entityResponse.getClientId()));
            
            if (!entityResponse.isSuccess()) {
                handleProcessFailure(processId, correlationId, "Entity creation failed", entityResponse.getErrorMessage());
                return;
            }
            
            // Step 3: Fenergo Journey Info
            Step step3 = createStep(processId, correlationId, "JOURNEY_INFO", 3);
            FenergoJourneyService.FenergoJourneyResponse journeyInfoResponse = fenergoJourneyService.getJourneyInfo(
                    entityResponse.getClientId(), processId);
            updateStep(step3, journeyInfoResponse.isSuccess() ? "COMPLETED" : "FAILED", 
                      journeyInfoResponse.getErrorMessage(), journeyInfoResponse.getDuration(),
                      Map.of("journeyId", journeyInfoResponse.getJourneyId()));
            
            if (!journeyInfoResponse.isSuccess()) {
                handleProcessFailure(processId, correlationId, "Journey info retrieval failed", journeyInfoResponse.getErrorMessage());
                return;
            }
            
            // Step 4: Fenergo Journey Initiate
            Step step4 = createStep(processId, correlationId, "JOURNEY_INITIATE", 4);
            FenergoJourneyService.FenergoJourneyResponse journeyInitiateResponse = fenergoJourneyService.initiateJourney(
                    journeyInfoResponse.getJourneyId(), entityResponse.getClientId(), processId);
            updateStep(step4, journeyInitiateResponse.isSuccess() ? "COMPLETED" : "FAILED", 
                      journeyInitiateResponse.getErrorMessage(), journeyInitiateResponse.getDuration(),
                      Map.of("journeyStatus", journeyInitiateResponse.getJourneyStatus()));
            
            if (!journeyInitiateResponse.isSuccess()) {
                handleProcessFailure(processId, correlationId, "Journey initiation failed", journeyInitiateResponse.getErrorMessage());
                return;
            }
            
            // Step 5: Fenergo Journey Details
            Step step5 = createStep(processId, correlationId, "JOURNEY_DETAILS", 5);
            FenergoJourneyService.FenergoJourneyResponse journeyDetailsResponse = fenergoJourneyService.getJourneyDetails(
                    journeyInitiateResponse.getJourneyId(), processId);
            updateStep(step5, journeyDetailsResponse.isSuccess() ? "COMPLETED" : "FAILED", 
                      journeyDetailsResponse.getErrorMessage(), journeyDetailsResponse.getDuration(),
                      journeyDetailsResponse.getJourneyData());
            
            if (!journeyDetailsResponse.isSuccess()) {
                handleProcessFailure(processId, correlationId, "Journey details retrieval failed", journeyDetailsResponse.getErrorMessage());
                return;
            }
            
            // Process Completion
            handleProcessCompletion(processId, correlationId, journeyDetailsResponse);
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Unexpected error in simplified Fenergo journey - ProcessId: {}", 
                    correlationId, processId, e);
            handleProcessFailure(processId, correlationId, "Unexpected error", e.getMessage());
        }
    }

    /**
     * Create initial process record
     */
    private OnboardingProcess createProcess(String processId, String correlationId, RequestType requestType, String payload) {
        OnboardingProcess process = OnboardingProcess.builder()
                .processId(processId)
                .correlationId(correlationId)
                .requestType(requestType.name())
                .status("PROCESSING")
                .currentStep("APIGEE_TRANSFORM")
                .message("Fenergo journey processing initiated")
                .payload(payload)
                .resultData(new HashMap<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        processRepository.save(process);
        log.info("[CORRELATION:{}] Created process record - ProcessId: {}", correlationId, processId);
        
        return process;
    }

    /**
     * Create step record
     */
    private Step createStep(String processId, String correlationId, String stepName, int stepOrder) {
        Step step = Step.builder()
                .processId(processId)
                .correlationId(correlationId)
                .stepName(stepName)
                .stepOrder(stepOrder)
                .status("IN_PROGRESS")
                .message("Step started")
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        stepRepository.save(step);
        log.info("[CORRELATION:{}] Created step - ProcessId: {}, Step: {}", correlationId, processId, stepName);
        
        return step;
    }

    /**
     * Update step record
     */
    private void updateStep(Step step, String status, String errorMessage, long duration, Object outputData) {
        step.setStatus(status);
        step.setDurationMs(duration);
        step.setUpdatedAt(LocalDateTime.now());
        
        if ("COMPLETED".equals(status)) {
            step.setMessage("Step completed successfully");
            step.setCompletedAt(LocalDateTime.now());
            step.setOutputData(outputData != null ? outputData.toString() : null);
        } else {
            step.setMessage(errorMessage);
            step.setErrorMessage(errorMessage);
        }
        
        stepRepository.save(step);
        log.info("[CORRELATION:{}] Updated step - ProcessId: {}, Step: {}, Status: {}", 
                step.getCorrelationId(), step.getProcessId(), step.getStepName(), status);
    }

    /**
     * Handle process completion
     */
    private void handleProcessCompletion(String processId, String correlationId, 
                                       FenergoJourneyService.FenergoJourneyResponse journeyResponse) {
        log.info("[CORRELATION:{}] Processing completion - ProcessId: {}", correlationId, processId);
        
        Optional<OnboardingProcess> processOpt = processRepository.findByProcessId(processId);
        if (processOpt.isPresent()) {
            OnboardingProcess process = processOpt.get();
            process.setStatus("COMPLETED");
            process.setMessage("Journey processing completed successfully");
            process.setCompletedAt(LocalDateTime.now());
            process.setUpdatedAt(LocalDateTime.now());
            process.setResultData(journeyResponse.getJourneyData());
            
            processRepository.save(process);
            log.info("[CORRELATION:{}] Process completed successfully - ProcessId: {}", correlationId, processId);
        }
    }

    /**
     * Handle process failure
     */
    private void handleProcessFailure(String processId, String correlationId, String stepName, String errorMessage) {
        log.error("[CORRELATION:{}] Process failed - ProcessId: {}, Step: {}, Error: {}", 
                correlationId, processId, stepName, errorMessage);
        
        Optional<OnboardingProcess> processOpt = processRepository.findByProcessId(processId);
        if (processOpt.isPresent()) {
            OnboardingProcess process = processOpt.get();
            process.setStatus("FAILED");
            process.setMessage(errorMessage);
            process.setErrorMessage(errorMessage);
            process.setUpdatedAt(LocalDateTime.now());
            
            processRepository.save(process);
        }
        
        // Log error using application logger
        log.error("[CORRELATION:{}] Error in step {} - ProcessId: {}, Error: {}", 
                correlationId, stepName, processId, errorMessage);
        Log errorLog = Log.builder()
                .processId(processId)
                .correlationId(correlationId)
                .logType("ERROR")
                .level("ERROR")
                .message(errorMessage)
                .serviceName("SimplifiedFenergoJourneyService")
                .contextData(Map.of("step", stepName))
                .timestamp(LocalDateTime.now())
                .build();

        //logRepository.save(errorLog);
    }

    /**
     * Get process status
     */
    public ProcessStatusResponse getProcessStatus(String processId) {
        Optional<OnboardingProcess> processOpt = processRepository.findByProcessId(processId);
        if (!processOpt.isPresent()) {
            return null;
        }
        
        OnboardingProcess process = processOpt.get();
        List<Step> steps = stepRepository.findByProcessIdOrderByStepOrderAsc(processId);
        List<ProcessStatusResponse.StepInfo> stepInfos = new ArrayList<>();
        
        for (Step step : steps) {
            stepInfos.add(ProcessStatusResponse.StepInfo.builder()
                    .stepName(step.getStepName())
                    .stepOrder(step.getStepOrder())
                    .status(step.getStatus())
                    .message(step.getMessage())
                    .durationMs(step.getDurationMs())
                    .completedAt(step.getCompletedAt())
                    .build());
        }
        
        return ProcessStatusResponse.builder()
                .processId(process.getProcessId())
                .correlationId(process.getCorrelationId())
                .status(process.getStatus())
                .currentStep(process.getCurrentStep())
                .message(process.getMessage())
                .createdAt(process.getCreatedAt())
                .updatedAt(process.getUpdatedAt())
                .completedAt(process.getCompletedAt())
                .steps(stepInfos)
                .build();
    }
}
