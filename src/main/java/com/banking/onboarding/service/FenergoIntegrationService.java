package com.banking.onboarding.service;

import com.banking.onboarding.model.EntityData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FenergoIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${fenergo.api.base-url}")
    private String fenergoBaseUrl;
    
    @Value("${fenergo.api.timeout}")
    private int timeoutMs;
    
    @Value("${fenergo.api.retry-attempts}")
    private int retryAttempts;
    
    public Map<String, Object> submitEntityToFenergo(EntityData entityData, String processId) {
        log.info("Submitting entity to Fenergo for process: {}", processId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = createFenergoRequest(entityData, processId);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String url = fenergoBaseUrl + "/entities/onboard";
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    request, 
                    Map.class
            );
            
            log.info("Successfully submitted entity to Fenergo for process: {}", processId);
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to submit entity to Fenergo for process: {}, error: {}", 
                    processId, e.getMessage(), e);
            
            return Map.of(
                    "error", true,
                    "message", e.getMessage(),
                    "processId", processId
            );
        }
    }
    
    public Map<String, Object> getEntityStatusFromFenergo(String fenergoEntityId) {
        log.info("Retrieving entity status from Fenergo for entity: {}", fenergoEntityId);
        
        try {
            String url = fenergoBaseUrl + "/entities/" + fenergoEntityId + "/status";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            log.info("Successfully retrieved entity status from Fenergo for entity: {}", fenergoEntityId);
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to retrieve entity status from Fenergo for entity: {}, error: {}", 
                    fenergoEntityId, e.getMessage(), e);
            
            return Map.of(
                    "error", true,
                    "message", e.getMessage(),
                    "entityId", fenergoEntityId
            );
        }
    }
    
    private Map<String, Object> createFenergoRequest(EntityData entityData, String processId) {
        Map<String, Object> request = new HashMap<>();
        request.put("processId", processId);
        request.put("entityId", entityData.getEntityId());
        request.put("entityName", entityData.getEntityName());
        request.put("entityType", entityData.getEntityType().toString());
        request.put("registrationNumber", entityData.getRegistrationNumber());
        request.put("taxId", entityData.getTaxId());
        request.put("incorporationDate", entityData.getIncorporationDate().toString());
        request.put("countryOfIncorporation", entityData.getCountryOfIncorporation());
        request.put("businessAddress", entityData.getBusinessAddress());
        request.put("legalAddress", entityData.getLegalAddress());
        
        // Primary contact
        Map<String, Object> primaryContact = new HashMap<>();
        primaryContact.put("firstName", entityData.getPrimaryContact().getFirstName());
        primaryContact.put("lastName", entityData.getPrimaryContact().getLastName());
        primaryContact.put("email", entityData.getPrimaryContact().getEmail());
        primaryContact.put("phoneNumber", entityData.getPrimaryContact().getPhoneNumber());
        request.put("primaryContact", primaryContact);
        
        // Risk profile
        Map<String, Object> riskProfile = new HashMap<>();
        riskProfile.put("riskLevel", entityData.getRiskProfile().getRiskLevel().toString());
        riskProfile.put("riskFactors", entityData.getRiskProfile().getRiskFactors());
        request.put("riskProfile", riskProfile);
        
        // Compliance info
        Map<String, Object> complianceInfo = new HashMap<>();
        complianceInfo.put("overallStatus", entityData.getComplianceInfo().getOverallStatus().toString());
        complianceInfo.put("regulatoryRequirements", entityData.getComplianceInfo().getRegulatoryRequirements());
        request.put("complianceInfo", complianceInfo);
        
        // Documents
        request.put("documents", entityData.getDocuments().stream()
                .map(doc -> {
                    Map<String, Object> docMap = new HashMap<>();
                    docMap.put("documentId", doc.getDocumentId());
                    docMap.put("documentType", doc.getDocumentType().toString());
                    docMap.put("documentName", doc.getDocumentName());
                    docMap.put("status", doc.getStatus().toString());
                    return docMap;
                })
                .toList());
        
        return request;
    }
}
