package com.banking.onboarding.config;

import com.banking.onboarding.monitoring.ProcessingMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Enhanced configuration for monitoring, tracing, and async processing
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class EnhancedProcessingConfig {
    
    /**
     * Enhanced task executor for functional processing with monitoring
     */
    @Bean(name = "enhancedTaskExecutor")
    public TaskExecutor enhancedTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("EnhancedProcessing-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("Enhanced task executor configured with core: {}, max: {}, queue: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * Processing metrics bean
     */
    @Bean
    public ProcessingMetrics processingMetrics() {
        log.info("Processing metrics bean created");
        return new ProcessingMetrics();
    }
    
    /**
     * Circuit breaker configuration
     */
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.builder()
                .failureThreshold(5)
                .timeoutMs(60000)
                .halfOpenMaxCalls(3)
                .build();
    }
    
    /**
     * Retry policy configuration
     */
    @Bean
    public RetryPolicyConfig retryPolicyConfig() {
        return RetryPolicyConfig.builder()
                .maxRetries(3)
                .baseDelayMs(1000)
                .maxDelayMs(10000)
                .multiplier(2.0)
                .build();
    }
    
    /**
     * Circuit breaker configuration
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CircuitBreakerConfig {
        private int failureThreshold;
        private long timeoutMs;
        private int halfOpenMaxCalls;
    }
    
    /**
     * Retry policy configuration
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RetryPolicyConfig {
        private int maxRetries;
        private long baseDelayMs;
        private long maxDelayMs;
        private double multiplier;
    }
}
