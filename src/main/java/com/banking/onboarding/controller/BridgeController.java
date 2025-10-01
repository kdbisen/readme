package com.banking.onboarding.controller;

import com.banking.onboarding.bridge.ApiProvider;
import com.banking.onboarding.bridge.BridgeRequest;
import com.banking.onboarding.bridge.BridgeResponse;
import com.banking.onboarding.bridge.BridgeServiceFacade;
import com.banking.onboarding.service.CorrelationIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Bridge Controller
 * Demonstrates async bridge service usage
 */
@Slf4j
@RestController
@RequestMapping("/api/bridge")
@RequiredArgsConstructor
public class BridgeController {

    private final BridgeServiceFacade bridgeServiceFacade;
    private final CorrelationIdService correlationIdService;

    /**
     * Call Apigee API asynchronously
     */
    @PostMapping("/apigee/{endpoint}")
    public CompletableFuture<ResponseEntity<BridgeResponse>> callApigeeApi(
            @PathVariable String endpoint,
            @RequestParam(defaultValue = "POST") String method,
            @RequestParam(defaultValue = "apigee-api") String authScope,
            @RequestBody(required = false) Object payload,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        log.info("[CORRELATION:{}] Received Apigee API call request for endpoint: {}", actualCorrelationId, endpoint);
        
        return bridgeServiceFacade.callApigeeApi(endpoint, method, payload, authScope, actualCorrelationId)
                .thenApply(response -> {
                    log.info("[CORRELATION:{}] Apigee API call completed with status: {}", actualCorrelationId, response.getStatusCode());
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Call Fenergo API asynchronously via proxy
     */
    @PostMapping("/fenergo/proxy")
    public CompletableFuture<ResponseEntity<BridgeResponse>> callFenergoApi(
            @RequestParam String proxyUrl,
            @RequestParam String actualEndpoint,
            @RequestParam(defaultValue = "POST") String method,
            @RequestParam(defaultValue = "fenergo-api") String authScope,
            @RequestBody(required = false) Object payload,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        log.info("[CORRELATION:{}] Received Fenergo API call request via proxy: {} -> {}", actualCorrelationId, proxyUrl, actualEndpoint);
        
        return bridgeServiceFacade.callFenergoApi(proxyUrl, actualEndpoint, method, payload, authScope, actualCorrelationId)
                .thenApply(response -> {
                    log.info("[CORRELATION:{}] Fenergo API call completed with status: {}", actualCorrelationId, response.getStatusCode());
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Call any API asynchronously with full control
     */
    @PostMapping("/call")
    public CompletableFuture<ResponseEntity<BridgeResponse>> callApi(
            @RequestBody BridgeRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        request.setCorrelationId(actualCorrelationId);
        
        log.info("[CORRELATION:{}] Received generic API call request for provider: {}", actualCorrelationId, request.getApiProvider());
        
        return bridgeServiceFacade.callApi(request)
                .thenApply(response -> {
                    log.info("[CORRELATION:{}] Generic API call completed with status: {}", actualCorrelationId, response.getStatusCode());
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Chain multiple API calls asynchronously
     */
    @PostMapping("/chain")
    public CompletableFuture<ResponseEntity<BridgeResponse>> chainApiCalls(
            @RequestBody Map<String, BridgeRequest> requests,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        BridgeRequest firstRequest = requests.get("first");
        BridgeRequest secondRequest = requests.get("second");
        
        if (firstRequest == null || secondRequest == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(BridgeResponse.error(
                            400, "Both 'first' and 'second' requests are required",
                            "chain", ApiProvider.APIGEE, actualCorrelationId
                    ))
            );
        }
        
        firstRequest.setCorrelationId(actualCorrelationId);
        secondRequest.setCorrelationId(actualCorrelationId);
        
        log.info("[CORRELATION:{}] Received chained API call request", actualCorrelationId);
        
        return bridgeServiceFacade.chainApiCalls(firstRequest, secondRequest)
                .thenApply(response -> {
                    log.info("[CORRELATION:{}] Chained API calls completed with status: {}", actualCorrelationId, response.getStatusCode());
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Get bridge service status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "service", "Bridge Service",
                "status", "active",
                "async", true,
                "providers", new String[]{"APIGEE", "FENERGO"},
                "timestamp", System.currentTimeMillis()
        ));
    }
}
