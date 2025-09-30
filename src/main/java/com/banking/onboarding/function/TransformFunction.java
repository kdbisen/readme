package com.banking.onboarding.function;

import com.banking.onboarding.context.ProcessingContext;
import com.banking.onboarding.model.EntityData;
import com.banking.onboarding.model.RequestType;
import com.banking.onboarding.service.EntityTransformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transform XML/JSON payload to EntityData
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransformFunction implements ProcessingFunction<EntityData> {
    
    private final EntityTransformationService transformationService;
    
    @Override
    public ProcessingContext<EntityData> apply(ProcessingContext<EntityData> context) {
        log.info("[TRACE:{}] Executing TransformFunction for process: {}", 
                context.getTraceId(), context.getProcessId());
        
        try {
            String payload = context.getRawPayload();
            RequestType requestType = context.getRequestType();
            
            // Transform XML to EntityData
            EntityData entityData = transformationService.transformXmlToEntityData(payload);
            
            // Update context with transformed data
            context.setProcessedData(entityData);
            context.addResult("transformedJson", transformationService.transformXmlToJson(payload));
            context.addResult("transformTimestamp", LocalDateTime.now());
            context.addResult("transformStatus", "SUCCESS");
            
            // Add step data
            context.addStepData("entityId", entityData.getEntityId());
            context.addStepData("entityName", entityData.getEntityName());
            
            log.info("[TRACE:{}] TransformFunction completed successfully for process: {}", 
                    context.getTraceId(), context.getProcessId());
            
        } catch (Exception e) {
            log.error("[TRACE:{}] TransformFunction failed for process: {}", 
                    context.getTraceId(), context.getProcessId(), e);
            context.addStepData("transformError", e.getMessage());
            throw new RuntimeException("Transform failed", e);
        }
        
        return context;
    }
    
    @Override
    public String getStepName() {
        return "TRANSFORM";
    }
}
