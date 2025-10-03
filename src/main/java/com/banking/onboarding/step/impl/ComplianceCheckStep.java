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
 * Compliance Check Step - Step 4 of Document Verification Process
 * Performs compliance checks against regulatory requirements
 */
@Slf4j
@Component
public class ComplianceCheckStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing Compliance Check Step", context.getCorrelationId());

        try {
            // Get OCR result from previous step
            Object ocrResult = context.getStepResult(StepNames.OCR_PROCESSING);
            
            if (ocrResult == null) {
                return StepResult.failure("No OCR data available from previous step", getStepName(), context.getCorrelationId());
            }

            // Simulate compliance checking
            Map<String, Object> complianceResult = Map.of(
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

            // Store result in context for next step
            context.addStepResult(getStepName(), complianceResult);

            log.info("[CORRELATION:{}] Compliance check completed successfully. Risk level: {}", 
                    context.getCorrelationId(), complianceResult.get("riskAssessment"));

            return StepResult.success(complianceResult, getStepName(), context.getCorrelationId());

        } catch (Exception e) {
            log.error("[CORRELATION:{}] Compliance check failed: {}", context.getCorrelationId(), e.getMessage());
            return StepResult.failure("Compliance check failed: " + e.getMessage(), getStepName(), context.getCorrelationId());
        }
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
        return context.getStepResult(StepNames.OCR_PROCESSING) != null;
    }
}
