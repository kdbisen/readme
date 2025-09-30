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
 * Service for Fenergo Entity operations via proxy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FenergoEntityService {

    private final WebClient webClient;
    private final CorrelationIdService correlationIdService;

    @Value("${fenergo.proxy.base-url:http://localhost:8080/fenergo-proxy}")
    private String fenergoProxyBaseUrl;

    @Value("${fenergo.proxy.timeout:30000}")
    private long fenergoTimeout;

    @Value("${fenergo.proxy.retry-count:3}")
    private int retryCount;

    /**
     * Create entity in Fenergo via proxy
     */
    public FenergoEntityResponse createEntity(String jsonPayload, String processId) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        long startTime = System.currentTimeMillis();
        
        log.info("[CORRELATION:{}] Starting Fenergo entity creation - ProcessId: {}", 
                correlationId, processId);
        
        try {
            // Prepare request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jsonPayload", jsonPayload);
            requestBody.put("processId", processId);
            requestBody.put("correlationId", correlationId);
            
            // Call Fenergo Entity Create API via proxy
            FenergoEntityResponse response = webClient
                    .post()
                    .uri(fenergoProxyBaseUrl + "/entity/create")
                    .header("X-Correlation-ID", correlationId)
                    .header("X-Process-ID", processId)
                    .header("X-Fenergo-Endpoint", "/api/v1/entities")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(FenergoEntityResponse.class)
                    .timeout(Duration.ofMillis(fenergoTimeout))
                    .retry(retryCount)
                    .block();
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (response != null && response.isSuccess()) {
                log.info("[CORRELATION:{}] Fenergo entity creation completed successfully - ProcessId: {}, EntityId: {}, Duration: {}ms", 
                        correlationId, processId, response.getEntityId(), duration);
                
                response.setProcessId(processId);
                response.setCorrelationId(correlationId);
                response.setDuration(duration);
                response.setStepName(StepName.FENERGO_ENTITY_CREATE.getValue());
                response.setStepStatus(StepStatus.COMPLETED.getValue());
                
                return response;
            } else {
                String errorMsg = response != null ? response.getErrorMessage() : "Unknown error";
                log.error("[CORRELATION:{}] Fenergo entity creation failed - ProcessId: {}, Error: {}", 
                        correlationId, processId, errorMsg);
                
                return createErrorResponse(processId, correlationId, errorMsg, duration);
            }
            
        } catch (WebClientResponseException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Fenergo entity creation API call failed - ProcessId: {}, Status: {}, Error: {}", 
                    correlationId, processId, e.getStatusCode(), e.getMessage());
            
            return createErrorResponse(processId, correlationId, 
                    "Fenergo entity creation API call failed: " + e.getMessage(), duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Unexpected error in Fenergo entity creation - ProcessId: {}", 
                    correlationId, processId, e);
            
            return createErrorResponse(processId, correlationId, 
                    "Unexpected error: " + e.getMessage(), duration);
        }
    }

    /**
     * Get entity details from Fenergo via proxy
     */
    public FenergoEntityResponse getEntityDetails(String entityId, String processId) {
        String correlationId = correlationIdService.getCurrentCorrelationId();
        long startTime = System.currentTimeMillis();
        
        log.info("[CORRELATION:{}] Getting Fenergo entity details - ProcessId: {}, EntityId: {}", 
                correlationId, processId, entityId);
        
        try {
            // Call Fenergo Entity Details API via proxy
            FenergoEntityResponse response = webClient
                    .get()
                    .uri(fenergoProxyBaseUrl + "/entity/details/{entityId}", entityId)
                    .header("X-Correlation-ID", correlationId)
                    .header("X-Process-ID", processId)
                    .header("X-Fenergo-Endpoint", "/api/v1/entities/" + entityId)
                    .retrieve()
                    .bodyToMono(FenergoEntityResponse.class)
                    .timeout(Duration.ofMillis(fenergoTimeout))
                    .retry(retryCount)
                    .block();
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (response != null && response.isSuccess()) {
                log.info("[CORRELATION:{}] Fenergo entity details retrieved successfully - ProcessId: {}, EntityId: {}, Duration: {}ms", 
                        correlationId, processId, entityId, duration);
                
                response.setProcessId(processId);
                response.setCorrelationId(correlationId);
                response.setDuration(duration);
                
                return response;
            } else {
                String errorMsg = response != null ? response.getErrorMessage() : "Unknown error";
                log.error("[CORRELATION:{}] Fenergo entity details retrieval failed - ProcessId: {}, EntityId: {}, Error: {}", 
                        correlationId, processId, entityId, errorMsg);
                
                return createErrorResponse(processId, correlationId, errorMsg, duration);
            }
            
        } catch (WebClientResponseException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Fenergo entity details API call failed - ProcessId: {}, EntityId: {}, Status: {}, Error: {}", 
                    correlationId, processId, entityId, e.getStatusCode(), e.getMessage());
            
            return createErrorResponse(processId, correlationId, 
                    "Fenergo entity details API call failed: " + e.getMessage(), duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Unexpected error in Fenergo entity details retrieval - ProcessId: {}, EntityId: {}", 
                    correlationId, processId, entityId, e);
            
            return createErrorResponse(processId, correlationId, 
                    "Unexpected error: " + e.getMessage(), duration);
        }
    }

    /**
     * Check Fenergo service health via proxy
     */
    public boolean isFenergoServiceHealthy() {
        try {
            String correlationId = correlationIdService.getCurrentCorrelationId();
            
            webClient
                    .get()
                    .uri(fenergoProxyBaseUrl + "/health")
                    .header("X-Correlation-ID", correlationId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(5000))
                    .block();
            
            log.debug("[CORRELATION:{}] Fenergo service health check passed", correlationId);
            return true;
            
        } catch (Exception e) {
            log.warn("[CORRELATION:{}] Fenergo service health check failed: {}", 
                    correlationIdService.getCurrentCorrelationId(), e.getMessage());
            return false;
        }
    }

    /**
     * Create error response
     */
    private FenergoEntityResponse createErrorResponse(String processId, String correlationId, 
                                                    String errorMessage, long duration) {
        return FenergoEntityResponse.builder()
                .processId(processId)
                .correlationId(correlationId)
                .success(false)
                .errorMessage(errorMessage)
                .duration(duration)
                .stepName(StepName.FENERGO_ENTITY_CREATE.getValue())
                .stepStatus(StepStatus.FAILED.getValue())
                .build();
    }

    /**
     * Fenergo Entity response model
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FenergoEntityResponse {
        private String processId;
        private String correlationId;
        private boolean success;
        private String entityId;
        private String clientId;
        private String errorMessage;
        private long duration;
        private String stepName;
        private String stepStatus;
        private Map<String, Object> entityData;
        private Map<String, Object> metadata;
    }
}
