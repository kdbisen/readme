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
public class FenergoJourneyService {
    
    private final FenergoProxyService fenergoProxyService;
    
    @Value("${fenergo.proxy.url:http://localhost:8080/api/v1/proxy}")
    private String fenergoProxyUrl;
    
    @Value("${fenergo.journey.info.url:https://fenergo.example.com/api/v1/journeys/info}")
    private String fenergoJourneyInfoUrl;
    
    @Value("${fenergo.journey.initiate.url:https://fenergo.example.com/api/v1/journeys/initiate}")
    private String fenergoJourneyInitiateUrl;
    
    @Value("${fenergo.journey.details.url:https://fenergo.example.com/api/v1/journeys/details}")
    private String fenergoJourneyDetailsUrl;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FenergoJourneyResponse {
        private boolean success;
        private String journeyId;
        private String clientId;
        private String journeyStatus;
        private String errorMessage;
        private long duration;
        private Map<String, Object> journeyData;
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getJourneyId() {
            return journeyId;
        }
        
        public String getClientId() {
            return clientId;
        }
        
        public String getJourneyStatus() {
            return journeyStatus;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public Map<String, Object> getJourneyData() {
            return journeyData;
        }
    }
    
    public FenergoJourneyResponse getJourneyInfo(String clientId, String processId) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[CORRELATION:{}] Calling Fenergo Journey Info API via proxy for clientId: {}", processId, clientId);
            
            String payload = String.format("{\"clientId\": \"%s\"}", clientId);
            
            ProxyResponse proxyResponse = fenergoProxyService.callFenergoApi(
                    fenergoProxyUrl,
                    fenergoJourneyInfoUrl,
                    HttpMethod.POST,
                    payload,
                    "JWT", // Auth type
                    "fenergo.journey.info" // Auth scope
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (proxyResponse.isSuccess()) {
                log.info("[CORRELATION:{}] Fenergo journey info retrieval successful - Duration: {}ms", processId, duration);
                
                String responseBody = proxyResponse.getResponseBody();
                String journeyId = extractJourneyId(responseBody);
                String journeyStatus = extractJourneyStatus(responseBody);
                
                return FenergoJourneyResponse.builder()
                        .success(true)
                        .journeyId(journeyId)
                        .clientId(clientId)
                        .journeyStatus(journeyStatus)
                        .errorMessage(null)
                        .duration(duration)
                        .journeyData(Map.of(
                                "processId", processId,
                                "responseBody", responseBody,
                                "proxyResponseTime", proxyResponse.getResponseTimeMs(),
                                "fenergoEndpoint", proxyResponse.getFenergoEndpoint()
                        ))
                        .build();
            } else {
                log.error("[CORRELATION:{}] Fenergo journey info retrieval failed: {}", processId, proxyResponse.getErrorMessage());
                return FenergoJourneyResponse.builder()
                        .success(false)
                        .journeyId(null)
                        .clientId(clientId)
                        .journeyStatus(null)
                        .errorMessage(proxyResponse.getErrorMessage())
                        .duration(duration)
                        .journeyData(Map.of(
                                "processId", processId,
                                "statusCode", proxyResponse.getStatusCode(),
                                "fenergoEndpoint", proxyResponse.getFenergoEndpoint()
                        ))
                        .build();
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Fenergo journey info retrieval error: {}", processId, e.getMessage(), e);
            return FenergoJourneyResponse.builder()
                    .success(false)
                    .journeyId(null)
                    .clientId(clientId)
                    .journeyStatus(null)
                    .errorMessage("Fenergo journey info retrieval failed: " + e.getMessage())
                    .duration(duration)
                    .journeyData(Map.of("processId", processId, "error", e.getClass().getSimpleName()))
                    .build();
        }
    }
    
    public FenergoJourneyResponse initiateJourney(String journeyId, String clientId, String processId) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[CORRELATION:{}] Calling Fenergo Journey Initiate API via proxy for journeyId: {}", processId, journeyId);
            
            String payload = String.format("{\"journeyId\": \"%s\", \"clientId\": \"%s\"}", journeyId, clientId);
            
            ProxyResponse proxyResponse = fenergoProxyService.callFenergoApi(
                    fenergoProxyUrl,
                    fenergoJourneyInitiateUrl,
                    HttpMethod.POST,
                    payload,
                    "JWT", // Auth type
                    "fenergo.journey.initiate" // Auth scope
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (proxyResponse.isSuccess()) {
                log.info("[CORRELATION:{}] Fenergo journey initiation successful - Duration: {}ms", processId, duration);
                
                String responseBody = proxyResponse.getResponseBody();
                String journeyStatus = extractJourneyStatus(responseBody);
                
                return FenergoJourneyResponse.builder()
                        .success(true)
                        .journeyId(journeyId)
                        .clientId(clientId)
                        .journeyStatus(journeyStatus)
                        .errorMessage(null)
                        .duration(duration)
                        .journeyData(Map.of(
                                "processId", processId,
                                "responseBody", responseBody,
                                "proxyResponseTime", proxyResponse.getResponseTimeMs(),
                                "fenergoEndpoint", proxyResponse.getFenergoEndpoint()
                        ))
                        .build();
            } else {
                log.error("[CORRELATION:{}] Fenergo journey initiation failed: {}", processId, proxyResponse.getErrorMessage());
                return FenergoJourneyResponse.builder()
                        .success(false)
                        .journeyId(journeyId)
                        .clientId(clientId)
                        .journeyStatus(null)
                        .errorMessage(proxyResponse.getErrorMessage())
                        .duration(duration)
                        .journeyData(Map.of(
                                "processId", processId,
                                "statusCode", proxyResponse.getStatusCode(),
                                "fenergoEndpoint", proxyResponse.getFenergoEndpoint()
                        ))
                        .build();
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Fenergo journey initiation error: {}", processId, e.getMessage(), e);
            return FenergoJourneyResponse.builder()
                    .success(false)
                    .journeyId(journeyId)
                    .clientId(clientId)
                    .journeyStatus(null)
                    .errorMessage("Fenergo journey initiation failed: " + e.getMessage())
                    .duration(duration)
                    .journeyData(Map.of("processId", processId, "error", e.getClass().getSimpleName()))
                    .build();
        }
    }
    
    public FenergoJourneyResponse getJourneyDetails(String journeyId, String processId) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[CORRELATION:{}] Calling Fenergo Journey Details API via proxy for journeyId: {}", processId, journeyId);
            
            String payload = String.format("{\"journeyId\": \"%s\"}", journeyId);
            
            ProxyResponse proxyResponse = fenergoProxyService.callFenergoApi(
                    fenergoProxyUrl,
                    fenergoJourneyDetailsUrl,
                    HttpMethod.POST,
                    payload,
                    "JWT", // Auth type
                    "fenergo.journey.details" // Auth scope
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (proxyResponse.isSuccess()) {
                log.info("[CORRELATION:{}] Fenergo journey details retrieval successful - Duration: {}ms", processId, duration);
                
                String responseBody = proxyResponse.getResponseBody();
                String journeyStatus = extractJourneyStatus(responseBody);
                String clientId = extractClientId(responseBody);
                
                return FenergoJourneyResponse.builder()
                        .success(true)
                        .journeyId(journeyId)
                        .clientId(clientId)
                        .journeyStatus(journeyStatus)
                        .errorMessage(null)
                        .duration(duration)
                        .journeyData(Map.of(
                                "processId", processId,
                                "responseBody", responseBody,
                                "proxyResponseTime", proxyResponse.getResponseTimeMs(),
                                "fenergoEndpoint", proxyResponse.getFenergoEndpoint()
                        ))
                        .build();
            } else {
                log.error("[CORRELATION:{}] Fenergo journey details retrieval failed: {}", processId, proxyResponse.getErrorMessage());
                return FenergoJourneyResponse.builder()
                        .success(false)
                        .journeyId(journeyId)
                        .clientId(null)
                        .journeyStatus(null)
                        .errorMessage(proxyResponse.getErrorMessage())
                        .duration(duration)
                        .journeyData(Map.of(
                                "processId", processId,
                                "statusCode", proxyResponse.getStatusCode(),
                                "fenergoEndpoint", proxyResponse.getFenergoEndpoint()
                        ))
                        .build();
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CORRELATION:{}] Fenergo journey details retrieval error: {}", processId, e.getMessage(), e);
            return FenergoJourneyResponse.builder()
                    .success(false)
                    .journeyId(journeyId)
                    .clientId(null)
                    .journeyStatus(null)
                    .errorMessage("Fenergo journey details retrieval failed: " + e.getMessage())
                    .duration(duration)
                    .journeyData(Map.of("processId", processId, "error", e.getClass().getSimpleName()))
                    .build();
        }
    }
    
    private String extractJourneyId(String responseBody) {
        if (responseBody.contains("\"journeyId\"")) {
            int start = responseBody.indexOf("\"journeyId\":\"") + 13;
            int end = responseBody.indexOf("\"", start);
            if (start > 12 && end > start) {
                return responseBody.substring(start, end);
            }
        }
        return "JOURNEY-" + System.currentTimeMillis();
    }
    
    private String extractJourneyStatus(String responseBody) {
        if (responseBody.contains("\"status\"")) {
            int start = responseBody.indexOf("\"status\":\"") + 10;
            int end = responseBody.indexOf("\"", start);
            if (start > 9 && end > start) {
                return responseBody.substring(start, end);
            }
        }
        return "INITIATED";
    }
    
    private String extractClientId(String responseBody) {
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