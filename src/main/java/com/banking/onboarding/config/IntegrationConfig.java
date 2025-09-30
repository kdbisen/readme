package com.banking.onboarding.config;

import com.banking.onboarding.model.EntityData;
import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.service.EntityTransformationService;
import com.banking.onboarding.service.FenergoIntegrationService;
import com.banking.onboarding.service.OnboardingProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;

@Slf4j
@Configuration
@EnableIntegration
@RequiredArgsConstructor
public class IntegrationConfig {
    
    private final EntityTransformationService transformationService;
    private final FenergoIntegrationService fenergoService;
    private final OnboardingProcessService processService;
    
    @Bean
    public MessageChannel processEntityChannel() {
        return new DirectChannel();
    }
    
    @Bean
    public MessageChannel transformationChannel() {
        return new QueueChannel(10);
    }
    
    @Bean
    public MessageChannel validationChannel() {
        return new QueueChannel(10);
    }
    
    @Bean
    public MessageChannel fenergoChannel() {
        return new QueueChannel(10);
    }
    
    @Bean
    public MessageChannel completionChannel() {
        return new DirectChannel();
    }
    
    @Bean
    public IntegrationFlow processEntityFlow() {
        return IntegrationFlows
                .from(processEntityChannel())
                .handle(processEntityHandler())
                .channel(transformationChannel())
                .get();
    }
    
    @Bean
    public IntegrationFlow transformationFlow() {
        return IntegrationFlows
                .from(transformationChannel())
                .handle(transformationHandler())
                .channel(validationChannel())
                .get();
    }
    
    @Bean
    public IntegrationFlow validationFlow() {
        return IntegrationFlows
                .from(validationChannel())
                .handle(validationHandler())
                .channel(fenergoChannel())
                .get();
    }
    
    @Bean
    public IntegrationFlow fenergoFlow() {
        return IntegrationFlows
                .from(fenergoChannel())
                .handle(fenergoHandler())
                .channel(completionChannel())
                .get();
    }
    
    @Bean
    public IntegrationFlow completionFlow() {
        return IntegrationFlows
                .from(completionChannel())
                .handle(completionHandler())
                .get();
    }
    
    @Bean
    public MessageHandler processEntityHandler() {
        return message -> {
            OnboardingProcess payload = (OnboardingProcess) message.getPayload();
            log.info("Processing entity for process: {}", payload.getProcessId());
            
            try {
                // Update status to TRANSFORMING
                payload.setStatus(OnboardingProcess.ProcessStatus.TRANSFORMING);
                processService.updateProcess(payload);
                
                transformationChannel().send(MessageBuilder.withPayload(payload).build());
                
            } catch (Exception e) {
                log.error("Error in process entity handler for process: {}", payload.getProcessId(), e);
                payload.setStatus(OnboardingProcess.ProcessStatus.FAILED);
                payload.setErrorMessage("Error in process entity handler: " + e.getMessage());
                processService.updateProcess(payload);
            }
        };
    }
    
    @Bean
    public MessageHandler transformationHandler() {
        return message -> {
            OnboardingProcess payload = (OnboardingProcess) message.getPayload();
            log.info("Transforming XML to JSON for process: {}", payload.getProcessId());
            
            try {
                // Transform XML to JSON
                Map<String, Object> transformedJson = transformationService.transformXmlToJson(payload.getOriginalXml());
                payload.setTransformedJson(transformedJson);
                
                // Transform XML to EntityData
                EntityData entityData = transformationService.transformXmlToEntityData(payload.getOriginalXml());
                payload.setEntityData(entityData);
                
                // Update status to VALIDATING
                payload.setStatus(OnboardingProcess.ProcessStatus.VALIDATING);
                processService.updateProcess(payload);
                
                validationChannel().send(MessageBuilder.withPayload(payload).build());
                
            } catch (Exception e) {
                log.error("Error in transformation handler for process: {}", payload.getProcessId(), e);
                payload.setStatus(OnboardingProcess.ProcessStatus.FAILED);
                payload.setErrorMessage("Error in transformation: " + e.getMessage());
                processService.updateProcess(payload);
            }
        };
    }
    
    @Bean
    public MessageHandler validationHandler() {
        return message -> {
            OnboardingProcess payload = (OnboardingProcess) message.getPayload();
            log.info("Validating entity data for process: {}", payload.getProcessId());
            
            try {
                // Validate entity data
                boolean isValid = transformationService.validateEntityData(payload.getEntityData());
                
                if (!isValid) {
                    payload.setStatus(OnboardingProcess.ProcessStatus.FAILED);
                    payload.setErrorMessage("Entity data validation failed");
                    processService.updateProcess(payload);
                    return;
                }
                
                // Update status to PROCESSING_FENERGO
                payload.setStatus(OnboardingProcess.ProcessStatus.PROCESSING_FENERGO);
                processService.updateProcess(payload);
                
                fenergoChannel().send(MessageBuilder.withPayload(payload).build());
                
            } catch (Exception e) {
                log.error("Error in validation handler for process: {}", payload.getProcessId(), e);
                payload.setStatus(OnboardingProcess.ProcessStatus.FAILED);
                payload.setErrorMessage("Error in validation: " + e.getMessage());
                processService.updateProcess(payload);
            }
        };
    }
    
    @Bean
    public MessageHandler fenergoHandler() {
        return message -> {
            OnboardingProcess payload = (OnboardingProcess) message.getPayload();
            log.info("Submitting to Fenergo for process: {}", payload.getProcessId());
            
            try {
                // Submit to Fenergo
                Map<String, Object> fenergoResponse = fenergoService
                        .submitEntityToFenergo(payload.getEntityData(), payload.getProcessId());
                
                payload.setFenergoResponse(fenergoResponse);
                
                // Check if Fenergo response indicates success
                if (fenergoResponse != null && !Boolean.TRUE.equals(fenergoResponse.get("error"))) {
                    payload.setStatus(OnboardingProcess.ProcessStatus.COMPLETED);
                } else {
                    payload.setStatus(OnboardingProcess.ProcessStatus.FAILED);
                    payload.setErrorMessage("Fenergo submission failed: " + 
                            fenergoResponse.getOrDefault("message", "Unknown error"));
                }
                
                processService.updateProcess(payload);
                
                completionChannel().send(MessageBuilder.withPayload(payload).build());
                
            } catch (Exception e) {
                log.error("Error in Fenergo handler for process: {}", payload.getProcessId(), e);
                payload.setStatus(OnboardingProcess.ProcessStatus.FAILED);
                payload.setErrorMessage("Error in Fenergo submission: " + e.getMessage());
                processService.updateProcess(payload);
            }
        };
    }
    
    @Bean
    public MessageHandler completionHandler() {
        return message -> {
            OnboardingProcess payload = (OnboardingProcess) message.getPayload();
            log.info("Completing process: {}", payload.getProcessId());
            
            try {
                // Final status update
                processService.updateProcess(payload);
                
                log.info("Process completed successfully: {}", payload.getProcessId());
                
            } catch (Exception e) {
                log.error("Error in completion handler for process: {}", payload.getProcessId(), e);
            }
        };
    }
}
