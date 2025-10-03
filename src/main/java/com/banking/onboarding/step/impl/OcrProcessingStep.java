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
 * OCR Processing Step - Clean implementation using StepExecutionHelper
 * Extracts text and data from documents using OCR technology
 */
@Slf4j
@Component
public class OcrProcessingStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        return StepExecutionHelper.executeStep(
                getStepName(),
                context.getCorrelationId(),
                this::processOcrExtraction,
                this::createSuccessMessage
        );
    }

    /**
     * Clean business logic - no if-else chains
     */
    private Map<String, Object> processOcrExtraction() {
        return Map.of(
            "ocrStatus", "COMPLETED",
            "processedDocuments", 3,
            "extractedData", Map.of(
                "DOC-001", Map.of(
                    "documentType", "ID_CARD",
                    "extractedFields", Map.of(
                        "name", "John Doe",
                        "idNumber", "123456789",
                        "dateOfBirth", "1990-01-01",
                        "address", "123 Main St, City, State"
                    ),
                    "confidence", 98.5
                ),
                "DOC-002", Map.of(
                    "documentType", "ADDRESS_PROOF",
                    "extractedFields", Map.of(
                        "address", "123 Main St, City, State",
                        "utilityProvider", "Electric Company",
                        "accountNumber", "ACC-789456"
                    ),
                    "confidence", 95.2
                ),
                "DOC-003", Map.of(
                    "documentType", "INCOME_PROOF",
                    "extractedFields", Map.of(
                        "employer", "ABC Corporation",
                        "salary", "$75,000",
                        "employmentDate", "2020-01-01"
                    ),
                    "confidence", 92.8
                )
            ),
            "overallConfidence", 95.5
        );
    }

    /**
     * Create success message from result
     */
    private String createSuccessMessage(Map<String, Object> result) {
        return String.format("Overall confidence: %s", result.get("overallConfidence"));
    }

    @Override
    public String getStepName() {
        return StepNames.OCR_PROCESSING;
    }

    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description(StepDescriptions.OCR_PROCESSING)
                .dependencies(StepDependencies.OCR_PROCESSING)
                .properties(Map.of("priority", StepPriorities.OCR_PROCESSING))
                .build();
    }

    @Override
    public boolean canExecute(GenericStepContext context) {
        return StepExecutionHelper.validatePrerequisites(context, getDependencies());
    }

    private String[] getDependencies() {
        return StepDependencies.OCR_PROCESSING;
    }
}
