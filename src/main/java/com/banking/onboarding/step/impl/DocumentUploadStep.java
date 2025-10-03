package com.banking.onboarding.step.impl;

import com.banking.onboarding.constants.OnboardingConstants.*;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Document Upload Step - Step 1 of Document Verification Process
 * Handles document file upload and initial validation
 */
@Slf4j
@Component
public class DocumentUploadStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing Document Upload Step", context.getCorrelationId());

        try {
            // Get input data (document files)
            Object inputData = context.getInputData();
            
            if (inputData == null) {
                return StepResult.failure("No document data provided", getStepName(), context.getCorrelationId());
            }

            // Simulate document upload processing
            Map<String, Object> uploadResult = Map.of(
                "uploadedFiles", 3,
                "totalSize", "2.5MB",
                "fileTypes", new String[]{"PDF", "JPG", "PNG"},
                "uploadStatus", "SUCCESS",
                "documentIds", new String[]{"DOC-001", "DOC-002", "DOC-003"}
            );

            // Store result in context for next step
            context.addStepResult(getStepName(), uploadResult);

            log.info("[CORRELATION:{}] Document upload completed successfully. Files uploaded: {}", 
                    context.getCorrelationId(), uploadResult.get("uploadedFiles"));

            return StepResult.success(uploadResult, getStepName(), context.getCorrelationId());

        } catch (Exception e) {
            log.error("[CORRELATION:{}] Document upload failed: {}", context.getCorrelationId(), e.getMessage());
            return StepResult.failure("Document upload failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
    }

    @Override
    public String getStepName() {
        return StepNames.DOCUMENT_UPLOAD;
    }

    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description(StepDescriptions.DOCUMENT_UPLOAD)
                .dependencies(StepDependencies.DOCUMENT_UPLOAD)
                .properties(Map.of("priority", StepPriorities.DOCUMENT_UPLOAD))
                .build();
    }

    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.getInputData() != null;
    }
}
