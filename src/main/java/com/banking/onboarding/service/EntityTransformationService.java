package com.banking.onboarding.service;

import com.banking.onboarding.model.EntityData;
import com.banking.onboarding.model.OnboardingProcess;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntityTransformationService {
    
    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;
    
    public EntityData transformXmlToEntityData(String xmlData) {
        try {
            log.info("Starting XML to EntityData transformation");
            
            // Parse XML to Map first
            Map<String, Object> xmlMap = xmlMapper.readValue(xmlData, Map.class);
            
            // Transform to EntityData object
            EntityData entityData = objectMapper.convertValue(xmlMap, EntityData.class);
            
            log.info("Successfully transformed XML to EntityData for entity: {}", entityData.getEntityId());
            return entityData;
            
        } catch (Exception e) {
            log.error("Failed to transform XML to EntityData: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to transform XML to EntityData", e);
        }
    }
    
    public Map<String, Object> transformXmlToJson(String xmlData) {
        try {
            log.info("Starting XML to JSON transformation");
            
            // Parse XML to Map
            Map<String, Object> xmlMap = xmlMapper.readValue(xmlData, Map.class);
            
            // Convert to JSON string and back to Map to ensure proper JSON structure
            String jsonString = objectMapper.writeValueAsString(xmlMap);
            Map<String, Object> jsonMap = objectMapper.readValue(jsonString, Map.class);
            
            log.info("Successfully transformed XML to JSON");
            return jsonMap;
            
        } catch (Exception e) {
            log.error("Failed to transform XML to JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to transform XML to JSON", e);
        }
    }
    
    public boolean validateEntityData(EntityData entityData) {
        try {
            log.info("Starting entity data validation for entity: {}", entityData.getEntityId());
            
            // Basic validation rules
            if (entityData.getEntityId() == null || entityData.getEntityId().trim().isEmpty()) {
                log.error("Entity ID is required");
                return false;
            }
            
            if (entityData.getEntityName() == null || entityData.getEntityName().trim().isEmpty()) {
                log.error("Entity name is required");
                return false;
            }
            
            if (entityData.getEntityType() == null) {
                log.error("Entity type is required");
                return false;
            }
            
            if (entityData.getRegistrationNumber() == null || entityData.getRegistrationNumber().trim().isEmpty()) {
                log.error("Registration number is required");
                return false;
            }
            
            if (entityData.getPrimaryContact() == null) {
                log.error("Primary contact is required");
                return false;
            }
            
            if (entityData.getPrimaryContact().getEmail() == null || 
                !entityData.getPrimaryContact().getEmail().contains("@")) {
                log.error("Valid primary contact email is required");
                return false;
            }
            
            if (entityData.getRiskProfile() == null) {
                log.error("Risk profile is required");
                return false;
            }
            
            if (entityData.getComplianceInfo() == null) {
                log.error("Compliance info is required");
                return false;
            }
            
            log.info("Entity data validation successful for entity: {}", entityData.getEntityId());
            return true;
            
        } catch (Exception e) {
            log.error("Error during entity data validation: {}", e.getMessage(), e);
            return false;
        }
    }
}
