package com.banking.onboarding.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration for Bridge Service
 * Provides dedicated thread pool for bridge operations
 */
@Slf4j
@Configuration
@EnableAsync
public class BridgeAsyncConfig {

    @Value("${bridge.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${bridge.async.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${bridge.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${bridge.async.thread-name-prefix:bridge-async-}")
    private String threadNamePrefix;

    @Value("${bridge.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    /**
     * Bridge Task Executor for async bridge operations
     */
    @Bean("bridgeTaskExecutor")
    public Executor bridgeTaskExecutor() {
        log.info("Configuring Bridge Task Executor - Core: {}, Max: {}, Queue: {}", 
                corePoolSize, maxPoolSize, queueCapacity);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("Bridge task rejected - queue is full. Task: {}", r.toString());
            throw new RuntimeException("Bridge task queue is full");
        });

        executor.initialize();
        log.info("Bridge Task Executor initialized successfully");
        return executor;
    }
}
