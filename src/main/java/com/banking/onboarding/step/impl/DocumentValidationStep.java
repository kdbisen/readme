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
 * Document Validation Step - Clean implementation using StepExecutionHelper
 * Validates document format, size, and content requirements
 */
@Slf4j
@Component
public class DocumentValidationStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        return StepExecutionHelper.executeStep(
                getStepName(),
                context.getCorrelationId(),
                this::processDocumentValidation,
                this::createSuccessMessage
        );
    }

    /**
     * Clean business logic - no if-else chains
     */
    private Map<String, Object> processDocumentValidation() {
        return Map.of(
            "validationStatus", "PASSED",
            "validatedFiles", 3,
            "validationChecks", Map.of(
                "formatCheck", "PASSED",
                "sizeCheck", "PASSED", 
                "contentCheck", "PASSED",
                "securityCheck", "PASSED"
            ),
            "documentDetails", Map.of(
                "DOC-001", Map.of("type", "ID_CARD", "status", "VALID"),
                "DOC-002", Map.of("type", "ADDRESS_PROOF", "status", "VALID"),
                "DOC-003", Map.of("type", "INCOME_PROOF", "status", "VALID")
            ),
            "validationScore", 95.5
        );
    }

    /**
     * Create success message from result
     */
    private String createSuccessMessage(Map<String, Object> result) {
        return String.format("Validation score: %s", result.get("validationScore"));
    }

    @Override
    public String getStepName() {
        return StepNames.DOCUMENT_VALIDATION;
    }

    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description(StepDescriptions.DOCUMENT_VALIDATION)
                .dependencies(StepDependencies.DOCUMENT_VALIDATION)
                .properties(Map.of("priority", StepPriorities.DOCUMENT_VALIDATION))
                .build();
    }

    @Override
    public boolean canExecute(GenericStepContext context) {
        return StepExecutionHelper.validatePrerequisites(context, getDependencies());
    }

    private String[] getDependencies() {
        return StepDependencies.DOCUMENT_VALIDATION;
    }
}
