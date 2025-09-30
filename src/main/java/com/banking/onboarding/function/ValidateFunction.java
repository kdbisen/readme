package com.banking.onboarding.function;

import com.banking.onboarding.context.ProcessingContext;
import com.banking.onboarding.model.EntityData;
import com.banking.onboarding.model.OnboardingProcess.ProcessStatus;
import com.banking.onboarding.service.EntityTransformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Validate EntityData for completeness and compliance
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateFunction implements ProcessingFunction<EntityData> {
    
    private final EntityTransformationService transformationService;
    
    @Override
    public ProcessingContext<EntityData> apply(ProcessingContext<EntityData> context) {
        log.info("Executing ValidateFunction for process: {}", context.getProcessId());
        
        try {
            EntityData entityData = context.getProcessedData();
            
            if (entityData == null) {
                throw new IllegalArgumentException("EntityData is null - transformation may have failed");
            }
            
            // Perform validation
            boolean isValid = transformationService.validateEntityData(entityData);
            
            if (!isValid) {
                throw new ValidationException("Entity data validation failed");
            }
            
            // Update context with validation results
            context.addResult("validationStatus", "SUCCESS");
            context.addResult("validationTimestamp", LocalDateTime.now());
            context.addResult("validationChecks", Map.of(
                "entityId", entityData.getEntityId() != null,
                "entityName", entityData.getEntityName() != null,
                "entityType", entityData.getEntityType() != null,
                "primaryContact", entityData.getPrimaryContact() != null,
                "riskProfile", entityData.getRiskProfile() != null,
                "complianceInfo", entityData.getComplianceInfo() != null
            ));
            
            // Add step data
            context.addStepData("validationCompleted", LocalDateTime.now());
            context.addStepData("validationPassed", true);
            
            // Update process status
            updateProcessStatus(context, ProcessStatus.VALIDATING);
            
            log.info("ValidateFunction completed successfully for process: {}", context.getProcessId());
            
        } catch (Exception e) {
            log.error("ValidateFunction failed for process: {}", context.getProcessId(), e);
            context.addStepData("validationFailed", LocalDateTime.now());
            context.addStepData("validationError", e.getMessage());
            throw new RuntimeException("Validation failed", e);
        }
        
        return context;
    }
    
    @Override
    public String getStepName() {
        return "VALIDATE";
    }
    
    @Override
    public boolean shouldExecute(ProcessingContext<EntityData> context) {
        return context.getProcessedData() != null;
    }
    
    private void updateProcessStatus(ProcessingContext<EntityData> context, ProcessStatus status) {
        log.info("Updating process status to: {} for process: {}", status, context.getProcessId());
    }
    
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
