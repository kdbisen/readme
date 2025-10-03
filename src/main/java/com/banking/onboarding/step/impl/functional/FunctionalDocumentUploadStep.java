package com.banking.onboarding.step.impl.functional;

import com.banking.onboarding.constants.OnboardingConstants.*;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import com.banking.onboarding.step.functional.FunctionalStepExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Functional Document Upload Step - Enhanced with Java 8 Functional Interfaces
 * Demonstrates powerful functional programming patterns
 */
@Slf4j
@Component
public class FunctionalDocumentUploadStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        return FunctionalStepExecutor.executeStep(
                getStepName(),
                context.getCorrelationId(),
                context,
                createBusinessLogic(),                    // Supplier<T>
                createSuccessMessageCreator(),            // Function<T, String>
                createPreCondition(),                     // Predicate<GenericStepContext>
                createPostExecutionAction(),              // Consumer<T>
                createErrorTransformer()                  // Function<Exception, String>
        );
    }

    /**
     * Business Logic as Supplier - Pure function with no parameters
     */
    private Supplier<Map<String, Object>> createBusinessLogic() {
        return () -> Map.of(
            "uploadedFiles", 3,
            "totalSize", "2.5MB",
            "fileTypes", new String[]{"PDF", "JPG", "PNG"},
            "uploadStatus", "SUCCESS",
            "documentIds", new String[]{"DOC-001", "DOC-002", "DOC-003"},
            "uploadTimestamp", System.currentTimeMillis(),
            "processingTime", 1500L
        );
    }

    /**
     * Success Message Creator as Function - Transforms result to message
     */
    private Function<Map<String, Object>, String> createSuccessMessageCreator() {
        return result -> String.format(
            "Successfully uploaded %d files (%s) in %dms. Document IDs: %s",
            result.get("uploadedFiles"),
            result.get("totalSize"),
            result.get("processingTime"),
            String.join(", ", (String[]) result.get("documentIds"))
        );
    }

    /**
     * Pre-condition as Predicate - Validates context before execution
     */
    private Predicate<GenericStepContext> createPreCondition() {
        return context -> {
            // Multiple validation conditions using functional composition
            return hasValidInputData(context) 
                && hasRequiredPermissions(context)
                && isWithinBusinessHours();
        };
    }

    /**
     * Post-execution Action as Consumer - Performs side effects
     */
    private Consumer<Map<String, Object>> createPostExecutionAction() {
        return result -> {
            // Side effects: logging, metrics, notifications
            logDocumentUploadMetrics(result);
            sendUploadNotification(result);
            updateUploadStatistics(result);
        };
    }

    /**
     * Error Transformer as Function - Customizes error messages
     */
    private Function<Exception, String> createErrorTransformer() {
        return exception -> {
            String baseMessage = "Document upload failed";
            
            // Custom error messages based on exception type
            if (exception instanceof IllegalArgumentException) {
                return baseMessage + " - Invalid input: " + exception.getMessage();
            } else if (exception instanceof SecurityException) {
                return baseMessage + " - Security violation: " + exception.getMessage();
            } else if (exception.getCause() instanceof OutOfMemoryError) {
                return baseMessage + " - Insufficient memory for file processing";
            } else {
                return baseMessage + " - Unexpected error: " + exception.getMessage();
            }
        };
    }

    // Helper methods for pre-condition validation
    private boolean hasValidInputData(GenericStepContext context) {
        return context.getInputData() != null && 
               !context.getInputData().toString().trim().isEmpty();
    }

    private boolean hasRequiredPermissions(GenericStepContext context) {
        // Simulate permission check
        return context.getCorrelationId() != null && 
               context.getCorrelationId().length() > 5;
    }

    private boolean isWithinBusinessHours() {
        int currentHour = java.time.LocalTime.now().getHour();
        return currentHour >= 8 && currentHour <= 18;
    }

    // Helper methods for post-execution actions
    private void logDocumentUploadMetrics(Map<String, Object> result) {
        log.info("Upload metrics - Files: {}, Size: {}, Time: {}ms", 
                result.get("uploadedFiles"), 
                result.get("totalSize"), 
                result.get("processingTime"));
    }

    private void sendUploadNotification(Map<String, Object> result) {
        // Simulate notification sending
        log.debug("Sending upload notification for {} files", result.get("uploadedFiles"));
    }

    private void updateUploadStatistics(Map<String, Object> result) {
        // Simulate statistics update
        log.debug("Updating upload statistics with {} files", result.get("uploadedFiles"));
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
        return createPreCondition().test(context);
    }
}
