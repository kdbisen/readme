package com.banking.onboarding.service;

import com.banking.onboarding.domain.ApiRequest;
import com.banking.onboarding.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fenergo service for external API operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FenergoService {
    
    private final ApiService apiService;
    
    @Value("${fenergo.proxy.url:http://fenergo-proxy.com/api/v1/proxy}")
    private String fenergoProxyUrl;
    
    @Value("${fenergo.entity.create.endpoint:https://fenergo.com/api/v1/entities}")
    private String entityCreateEndpoint;
    
    @Value("${fenergo.entity.create.auth-scope:fenergo-entity-create}")
    private String entityCreateAuthScope;
    
    @Value("${fenergo.journey.info.endpoint:https://fenergo.com/api/v1/journeys/info}")
    private String journeyInfoEndpoint;
    
    @Value("${fenergo.journey.info.auth-scope:fenergo-journey-info}")
    private String journeyInfoAuthScope;
    
    @Value("${fenergo.journey.initiate.endpoint:https://fenergo.com/api/v1/journeys/initiate}")
    private String journeyInitiateEndpoint;
    
    @Value("${fenergo.journey.initiate.auth-scope:fenergo-journey-initiate}")
    private String journeyInitiateAuthScope;
    
    @Value("${fenergo.journey.details.endpoint:https://fenergo.com/api/v1/journeys/details}")
    private String journeyDetailsEndpoint;
    
    @Value("${fenergo.journey.details.auth-scope:fenergo-journey-details}")
    private String journeyDetailsAuthScope;
    
    /**
     * Create entity in Fenergo via proxy
     */
    public CompletableFuture<ApiResponse> createEntity(Object entityData, String correlationId) {
        log.info("[CORRELATION:{}] Creating Fenergo entity", correlationId);
        
        Map<String, String> fenergoHeaders = Map.of(
                "X-Service-Type", "entity-create",
                "X-Request-Source", "fenergo-service"
        );
        
        return apiService.callExternalApi(
                entityCreateEndpoint,
                ApiRequest.HttpMethod.POST,
                entityData,
                entityCreateAuthScope,
                fenergoProxyUrl,
                fenergoHeaders,
                correlationId
        );
    }
    
    /**
     * Get journey information from Fenergo via proxy
     */
    public CompletableFuture<ApiResponse> getJourneyInfo(Object journeyData, String correlationId) {
        log.info("[CORRELATION:{}] Getting Fenergo journey info", correlationId);
        
        Map<String, String> fenergoHeaders = Map.of(
                "X-Service-Type", "journey-info",
                "X-Request-Source", "fenergo-service"
        );
        
        return apiService.callExternalApi(
                journeyInfoEndpoint,
                ApiRequest.HttpMethod.POST,
                journeyData,
                journeyInfoAuthScope,
                fenergoProxyUrl,
                fenergoHeaders,
                correlationId
        );
    }
    
    /**
     * Initiate journey in Fenergo via proxy
     */
    public CompletableFuture<ApiResponse> initiateJourney(Object journeyData, String correlationId) {
        log.info("[CORRELATION:{}] Initiating Fenergo journey", correlationId);
        
        Map<String, String> fenergoHeaders = Map.of(
                "X-Service-Type", "journey-initiate",
                "X-Request-Source", "fenergo-service"
        );
        
        return apiService.callExternalApi(
                journeyInitiateEndpoint,
                ApiRequest.HttpMethod.POST,
                journeyData,
                journeyInitiateAuthScope,
                fenergoProxyUrl,
                fenergoHeaders,
                correlationId
        );
    }
    
    /**
     * Get journey details from Fenergo via proxy
     */
    public CompletableFuture<ApiResponse> getJourneyDetails(Object journeyData, String correlationId) {
        log.info("[CORRELATION:{}] Getting Fenergo journey details", correlationId);
        
        Map<String, String> fenergoHeaders = Map.of(
                "X-Service-Type", "journey-details",
                "X-Request-Source", "fenergo-service"
        );
        
        return apiService.callExternalApi(
                journeyDetailsEndpoint,
                ApiRequest.HttpMethod.POST,
                journeyData,
                journeyDetailsAuthScope,
                fenergoProxyUrl,
                fenergoHeaders,
                correlationId
        );
    }
}
