package com.banking.onboarding.step.config;

import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.GenericStepExecutionEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Generic Step Auto-Registration Configuration
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GenericStepAutoRegistrationConfig {
    
    private final ApplicationContext applicationContext;
    private final GenericStepExecutionEngine stepExecutionEngine;
    
    /**
     * Auto-register all GenericStepExecutor beans
     */
    @PostConstruct
    public void autoRegisterSteps() {
        Map<String, GenericStepExecutor> stepExecutors = applicationContext.getBeansOfType(GenericStepExecutor.class);
        
        log.info("Found {} generic step executors to register", stepExecutors.size());
        
        stepExecutors.forEach((beanName, executor) -> {
            stepExecutionEngine.register(executor);
            log.info("Auto-registered generic step executor: {} -> {}", beanName, executor.getStepName());
        });
        
        log.info("Generic step auto-registration completed. Total registered steps: {}", 
                stepExecutionEngine.getRegisteredSteps().length);
    }
}






