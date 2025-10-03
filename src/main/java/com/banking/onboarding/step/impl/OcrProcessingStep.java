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
 * OCR Processing Step - Step 3 of Document Verification Process
 * Extracts text and data from documents using OCR technology
 */
@Slf4j
@Component
public class OcrProcessingStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing OCR Processing Step", context.getCorrelationId());

        try {
            // Get validation result from previous step
            Object validationResult = context.getStepResult(StepNames.DOCUMENT_VALIDATION);
            
            if (validationResult == null) {
                return StepResult.failure("No validation data available from previous step", getStepName(), context.getCorrelationId());
            }

            // Simulate OCR processing
            Map<String, Object> ocrResult = Map.of(
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

            // Store result in context for next step
            context.addStepResult(getStepName(), ocrResult);

            log.info("[CORRELATION:{}] OCR processing completed successfully. Overall confidence: {}", 
                    context.getCorrelationId(), ocrResult.get("overallConfidence"));

            return StepResult.success(ocrResult, getStepName(), context.getCorrelationId());

        } catch (Exception e) {
            log.error("[CORRELATION:{}] OCR processing failed: {}", context.getCorrelationId(), e.getMessage());
            return StepResult.failure("OCR processing failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
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
        return context.getStepResult(StepNames.DOCUMENT_VALIDATION) != null;
    }
}
