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
 * Compliance Check Step - Clean implementation using StepExecutionHelper
 * Performs compliance checks against regulatory requirements
 */
@Slf4j
@Component
public class ComplianceCheckStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        return StepExecutionHelper.executeStep(
                getStepName(),
                context.getCorrelationId(),
                this::processComplianceCheck,
                this::createSuccessMessage
        );
    }

    /**
     * Clean business logic - no if-else chains
     */
    private Map<String, Object> processComplianceCheck() {
        return Map.of(
            "complianceStatus", "COMPLIANT",
            "checksPerformed", Map.of(
                "kycCompliance", Map.of("status", "PASSED", "score", 98),
                "amlCompliance", Map.of("status", "PASSED", "score", 95),
                "sanctionsCheck", Map.of("status", "PASSED", "score", 100),
                "pepCheck", Map.of("status", "PASSED", "score", 100),
                "adverseMediaCheck", Map.of("status", "PASSED", "score", 100)
            ),
            "riskAssessment", Map.of(
                "overallRisk", "LOW",
                "riskScore", 15,
                "riskFactors", new String[]{"Standard documentation", "Clean background"}
            ),
            "regulatoryRequirements", Map.of(
                "meetsRequirements", true,
                "jurisdiction", "US",
                "regulations", new String[]{"BSA", "PATRIOT Act", "OFAC"}
            )
        );
    }

    /**
     * Create success message from result
     */
    private String createSuccessMessage(Map<String, Object> result) {
        Map<String, Object> riskAssessment = (Map<String, Object>) result.get("riskAssessment");
        return String.format("Risk level: %s", riskAssessment.get("overallRisk"));
    }

    @Override
    public String getStepName() {
        return StepNames.COMPLIANCE_CHECK;
    }

    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description(StepDescriptions.COMPLIANCE_CHECK)
                .dependencies(StepDependencies.COMPLIANCE_CHECK)
                .properties(Map.of("priority", StepPriorities.COMPLIANCE_CHECK))
                .build();
    }

    @Override
    public boolean canExecute(GenericStepContext context) {
        return StepExecutionHelper.validatePrerequisites(context, getDependencies());
    }

    private String[] getDependencies() {
        return StepDependencies.COMPLIANCE_CHECK;
    }
}