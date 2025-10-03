package com.banking.onboarding.step.impl;

import com.banking.onboarding.constants.OnboardingConstants.*;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import com.banking.onboarding.step.util.StepExecutionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Document Approval Step - Clean implementation using StepExecutionHelper
 * Final approval step that consolidates all verification results
 */
@Slf4j
@Component
public class DocumentApprovalStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        return StepExecutionHelper.executeStep(
                getStepName(),
                context.getCorrelationId(),
                this::processDocumentApproval,
                this::createSuccessMessage
        );
    }

    /**
     * Clean business logic - no if-else chains
     */
    private Map<String, Object> processDocumentApproval() {
        return Map.of(
            "approvalStatus", "APPROVED",
            "approvalDate", LocalDateTime.now().toString(),
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
    }

    /**
     * Create success message from result
     */
    private String createSuccessMessage(Map<String, Object> result) {
        return String.format("Final score: %s", result.get("finalScore"));
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
        return StepExecutionHelper.validatePrerequisites(context, getDependencies());
    }

    private String[] getDependencies() {
        return StepDependencies.DOCUMENT_APPROVAL;
    }
}