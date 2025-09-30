package com.banking.onboarding.service;

import com.banking.onboarding.model.StepName;
import com.banking.onboarding.model.StepStatus;
import com.banking.onboarding.service.CorrelationIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for Apigee API integration - XML to JSON transformation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApigeeService {

    private final WebClient webClient;
    private final CorrelationIdService correlationIdService;

    @Value("${apigee.api.base-url:http://localhost:8080/apigee}")
    private String apigeeBaseUrl;

    @Value("${apigee.api.timeout:30000}")
    private long apigeeTimeout;

    @Value("${apigee.api.retry-count:3}")
    private int retryCount;

    /**
     * Transform XML payload to JSON using Apigee API
     */
    public ApigeeResponse transformXmlToJson(String xmlPayload, String processId) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        long startTime = System.currentTimeMillis();
        
        log.info("[CORRELATION:{}] Starting Apigee XML to JSON transformation - ProcessId: {}", 
                correlationId, processId);
        
        try {
            // Prepare request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("xmlPayload", xmlPayload);
            requestBody.put("processId", processId);
            requestBody.put("correlationId", correlationId);
            
            // Call Apigee API
            ApigeeResponse response = webClient
                    .post()
                    .uri(apigeeBaseUrl + "/transform/xml-to-json")
                    .header("X-Correlation-ID", correlationId)
                    .header("X-Process-ID", processId)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ApigeeResponse.class)
                    .timeout(Duration.ofMillis(apigeeTimeout))
                    .retry(retryCount)
                    .block();
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (response != null && response.isSuccess()) {
                log.info("[CORRELATION:{}] Apigee transformation completed successfully - ProcessId: {}, Duration: {}ms", 
                        correlationId, processId, duration);
                
                response.setProcessId(processId);
                response.setCorrelationId(correlationId);
                response.setDuration(duration);
                response.setStepName(StepName.APIGEE_TRANSFORMATION.getValue());
                response.setStepStatus(StepStatus.COMPLETED.getValue());
                
                return response;
            } else {
                String errorMsg = response != null ? response.getErrorMessage() : "Unknown error";
                log.error("[CORRELATION:{}] Apigee transformation failed - ProcessId: {}, Error: {}", 
                        correlationId, processId, errorMsg);
                
                return createErrorResponse(processId, correlationId, errorMsg, duration);
            }
            
        } catch (WebClientResponseException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Apigee API call failed - ProcessId: {}, Status: {}, Error: {}", 
                    correlationId, processId, e.getStatusCode(), e.getMessage());
            
            return createErrorResponse(processId, correlationId, 
                    "Apigee API call failed: " + e.getMessage(), duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Unexpected error in Apigee transformation - ProcessId: {}", 
                    correlationId, processId, e);
            
            return createErrorResponse(processId, correlationId, 
                    "Unexpected error: " + e.getMessage(), duration);
        }
    }

    /**
     * Check Apigee service health
     */
    public boolean isApigeeServiceHealthy() {
        try {
            String correlationId = correlationIdService.getCurrentCorrelationId();
            
            webClient
                    .get()
                    .uri(apigeeBaseUrl + "/health")
                    .header("X-Correlation-ID", correlationId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(5000))
                    .block();
            
            log.debug("[CORRELATION:{}] Apigee service health check passed", correlationId);
            return true;
            
        } catch (Exception e) {
            log.warn("[CORRELATION:{}] Apigee service health check failed: {}", 
                    correlationIdService.getCurrentCorrelationId(), e.getMessage());
            return false;
        }
    }

    /**
     * Create error response
     */
    private ApigeeResponse createErrorResponse(String processId, String correlationId, 
                                             String errorMessage, long duration) {
        return ApigeeResponse.builder()
                .processId(processId)
                .correlationId(correlationId)
                .success(false)
                .errorMessage(errorMessage)
                .duration(duration)
                .stepName(StepName.APIGEE_TRANSFORMATION.getValue())
                .stepStatus(StepStatus.FAILED.getValue())
                .build();
    }

    /**
     * Apigee response model
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApigeeResponse {
        private String processId;
        private String correlationId;
        private boolean success;
        private String jsonPayload;
        private String errorMessage;
        private long duration;
        private String stepName;
        private String stepStatus;
        private Map<String, Object> metadata;
    }
}
