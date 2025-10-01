package com.banking.onboarding.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Configuration for Fenergo Journey services
 */
@Configuration
public class FenergoJourneyConfig {

    /**
     * WebClient for Fenergo Journey API calls
     */
    @Bean("fenergoWebClient")
    public WebClient fenergoWebClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
}
