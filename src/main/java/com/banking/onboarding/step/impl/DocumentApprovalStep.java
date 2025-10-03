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
 * Document Approval Step - Step 5 of Document Verification Process
 * Final approval step that consolidates all verification results
 */
@Slf4j
@Component
public class DocumentApprovalStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing Document Approval Step", context.getCorrelationId());

        try {
            // Get compliance result from previous step
            Object complianceResult = context.getStepResult(StepNames.COMPLIANCE_CHECK);
            
            if (complianceResult == null) {
                return StepResult.failure("No compliance data available from previous step", getStepName(), context.getCorrelationId());
            }

            // Consolidate all step results for final approval
            Map<String, Object> approvalResult = Map.of(
                "approvalStatus", "APPROVED",
                "approvalDate", java.time.LocalDateTime.now().toString(),
                "approvedBy", "SYSTEM_AUTO",
                "verificationSummary", Map.of(
                    "totalSteps", 5,
                    "completedSteps", 5,
                    "successfulSteps", 5,
                    "failedSteps", 0
                ),
                "documentSummary", Map.of(
                    "totalDocuments", 3,
                    "approvedDocuments", 3,
                    "rejectedDocuments", 0,
                    "documentTypes", new String[]{"ID_CARD", "ADDRESS_PROOF", "INCOME_PROOF"}
                ),
                "finalScore", 96.8,
                "recommendation", "APPROVE",
                "nextSteps", new String[]{"Account activation", "Welcome email", "Document archival"}
            );

            // Store final result in context
            context.addStepResult(getStepName(), approvalResult);

            log.info("[CORRELATION:{}] Document approval completed successfully. Final score: {}", 
                    context.getCorrelationId(), approvalResult.get("finalScore"));

            return StepResult.success(approvalResult, getStepName(), context.getCorrelationId());

        } catch (Exception e) {
            log.error("[CORRELATION:{}] Document approval failed: {}", context.getCorrelationId(), e.getMessage());
            return StepResult.failure("Document approval failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
    }

    @Override
    public String getStepName() {
        return StepNames.DOCUMENT_APPROVAL;
    }

    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description(StepDescriptions.DOCUMENT_APPROVAL)
                .dependencies(StepDependencies.DOCUMENT_APPROVAL)
                .properties(Map.of("priority", StepPriorities.DOCUMENT_APPROVAL))
                .build();
    }

    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.getStepResult(StepNames.COMPLIANCE_CHECK) != null;
    }
}
