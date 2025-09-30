package com.banking.onboarding.function;

import com.banking.onboarding.context.ProcessingContext;
import com.banking.onboarding.model.EntityData;
import com.banking.onboarding.model.OnboardingProcess.ProcessStatus;
import com.banking.onboarding.service.FenergoIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Submit EntityData to Fenergo API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FenergoFunction implements ProcessingFunction<EntityData> {
    
    private final FenergoIntegrationService fenergoService;
    
    @Override
    public ProcessingContext<EntityData> apply(ProcessingContext<EntityData> context) {
        log.info("[TRACE:{}] Executing FenergoFunction for process: {}", 
                context.getTraceId(), context.getProcessId());
        
        try {
            EntityData entityData = context.getProcessedData();
            
            if (entityData == null) {
                throw new IllegalArgumentException("EntityData is null - validation may have failed");
            }
            
            // Submit to Fenergo
            Map<String, Object> fenergoResponse = fenergoService.submitEntityToFenergo(entityData, context.getProcessId());
            
            // Add Fenergo response to context
            context.addResult("fenergoResponse", fenergoResponse);
            context.addResult("fenergoStatus", "SUCCESS");
            context.addResult("fenergoTimestamp", LocalDateTime.now());
            
            // Check if Fenergo response indicates success
            if (fenergoResponse != null && Boolean.TRUE.equals(fenergoResponse.get("error"))) {
                throw new FenergoException("Fenergo submission failed: " + fenergoResponse.get("message"));
            }
            
            log.info("[TRACE:{}] FenergoFunction completed successfully for process: {}", 
                    context.getTraceId(), context.getProcessId());
            
        } catch (Exception e) {
            log.error("[TRACE:{}] FenergoFunction failed for process: {}", 
                    context.getTraceId(), context.getProcessId(), e);
            context.addResult("fenergoStatus", "FAILED");
            context.addResult("fenergoError", e.getMessage());
            throw new RuntimeException("Fenergo submission failed", e);
        }
        
        return context;
    }
    
    @Override
    public String getStepName() {
        return "FENERGO";
    }
    
    @Override
    public boolean shouldExecute(ProcessingContext<EntityData> context) {
        return context.getProcessedData() != null &&
               "SUCCESS".equals(context.getResult("validationStatus"));
    }
    
    
    public static class FenergoException extends RuntimeException {
        public FenergoException(String message) {
            super(message);
        }
    }
}
