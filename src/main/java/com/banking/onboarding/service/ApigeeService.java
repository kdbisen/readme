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
public class ApigeeService {
    
    private final FenergoProxyService fenergoProxyService;
    
    @Value("${apigee.proxy.url:http://localhost:8080/api/v1/proxy}")
    private String apigeeProxyUrl;
    
    @Value("${apigee.endpoint.url:https://apigee.example.com/transform}")
    private String apigeeEndpointUrl;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApigeeResponse {
        private boolean success;
        private String jsonPayload;
        private String errorMessage;
        private long duration;
        private Map<String, Object> metadata;
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getJsonPayload() {
            return jsonPayload;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }
    
    public ApigeeResponse transformXmlToJson(String xmlPayload, String processId) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[CORRELATION:{}] Calling Apigee API via proxy for XML to JSON transformation", processId);
            
            // Call Apigee API through proxy
            ProxyResponse proxyResponse = fenergoProxyService.callFenergoApi(
                    apigeeProxyUrl,
                    apigeeEndpointUrl,
                    HttpMethod.POST,
                    xmlPayload,
                    "JWT", // Auth type
                    "apigee.transform" // Auth scope
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (proxyResponse.isSuccess()) {
                log.info("[CORRELATION:{}] Apigee transformation successful - Duration: {}ms", processId, duration);
                return ApigeeResponse.builder()
                        .success(true)
                        .jsonPayload(proxyResponse.getResponseBody())
                        .errorMessage(null)
                        .duration(duration)
                        .metadata(Map.of(
                                "processId", processId,
                                "proxyResponseTime", proxyResponse.getResponseTimeMs(),
                                "fenergoEndpoint", proxyResponse.getFenergoEndpoint()
                        ))
                        .build();
            } else {
                log.error("[CORRELATION:{}] Apigee transformation failed: {}", processId, proxyResponse.getErrorMessage());
                return ApigeeResponse.builder()
                        .success(false)
                        .jsonPayload(null)
                        .errorMessage(proxyResponse.getErrorMessage())
                        .duration(duration)
                        .metadata(Map.of(
                                "processId", processId,
                                "statusCode", proxyResponse.getStatusCode(),
                                "fenergoEndpoint", proxyResponse.getFenergoEndpoint()
                        ))
                        .build();
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Apigee transformation error: {}", processId, e.getMessage(), e);
            return ApigeeResponse.builder()
                    .success(false)
                    .jsonPayload(null)
                    .errorMessage("Apigee transformation failed: " + e.getMessage())
                    .duration(duration)
                    .metadata(Map.of("processId", processId, "error", e.getClass().getSimpleName()))
                    .build();
        }
    }
}