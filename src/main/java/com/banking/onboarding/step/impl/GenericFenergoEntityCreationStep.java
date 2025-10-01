package com.banking.onboarding.step.impl;

import com.banking.onboarding.service.FenergoService;
import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Generic Fenergo Entity Creation Step
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenericFenergoEntityCreationStep implements GenericStepExecutor {
    
    private final FenergoService fenergoService;
    
    @Override
    public CompletableFuture<StepResult<Object>> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing generic Fenergo entity creation", context.getCorrelationId());
        
        // Get input data from previous step or initial input
        Object inputData = getInputData(context);
        
        if (inputData == null) {
            return CompletableFuture.completedFuture(
                    StepResult.failure("No input data available", getStepName(), context.getCorrelationId())
            );
        }
        
        return fenergoService.createEntity(inputData, context.getCorrelationId())
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        // Store result in context for next steps
                        context.addStepResult(getStepName(), response.getBody());
                        return StepResult.success(response.getBody(), getStepName(), context.getCorrelationId());
                    } else {
                        return StepResult.failure(response.getErrorMessage(), getStepName(), context.getCorrelationId());
                    }
                });
    }
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description("Create entity in Fenergo via proxy")
                .retryEnabled(true)
                .maxRetries(3)
                .retryDelayMs(2000)
                .backoffMultiplier(2.0)
                .asyncEnabled(true)
                .timeoutMs(60000)
                .dependencies(new String[]{"XML_TO_JSON_TRANSFORMATION"})
                .build();
    }
    
    @Override
    public String getStepName() {
        return "FENERGO_ENTITY_CREATION";
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        // Check if previous step result exists or we have initial input
        return context.hasStepResult("XML_TO_JSON_TRANSFORMATION") || context.getInputData() != null;
    }
}
