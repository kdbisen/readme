package com.banking.onboarding.service;

import com.banking.onboarding.constants.OnboardingConstants.*;
import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.repository.OnboardingProcessRepository;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutionEngine;
import com.banking.onboarding.step.StepResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Document Verification Service
 * Handles the complete document verification process with 5 steps
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentVerificationService {

    private final GenericStepExecutionEngine stepExecutionEngine;
    private final OnboardingProcessRepository processRepository;

    /**
     * Execute complete document verification process
     */
    public OnboardingProcess executeDocumentVerificationProcess(String documentData, String requestType, String correlationId) {
        String actualCorrelationId = correlationId != null ? correlationId : 
            ProcessIdPrefixes.CORRELATION + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        String processId = ProcessIdPrefixes.PROCESS + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("[CORRELATION:{}] Starting document verification process with processId: {}", 
                actualCorrelationId, processId);
        
        // Create initial process record
        OnboardingProcess process = createInitialProcess(processId, actualCorrelationId, requestType, documentData);
        processRepository.save(process);
        
        try {
            // Create step context
            GenericStepContext context = GenericStepContext.create(actualCorrelationId, processId, documentData);
            
            // Execute document verification steps
            StepResult<Object> result = executeDocumentVerificationSteps(context);
            
            if (result.isSuccess()) {
                process.setStatus(OnboardingProcess.ProcessStatus.COMPLETED);
                process.setCompletedAt(LocalDateTime.now());
                log.info("[CORRELATION:{}] Document verification process completed successfully", actualCorrelationId);
            } else {
                process.setStatus(OnboardingProcess.ProcessStatus.FAILED);
                process.setErrorMessage(result.getErrorMessage());
                log.error("[CORRELATION:{}] Document verification process failed: {}", 
                        actualCorrelationId, result.getErrorMessage());
            }
            
        } catch (Exception e) {
            process.setStatus(OnboardingProcess.ProcessStatus.FAILED);
            process.setErrorMessage("Document verification process failed: " + e.getMessage());
            log.error("[CORRELATION:{}] Document verification process failed with exception: {}", 
                    actualCorrelationId, e.getMessage());
        }
        
        process.setUpdatedAt(LocalDateTime.now());
        processRepository.save(process);
        
        return process;
    }

    /**
     * Execute all document verification steps in sequence
     */
    private StepResult<Object> executeDocumentVerificationSteps(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing document verification steps", context.getCorrelationId());
        
        // Step 1: Document Upload
        StepResult<Object> uploadResult = stepExecutionEngine.executeStep(StepNames.DOCUMENT_UPLOAD, context);
        if (!uploadResult.isSuccess()) {
            return uploadResult;
        }
        
        // Step 2: Document Validation
        StepResult<Object> validationResult = stepExecutionEngine.executeStep(StepNames.DOCUMENT_VALIDATION, context);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }
        
        // Step 3: OCR Processing
        StepResult<Object> ocrResult = stepExecutionEngine.executeStep(StepNames.OCR_PROCESSING, context);
        if (!ocrResult.isSuccess()) {
            return ocrResult;
        }
        
        // Step 4: Compliance Check
        StepResult<Object> complianceResult = stepExecutionEngine.executeStep(StepNames.COMPLIANCE_CHECK, context);
        if (!complianceResult.isSuccess()) {
            return complianceResult;
        }
        
        // Step 5: Document Approval
        StepResult<Object> approvalResult = stepExecutionEngine.executeStep(StepNames.DOCUMENT_APPROVAL, context);
        if (!approvalResult.isSuccess()) {
            return approvalResult;
        }
        
        // Return final consolidated result
        Map<String, Object> finalResult = Map.of(
            "processType", "DOCUMENT_VERIFICATION",
            "totalSteps", 5,
            "completedSteps", 5,
            "finalStatus", "APPROVED",
            "stepResults", Map.of(
                "upload", uploadResult.getData(),
                "validation", validationResult.getData(),
                "ocr", ocrResult.getData(),
                "compliance", complianceResult.getData(),
                "approval", approvalResult.getData()
            )
        );
        
        return StepResult.success(finalResult, "DOCUMENT_VERIFICATION_COMPLETE", context.getCorrelationId());
    }

    /**
     * Create initial process record
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
        return process;
    }

    /**
     * Get process by ID
     */
    public OnboardingProcess getProcessById(String processId) {
        return processRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException(Messages.PROCESS_NOT_FOUND + processId));
    }
}
