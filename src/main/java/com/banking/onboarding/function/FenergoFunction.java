package com.banking.onboarding.function;

import com.banking.onboarding.bridge.ApiBridgeService;
import com.banking.onboarding.bridge.ApiResponse;
import com.banking.onboarding.context.ProcessingContext;
import com.banking.onboarding.model.EntityData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Submit EntityData to Fenergo API using the bridge system
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FenergoFunction implements ProcessingFunction<EntityData> {
    
    private final ApiBridgeService bridgeService;
    
    @Override
    public ProcessingContext<EntityData> apply(ProcessingContext<EntityData> context) {
        log.info("[TRACE:{}] Executing FenergoFunction for process: {}", 
                context.getTraceId(), context.getProcessId());
        
        try {
            EntityData entityData = context.getProcessedData();
            
            if (entityData == null) {
                throw new IllegalArgumentException("EntityData is null - validation may have failed");
            }
            
            // Submit to Fenergo using bridge service
            ApiResponse fenergoResponse = bridgeService.callApi("SUBMIT_KYC", entityData);
            
            // Add Fenergo response to context
            context.addResult("fenergoResponse", fenergoResponse.getData());
            context.addResult("fenergoStatus", fenergoResponse.isSuccess() ? "SUCCESS" : "FAILED");
            context.addResult("fenergoTimestamp", LocalDateTime.now());
            context.addResult("fenergoStatusCode", fenergoResponse.getStatusCode());
            context.addResult("fenergoResponseTime", fenergoResponse.getResponseTimeMs());
            
            // Check if Fenergo response indicates success
            if (!fenergoResponse.isSuccess()) {
                throw new FenergoException("Fenergo submission failed: " + fenergoResponse.getErrorMessage());
            }
            
            log.info("[TRACE:{}] FenergoFunction completed successfully for process: {} in {}ms", 
                    context.getTraceId(), context.getProcessId(), fenergoResponse.getResponseTimeMs());
            
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
