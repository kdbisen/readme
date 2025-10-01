package com.banking.onboarding.service;

import com.banking.onboarding.bridge.ApiProvider;
import com.banking.onboarding.bridge.BridgeRequest;
import com.banking.onboarding.bridge.BridgeResponse;
import com.banking.onboarding.bridge.BridgeServiceFacade;
import com.banking.onboarding.service.CorrelationIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Complete Onboarding Flow Service
 * Demonstrates the full async flow: XML -> JSON (via Apigee) -> Fenergo (via Proxy)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteOnboardingFlowService {

    private final BridgeServiceFacade bridgeServiceFacade;
    private final TransformationService transformationService;
    private final CorrelationIdService correlationIdService;

    @Value("${fenergo.entity.create.proxy-url:http://fenergo-proxy.com/api/v1/proxy}")
    private String fenergoEntityCreateProxyUrl;

    @Value("${fenergo.entity.create.actual-endpoint:https://fenergo.com/api/v1/entities}")
    private String fenergoEntityCreateActualEndpoint;

    @Value("${fenergo.entity.create.auth-scope:fenergo-entity-create}")
    private String fenergoEntityCreateAuthScope;

    /**
     * Complete onboarding flow: XML -> JSON (Apigee) -> Fenergo Entity Create (Proxy)
     */
    public CompletableFuture<BridgeResponse> processOnboardingFlow(String xmlData, String correlationId) {
        log.info("[CORRELATION:{}] Starting complete onboarding flow", correlationId);
        
        // Step 1: Transform XML to JSON via Apigee transformation service
        return transformationService.transformXmlToJson(xmlData, correlationId)
                .thenCompose(transformationResponse -> {
                    if (!transformationResponse.isSuccess()) {
                        log.error("[CORRELATION:{}] XML to JSON transformation failed: {}", 
                                correlationId, transformationResponse.getErrorMessage());
                        return CompletableFuture.completedFuture(transformationResponse);
                    }
                    
                    log.info("[CORRELATION:{}] XML to JSON transformation successful, proceeding to Fenergo", correlationId);
                    
                    // Step 2: Call Fenergo Entity Create API via proxy
                    return callFenergoEntityCreate(transformationResponse.getBody(), correlationId);
                });
    }

    /**
     * Call Fenergo Entity Create API via proxy
     */
    private CompletableFuture<BridgeResponse> callFenergoEntityCreate(String jsonData, String correlationId) {
        log.info("[CORRELATION:{}] Calling Fenergo Entity Create API via proxy", correlationId);
        
        BridgeRequest fenergoRequest = BridgeRequest.builder()
                .proxyUrl(fenergoEntityCreateProxyUrl)
                .actualEndpoint(fenergoEntityCreateActualEndpoint)
                .method("POST")
                .payload(Map.of("entityData", jsonData))
                .apiProvider(ApiProvider.FENERGO)
                .authScope(fenergoEntityCreateAuthScope)
                .correlationId(correlationId)
                .headers(Map.of(
                    "Content-Type", "application/json",
                    "X-Service-Type", "entity-create",
                    "X-Request-Source", "onboarding-flow"
                ))
                .build();

        return bridgeServiceFacade.callApi(fenergoRequest)
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        log.info("[CORRELATION:{}] Fenergo Entity Create API call successful", correlationId);
                    } else {
                        log.error("[CORRELATION:{}] Fenergo Entity Create API call failed: {}", 
                                correlationId, response.getErrorMessage());
                    }
                    return response;
                });
    }

    /**
     * Multi-step onboarding flow with multiple Fenergo API calls
     */
    public CompletableFuture<BridgeResponse> processMultiStepOnboardingFlow(String xmlData, String correlationId) {
        log.info("[CORRELATION:{}] Starting multi-step onboarding flow", correlationId);
        
        // Step 1: Transform XML to JSON via Apigee
        return transformationService.transformXmlToJson(xmlData, correlationId)
                .thenCompose(transformationResponse -> {
                    if (!transformationResponse.isSuccess()) {
                        return CompletableFuture.completedFuture(transformationResponse);
                    }
                    
                    // Step 2: Create Entity in Fenergo
                    return callFenergoEntityCreate(transformationResponse.getBody(), correlationId);
                })
                .thenCompose(entityCreateResponse -> {
                    if (!entityCreateResponse.isSuccess()) {
                        return CompletableFuture.completedFuture(entityCreateResponse);
                    }
                    
                    // Step 3: Get Journey Information
                    return callFenergoJourneyInfo(entityCreateResponse.getBody(), correlationId);
                })
                .thenCompose(journeyInfoResponse -> {
                    if (!journeyInfoResponse.isSuccess()) {
                        return CompletableFuture.completedFuture(journeyInfoResponse);
                    }
                    
                    // Step 4: Initiate Journey
                    return callFenergoJourneyInitiate(journeyInfoResponse.getBody(), correlationId);
                });
    }

    /**
     * Call Fenergo Journey Info API
     */
    private CompletableFuture<BridgeResponse> callFenergoJourneyInfo(String entityData, String correlationId) {
        log.info("[CORRELATION:{}] Calling Fenergo Journey Info API", correlationId);
        
        BridgeRequest request = BridgeRequest.builder()
                .proxyUrl(fenergoEntityCreateProxyUrl) // Same proxy, different endpoint
                .actualEndpoint("https://fenergo.com/api/v1/journeys/info")
                .method("POST")
                .payload(Map.of("entityData", entityData))
                .apiProvider(ApiProvider.FENERGO)
                .authScope("fenergo-journey-info")
                .correlationId(correlationId)
                .build();

        return bridgeServiceFacade.callApi(request);
    }

    /**
     * Call Fenergo Journey Initiate API
     */
    private CompletableFuture<BridgeResponse> callFenergoJourneyInitiate(String journeyData, String correlationId) {
        log.info("[CORRELATION:{}] Calling Fenergo Journey Initiate API", correlationId);
        
        BridgeRequest request = BridgeRequest.builder()
                .proxyUrl(fenergoEntityCreateProxyUrl) // Same proxy, different endpoint
                .actualEndpoint("https://fenergo.com/api/v1/journeys/initiate")
                .method("POST")
                .payload(Map.of("journeyData", journeyData))
                .apiProvider(ApiProvider.FENERGO)
                .authScope("fenergo-journey-initiate")
                .correlationId(correlationId)
                .build();

        return bridgeServiceFacade.callApi(request);
    }
}
