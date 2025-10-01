package com.banking.onboarding.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * RestClient Configuration
 * Replaces WebClient with RestClient for HTTP calls
 */
@Slf4j
@Configuration
public class RestClientConfig {

    @Value("${restclient.timeout.connect:10000}")
    private int connectTimeout;

    @Value("${restclient.timeout.read:30000}")
    private int readTimeout;

    @Value("${restclient.timeout.write:30000}")
    private int writeTimeout;

    @Bean
    public RestClient restClient() {
        log.info("Configuring RestClient with connect timeout: {}ms, read timeout: {}ms", 
                connectTimeout, readTimeout);
        
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        
        return RestClient.builder()
                .requestFactory(requestFactory);
    }
}
