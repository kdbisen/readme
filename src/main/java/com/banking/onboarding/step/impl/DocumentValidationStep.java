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
 * Document Validation Step - Step 2 of Document Verification Process
 * Validates document format, size, and content requirements
 */
@Slf4j
@Component
public class DocumentValidationStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing Document Validation Step", context.getCorrelationId());

        try {
            // Get upload result from previous step
            Object uploadResult = context.getStepResult(StepNames.DOCUMENT_UPLOAD);
            
            if (uploadResult == null) {
                return StepResult.failure("No upload data available from previous step", getStepName(), context.getCorrelationId());
            }

            // Simulate document validation processing
            Map<String, Object> validationResult = Map.of(
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

            // Store result in context for next step
            context.addStepResult(getStepName(), validationResult);

            log.info("[CORRELATION:{}] Document validation completed successfully. Score: {}", 
                    context.getCorrelationId(), validationResult.get("validationScore"));

            return StepResult.success(validationResult, getStepName(), context.getCorrelationId());

        } catch (Exception e) {
            log.error("[CORRELATION:{}] Document validation failed: {}", context.getCorrelationId(), e.getMessage());
            return StepResult.failure("Document validation failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
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
        return context.getStepResult(StepNames.DOCUMENT_UPLOAD) != null;
    }
}
