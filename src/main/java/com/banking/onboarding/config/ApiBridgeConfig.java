package com.banking.onboarding.config;

import com.banking.onboarding.bridge.ApiEndpointRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for the API Bridge system with WebClient
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApiBridgeConfig {
    
    // WebClient Configuration - BEST MODERN ALTERNATIVE
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
    
    @Bean
    public CommandLineRunner initializeApiEndpoints(ApiEndpointRegistry registry) {
        return args -> {
            log.info("Initializing API Bridge endpoints...");
            registry.initializeDefaultEndpoints();
            log.info("API Bridge initialization completed");
        };
    }
}
