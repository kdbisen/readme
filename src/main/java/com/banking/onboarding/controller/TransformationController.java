package com.banking.onboarding.controller;

import com.banking.onboarding.bridge.BridgeResponse;
import com.banking.onboarding.service.CorrelationIdService;
import com.banking.onboarding.service.TransformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Transformation Controller
 * Handles XML/JSON transformation requests via Apigee service
 */
@Slf4j
@RestController
@RequestMapping("/api/transformation")
@RequiredArgsConstructor
public class TransformationController {

    private final TransformationService transformationService;
    private final CorrelationIdService correlationIdService;

    /**
     * Transform XML to JSON via Apigee transformation service
     */
    @PostMapping("/xml-to-json")
    public CompletableFuture<ResponseEntity<BridgeResponse>> transformXmlToJson(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        String xmlData = request.get("xmlData");
        
        if (xmlData == null || xmlData.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(BridgeResponse.error(
                            400, "xmlData is required", "xml-to-json", 
                            com.banking.onboarding.bridge.ApiProvider.APIGEE, actualCorrelationId
                    ))
            );
        }
        
        log.info("[CORRELATION:{}] Received XML to JSON transformation request", actualCorrelationId);
        
        return transformationService.transformXmlToJson(xmlData, actualCorrelationId)
                .thenApply(response -> {
                    log.info("[CORRELATION:{}] XML to JSON transformation completed with status: {}", 
                            actualCorrelationId, response.getStatusCode());
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Transform JSON to XML via Apigee transformation service
     */
    @PostMapping("/json-to-xml")
    public CompletableFuture<ResponseEntity<BridgeResponse>> transformJsonToXml(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        String jsonData = request.get("jsonData");
        
        if (jsonData == null || jsonData.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(BridgeResponse.error(
                            400, "jsonData is required", "json-to-xml", 
                            com.banking.onboarding.bridge.ApiProvider.APIGEE, actualCorrelationId
                    ))
            );
        }
        
        log.info("[CORRELATION:{}] Received JSON to XML transformation request", actualCorrelationId);
        
        return transformationService.transformJsonToXml(jsonData, actualCorrelationId)
                .thenApply(response -> {
                    log.info("[CORRELATION:{}] JSON to XML transformation completed with status: {}", 
                            actualCorrelationId, response.getStatusCode());
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Transform data with custom input/output formats
     */
    @PostMapping("/transform")
    public CompletableFuture<ResponseEntity<BridgeResponse>> transformData(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        String inputData = request.get("inputData");
        String inputFormat = request.get("inputFormat");
        String outputFormat = request.get("outputFormat");
        
        if (inputData == null || inputFormat == null || outputFormat == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(BridgeResponse.error(
                            400, "inputData, inputFormat, and outputFormat are required", "transform", 
                            com.banking.onboarding.bridge.ApiProvider.APIGEE, actualCorrelationId
                    ))
            );
        }
        
        log.info("[CORRELATION:{}] Received custom transformation request: {} -> {}", 
                actualCorrelationId, inputFormat, outputFormat);
        
        return transformationService.transformData(inputData, inputFormat, outputFormat, actualCorrelationId)
                .thenApply(response -> {
                    log.info("[CORRELATION:{}] Custom transformation completed with status: {}", 
                            actualCorrelationId, response.getStatusCode());
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Get transformation service status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "service", "Transformation Service",
                "provider", "Apigee",
                "status", "active",
                "async", true,
                "supportedFormats", new String[]{"XML", "JSON"},
                "timestamp", System.currentTimeMillis()
        ));
    }
}
