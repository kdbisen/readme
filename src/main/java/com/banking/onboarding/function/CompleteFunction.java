package com.banking.onboarding.function;

import com.banking.onboarding.context.ProcessingContext;
import com.banking.onboarding.model.EntityData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Complete the process and update final status
 */
@Slf4j
@Component
public class CompleteFunction implements ProcessingFunction<EntityData> {
    
    @Override
    public ProcessingContext<EntityData> apply(ProcessingContext<EntityData> context) {
        log.info("[TRACE:{}] Executing CompleteFunction for process: {}", 
                context.getTraceId(), context.getProcessId());
        
        try {
            // Mark process as completed
            context.addResult("processStatus", "COMPLETED");
            context.addResult("completionTimestamp", LocalDateTime.now());
            context.addResult("totalProcessingTime", context.getProcessingDurationMs());
            
            log.info("[TRACE:{}] CompleteFunction completed successfully for process: {}", 
                    context.getTraceId(), context.getProcessId());
            
        } catch (Exception e) {
            log.error("[TRACE:{}] CompleteFunction failed for process: {}", 
                    context.getTraceId(), context.getProcessId(), e);
            context.addResult("processStatus", "FAILED");
            context.addResult("completionError", e.getMessage());
            throw new RuntimeException("Completion failed", e);
        }
        
        return context;
    }
    
    @Override
    public String getStepName() {
        return "COMPLETE";
    }
    
    @Override
    public boolean shouldExecute(ProcessingContext<EntityData> context) {
        return "SUCCESS".equals(context.getResult("fenergoStatus"));
    }
    
}
