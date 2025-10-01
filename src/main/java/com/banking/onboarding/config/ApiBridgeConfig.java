package com.banking.onboarding.config;

import com.banking.onboarding.bridge.ApiEndpointRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuration for the API Bridge system with RestClient
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApiBridgeConfig {
    
    // RestClient Configuration
    @Bean
    public RestClient webClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(30000);
        
        return RestClient.builder()
                .requestFactory(requestFactory)
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
