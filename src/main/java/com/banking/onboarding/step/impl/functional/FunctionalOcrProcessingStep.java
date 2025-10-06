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
import java.util.function.*;

/**
 * Functional OCR Processing Step - Advanced Functional Programming Patterns
 * Demonstrates Function composition, BiPredicate, BiConsumer, and retry mechanisms
 */
@Slf4j
@Component
public class FunctionalOcrProcessingStep implements GenericStepExecutor {

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Use retry mechanism with functional interfaces
        return FunctionalStepExecutor.executeStepWithRetry(
                getStepName(),
                context.getCorrelationId(),
                context,
                createOcrBusinessLogic(),                 // Supplier<T>
                createSuccessMessageCreator(),            // Function<T, String>
                createRetryCondition(),                   // Predicate<Exception>
                3                                         // maxRetries
        );
    }

    /**
     * OCR Business Logic as Supplier - Complex processing logic
     */
    private Supplier<Map<String, Object>> createOcrBusinessLogic() {
        return () -> {
            // Simulate OCR processing with multiple steps
            Map<String, Object> rawOcrData = extractRawOcrData();
            Map<String, Object> processedData = processOcrData(rawOcrData);
            Map<String, Object> validatedData = validateOcrData(processedData);
            
            return Map.of(
                "ocrStatus", "COMPLETED",
                "processedDocuments", 3,
                "rawOcrData", rawOcrData,
                "processedData", processedData,
                "validatedData", validatedData,
                "overallConfidence", calculateOverallConfidence(validatedData),
                "processingTime", System.currentTimeMillis() % 5000 + 1000,
                "ocrEngine", "Advanced-OCR-v2.1"
            );
        };
    }

    /**
     * Success Message Creator with Function composition
     */
    private Function<Map<String, Object>, String> createSuccessMessageCreator() {
        // Compose multiple functions for complex message creation
        Function<Map<String, Object>, Double> confidenceExtractor = 
            result -> (Double) result.get("overallConfidence");
        
        Function<Map<String, Object>, Integer> documentCountExtractor = 
            result -> (Integer) result.get("processedDocuments");
        
        Function<Map<String, Object>, Long> processingTimeExtractor = 
            result -> (Long) result.get("processingTime");
        
        return result -> {
            double confidence = confidenceExtractor.apply(result);
            int documents = documentCountExtractor.apply(result);
            long time = processingTimeExtractor.apply(result);
            
            return String.format(
                "OCR completed: %d documents processed with %.1f%% confidence in %dms",
                documents, confidence, time
            );
        };
    }

    /**
     * Retry Condition as Predicate - Determines when to retry
     */
    private Predicate<Exception> createRetryCondition() {
        return exception -> {
            // Retry on specific exceptions
            return exception instanceof RuntimeException ||
                   exception.getMessage().contains("timeout") ||
                   exception.getMessage().contains("network");
        };
    }

    /**
     * Alternative execution with conditional logic using BiPredicate
     */
    public StepResult<Object> executeConditionally(GenericStepContext context) {
        return FunctionalStepExecutor.executeStepConditionally(
                getStepName(),
                context.getCorrelationId(),
                context,
                createOcrBusinessLogic(),
                createSuccessMessageCreator(),
                createExecutionCondition()  // BiPredicate<GenericStepContext, T>
        );
    }

    /**
     * Execution Condition as BiPredicate - Context and result dependent
     */
    private BiPredicate<GenericStepContext, Map<String, Object>> createExecutionCondition() {
        return (context, result) -> {
            // Only execute if confidence is above threshold
            double confidence = (Double) result.get("overallConfidence");
            boolean hasValidContext = context.getCorrelationId() != null;
            boolean meetsConfidenceThreshold = confidence >= 85.0;
            
            return hasValidContext && meetsConfidenceThreshold;
        };
    }

    /**
     * Alternative execution with side effects using BiConsumer
     */
    public StepResult<Object> executeWithSideEffects(GenericStepContext context) {
        return FunctionalStepExecutor.executeStepWithSideEffects(
                getStepName(),
                context.getCorrelationId(),
                context,
                createOcrBusinessLogic(),
                createSuccessMessageCreator(),
                createSideEffectAction()  // BiConsumer<GenericStepContext, T>
        );
    }

    /**
     * Side Effect Action as BiConsumer - Context and result dependent side effects
     */
    private BiConsumer<GenericStepContext, Map<String, Object>> createSideEffectAction() {
        return (context, result) -> {
            // Multiple side effects based on context and result
            updateOcrMetrics(context, result);
            cacheOcrResults(context, result);
            triggerDownstreamProcessing(context, result);
            sendOcrCompletionNotification(context, result);
        };
    }

    // Helper methods for OCR processing
    private Map<String, Object> extractRawOcrData() {
        return Map.of(
            "DOC-001", Map.of(
                "rawText", "John Doe\n123456789\n01/01/1990\n123 Main St",
                "confidence", 98.5,
                "language", "en"
            ),
            "DOC-002", Map.of(
                "rawText", "Electric Company\n123 Main St\nAccount: ACC-789456",
                "confidence", 95.2,
                "language", "en"
            ),
            "DOC-003", Map.of(
                "rawText", "ABC Corporation\nSalary: $75,000\nStart: 2020-01-01",
                "confidence", 92.8,
                "language", "en"
            )
        );
    }

    private Map<String, Object> processOcrData(Map<String, Object> rawData) {
        return Map.of(
            "processedFields", Map.of(
                "DOC-001", Map.of("name", "John Doe", "id", "123456789", "dob", "01/01/1990"),
                "DOC-002", Map.of("address", "123 Main St", "account", "ACC-789456"),
                "DOC-003", Map.of("employer", "ABC Corporation", "salary", "$75,000")
            ),
            "processingMetadata", Map.of(
                "algorithm", "Advanced-NLP",
                "version", "2.1",
                "timestamp", System.currentTimeMillis()
            )
        );
    }

    private Map<String, Object> validateOcrData(Map<String, Object> processedData) {
        return Map.of(
            "validationResults", Map.of(
                "fieldCompleteness", 95.0,
                "dataAccuracy", 92.5,
                "formatCompliance", 98.0
            ),
            "validatedFields", processedData.get("processedFields"),
            "validationTimestamp", System.currentTimeMillis()
        );
    }

    private double calculateOverallConfidence(Map<String, Object> validatedData) {
        Map<String, Object> validationResults = (Map<String, Object>) validatedData.get("validationResults");
        return ((Double) validationResults.get("fieldCompleteness") +
                (Double) validationResults.get("dataAccuracy") +
                (Double) validationResults.get("formatCompliance")) / 3.0;
    }

    // Side effect methods
    private void updateOcrMetrics(GenericStepContext context, Map<String, Object> result) {
        log.info("[CORRELATION:{}] Updating OCR metrics for {} documents", 
                context.getCorrelationId(), result.get("processedDocuments"));
    }

    private void cacheOcrResults(GenericStepContext context, Map<String, Object> result) {
        log.debug("[CORRELATION:{}] Caching OCR results with confidence {}", 
                context.getCorrelationId(), result.get("overallConfidence"));
    }

    private void triggerDownstreamProcessing(GenericStepContext context, Map<String, Object> result) {
        log.debug("[CORRELATION:{}] Triggering downstream processing", context.getCorrelationId());
    }

    private void sendOcrCompletionNotification(GenericStepContext context, Map<String, Object> result) {
        log.debug("[CORRELATION:{}] Sending OCR completion notification", context.getCorrelationId());
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
        // Use functional validation
        return context.getStepResult(StepNames.DOCUMENT_VALIDATION) != null &&
               context.getCorrelationId() != null &&
               !context.getCorrelationId().isEmpty();
    }
}

