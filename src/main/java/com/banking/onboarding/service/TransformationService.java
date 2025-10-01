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

import java.util.concurrent.CompletableFuture;

/**
 * Transformation Service
 * Handles XML to JSON transformation via Apigee service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransformationService {

    private final BridgeServiceFacade bridgeServiceFacade;
    private final CorrelationIdService correlationIdService;

    @Value("${apigee.transformation.endpoint:https://apigee-transformation-service.com/api/v1/transform}")
    private String transformationEndpoint;

    @Value("${apigee.transformation.auth-scope:transformation-api}")
    private String transformationAuthScope;

    /**
     * Transform XML to JSON via Apigee transformation service
     */
    public CompletableFuture<BridgeResponse> transformXmlToJson(String xmlData, String correlationId) {
        log.info("[CORRELATION:{}] Initiating XML to JSON transformation via Apigee service", correlationId);
        
        BridgeRequest request = BridgeRequest.builder()
                .endpoint(transformationEndpoint)
                .method("POST")
                .payload(Map.of("xmlData", xmlData))
                .apiProvider(ApiProvider.APIGEE)
                .authScope(transformationAuthScope)
                .correlationId(correlationId)
                .headers(Map.of(
                    "Content-Type", "application/json",
                    "X-Service-Type", "transformation",
                    "X-Input-Format", "XML",
                    "X-Output-Format", "JSON"
                ))
                .build();

        return bridgeServiceFacade.callApi(request)
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        log.info("[CORRELATION:{}] XML to JSON transformation completed successfully", correlationId);
                    } else {
                        log.error("[CORRELATION:{}] XML to JSON transformation failed: {}", correlationId, response.getErrorMessage());
                    }
                    return response;
                });
    }

    /**
     * Transform JSON to XML via Apigee transformation service
     */
    public CompletableFuture<BridgeResponse> transformJsonToXml(String jsonData, String correlationId) {
        log.info("[CORRELATION:{}] Initiating JSON to XML transformation via Apigee service", correlationId);
        
        BridgeRequest request = BridgeRequest.builder()
                .endpoint(transformationEndpoint)
                .method("POST")
                .payload(Map.of("jsonData", jsonData))
                .apiProvider(ApiProvider.APIGEE)
                .authScope(transformationAuthScope)
                .correlationId(correlationId)
                .headers(Map.of(
                    "Content-Type", "application/json",
                    "X-Service-Type", "transformation",
                    "X-Input-Format", "JSON",
                    "X-Output-Format", "XML"
                ))
                .build();

        return bridgeServiceFacade.callApi(request)
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        log.info("[CORRELATION:{}] JSON to XML transformation completed successfully", correlationId);
                    } else {
                        log.error("[CORRELATION:{}] JSON to XML transformation failed: {}", correlationId, response.getErrorMessage());
                    }
                    return response;
                });
    }

    /**
     * Transform data with custom transformation rules
     */
    public CompletableFuture<BridgeResponse> transformData(String inputData, String inputFormat, String outputFormat, String correlationId) {
        log.info("[CORRELATION:{}] Initiating custom transformation: {} -> {} via Apigee service", 
                correlationId, inputFormat, outputFormat);
        
        BridgeRequest request = BridgeRequest.builder()
                .endpoint(transformationEndpoint)
                .method("POST")
                .payload(Map.of(
                    "inputData", inputData,
                    "inputFormat", inputFormat,
                    "outputFormat", outputFormat
                ))
                .apiProvider(ApiProvider.APIGEE)
                .authScope(transformationAuthScope)
                .correlationId(correlationId)
                .headers(Map.of(
                    "Content-Type", "application/json",
                    "X-Service-Type", "transformation",
                    "X-Input-Format", inputFormat,
                    "X-Output-Format", outputFormat
                ))
                .build();

        return bridgeServiceFacade.callApi(request)
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        log.info("[CORRELATION:{}] Custom transformation completed successfully: {} -> {}", 
                                correlationId, inputFormat, outputFormat);
                    } else {
                        log.error("[CORRELATION:{}] Custom transformation failed: {}", correlationId, response.getErrorMessage());
                    }
                    return response;
                });
    }
}
