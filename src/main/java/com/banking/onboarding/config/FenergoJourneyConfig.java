package com.banking.onboarding.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuration for Fenergo Journey services
 */
@Configuration
public class FenergoJourneyConfig {

    /**
     * RestClient for Fenergo Journey API calls
     */
    @Bean("fenergoRestClient")
    public RestClient fenergoRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(30000);
        
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
