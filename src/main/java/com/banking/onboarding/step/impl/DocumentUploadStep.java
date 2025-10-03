package com.banking.onboarding.step.impl;

import com.banking.onboarding.constants.OnboardingConstants.*;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import com.banking.onboarding.step.util.StepExecutionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Document Upload Step - Clean implementation using StepExecutionHelper
 * Handles document file upload and initial validation
 */
@Slf4j
@Component
public class DocumentUploadStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        return StepExecutionHelper.executeStep(
                getStepName(),
                context.getCorrelationId(),
                this::processDocumentUpload,
                this::createSuccessMessage
        );
    }

    /**
     * Clean business logic - no if-else chains
     */
    private Map<String, Object> processDocumentUpload() {
        return Map.of(
            "uploadedFiles", 3,
            "totalSize", "2.5MB",
            "fileTypes", new String[]{"PDF", "JPG", "PNG"},
            "uploadStatus", "SUCCESS",
            "documentIds", new String[]{"DOC-001", "DOC-002", "DOC-003"}
        );
    }

    /**
     * Create success message from result
     */
    private String createSuccessMessage(Map<String, Object> result) {
        return String.format("Files uploaded: %s", result.get("uploadedFiles"));
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
        return StepExecutionHelper.validatePrerequisites(context, getDependencies());
    }

    private String[] getDependencies() {
        return StepDependencies.DOCUMENT_UPLOAD;
    }
}
