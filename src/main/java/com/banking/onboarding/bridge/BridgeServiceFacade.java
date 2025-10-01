package com.banking.onboarding.bridge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Bridge Service Facade
 * Provides high-level methods for using the async bridge service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BridgeServiceFacade {

    private final GenericBridgeService genericBridgeService;

    /**
     * Call Apigee API asynchronously
     */
    public CompletableFuture<BridgeResponse> callApigeeApi(String endpoint, String method, Object payload, String authScope, String correlationId) {
        log.info("[CORRELATION:{}] Initiating async Apigee API call to: {}", correlationId, endpoint);
        
        BridgeRequest request = BridgeRequest.builder()
                .endpoint(endpoint)
                .method(method)
                .payload(payload)
                .apiProvider(ApiProvider.APIGEE)
                .authScope(authScope)
                .correlationId(correlationId)
                .build();

        return genericBridgeService.execute(request);
    }

    /**
     * Call Fenergo API asynchronously via proxy
     */
    public CompletableFuture<BridgeResponse> callFenergoApi(String proxyUrl, String actualEndpoint, String method, Object payload, String authScope, String correlationId) {
        log.info("[CORRELATION:{}] Initiating async Fenergo API call via proxy: {} -> {}", correlationId, proxyUrl, actualEndpoint);
        
        BridgeRequest request = BridgeRequest.builder()
                .proxyUrl(proxyUrl)
                .actualEndpoint(actualEndpoint)
                .method(method)
                .payload(payload)
                .apiProvider(ApiProvider.FENERGO)
                .authScope(authScope)
                .correlationId(correlationId)
                .build();

        return genericBridgeService.execute(request);
    }

    /**
     * Call any API asynchronously with full control
     */
    public CompletableFuture<BridgeResponse> callApi(BridgeRequest request) {
        log.info("[CORRELATION:{}] Initiating async API call via {} to: {}", 
                request.getCorrelationId(), request.getApiProvider().getCode(), request.getEndpoint());
        
        return genericBridgeService.execute(request);
    }

    /**
     * Chain multiple API calls asynchronously
     */
    public CompletableFuture<BridgeResponse> chainApiCalls(BridgeRequest firstRequest, BridgeRequest secondRequest) {
        log.info("[CORRELATION:{}] Initiating chained async API calls", firstRequest.getCorrelationId());
        
        return genericBridgeService.execute(firstRequest)
                .thenCompose(firstResponse -> {
                    if (firstResponse.isSuccess()) {
                        log.info("[CORRELATION:{}] First API call successful, proceeding with second call", firstRequest.getCorrelationId());
                        return genericBridgeService.execute(secondRequest);
                    } else {
                        log.error("[CORRELATION:{}] First API call failed, skipping second call", firstRequest.getCorrelationId());
                        return CompletableFuture.completedFuture(BridgeResponse.error(
                                500, 
                                "First API call failed: " + firstResponse.getErrorMessage(),
                                secondRequest.getEndpoint(),
                                secondRequest.getApiProvider(),
                                secondRequest.getCorrelationId()
                        ));
                    }
                });
    }
}
