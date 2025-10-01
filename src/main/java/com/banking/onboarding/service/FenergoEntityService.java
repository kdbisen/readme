package com.banking.onboarding.service;

import com.banking.onboarding.proxy.FenergoProxyService;
import com.banking.onboarding.proxy.ProxyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FenergoEntityService {
    
    private final FenergoProxyService fenergoProxyService;
    
    @Value("${fenergo.proxy.url:http://localhost:8080/api/v1/proxy}")
    private String fenergoProxyUrl;
    
    @Value("${fenergo.entity.create.url:https://fenergo.example.com/api/v1/entities}")
    private String fenergoEntityCreateUrl;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FenergoEntityResponse {
        private boolean success;
        private String entityId;
        private String clientId;
        private String errorMessage;
        private long duration;
        private Map<String, Object> entityData;
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getEntityId() {
            return entityId;
        }
        
        public String getClientId() {
            return clientId;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public Map<String, Object> getEntityData() {
            return entityData;
        }
    }
    
    public FenergoEntityResponse createEntity(String jsonPayload, String processId) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[CORRELATION:{}] Calling Fenergo Entity Create API via proxy", processId);
            
            // Call Fenergo Entity Create API through proxy
            ProxyResponse proxyResponse = fenergoProxyService.callFenergoApi(
                    fenergoProxyUrl,
                    fenergoEntityCreateUrl,
                    HttpMethod.POST,
                    jsonPayload,
                    "JWT", // Auth type
                    "fenergo.entity.create" // Auth scope
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (proxyResponse.isSuccess()) {
                log.info("[CORRELATION:{}] Fenergo entity creation successful - Duration: {}ms", processId, duration);
                
                // Parse response to extract entity and client IDs
                String responseBody = proxyResponse.getResponseBody();
                String entityId = extractEntityId(responseBody);
                String clientId = extractClientId(responseBody);
                
                return FenergoEntityResponse.builder()
                        .success(true)
                        .entityId(entityId)
                        .clientId(clientId)
                        .errorMessage(null)
                        .duration(duration)
                        .entityData(Map.of(
                                "processId", processId,
                                "responseBody", responseBody,
                                "proxyResponseTime", proxyResponse.getResponseTimeMs(),
                                "fenergoEndpoint", proxyResponse.getFenergoEndpoint()
                        ))
                        .build();
            } else {
                log.error("[CORRELATION:{}] Fenergo entity creation failed: {}", processId, proxyResponse.getErrorMessage());
                return FenergoEntityResponse.builder()
                        .success(false)
                        .entityId(null)
                        .clientId(null)
                        .errorMessage(proxyResponse.getErrorMessage())
                        .duration(duration)
                        .entityData(Map.of(
                                "processId", processId,
                                "statusCode", proxyResponse.getStatusCode(),
                                "fenergoEndpoint", proxyResponse.getFenergoEndpoint()
                        ))
                        .build();
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Fenergo entity creation error: {}", processId, e.getMessage(), e);
            return FenergoEntityResponse.builder()
                    .success(false)
                    .entityId(null)
                    .clientId(null)
                    .errorMessage("Fenergo entity creation failed: " + e.getMessage())
                    .duration(duration)
                    .entityData(Map.of("processId", processId, "error", e.getClass().getSimpleName()))
                    .build();
        }
    }
    
    private String extractEntityId(String responseBody) {
        // Simple JSON parsing - in production, use proper JSON parser
        if (responseBody.contains("\"entityId\"")) {
            int start = responseBody.indexOf("\"entityId\":\"") + 12;
            int end = responseBody.indexOf("\"", start);
            if (start > 11 && end > start) {
                return responseBody.substring(start, end);
            }
        }
        return "ENTITY-" + System.currentTimeMillis();
    }
    
    private String extractClientId(String responseBody) {
        // Simple JSON parsing - in production, use proper JSON parser
        if (responseBody.contains("\"clientId\"")) {
            int start = responseBody.indexOf("\"clientId\":\"") + 12;
            int end = responseBody.indexOf("\"", start);
            if (start > 11 && end > start) {
                return responseBody.substring(start, end);
            }
        }
        return "CLIENT-" + System.currentTimeMillis();
    }
}