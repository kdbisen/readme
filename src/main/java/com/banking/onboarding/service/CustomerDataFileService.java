package com.banking.onboarding.service;

import com.banking.onboarding.util.FileExtractionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Service for processing customer data from JSON and XML files
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerDataFileService {

    private final FileExtractionUtils fileExtractionUtils;

    /**
     * Process customer data from JSON file for KYC submission
     * 
     * @param jsonFilePath Path to the JSON file containing customer data
     * @return Processed customer data as Map
     */
    public Optional<Map<String, Object>> processCustomerDataFromJson(String jsonFilePath) {
        log.info("Processing customer data from JSON file: {}", jsonFilePath);
        
        // Validate file exists and is readable
        if (!fileExtractionUtils.isFileReadable(jsonFilePath)) {
            log.error("JSON file is not readable: {}", jsonFilePath);
            return Optional.empty();
        }

        // Extract data from JSON file
        Optional<Map<String, Object>> customerData = fileExtractionUtils.extractFromJsonFile(jsonFilePath);
        
        if (customerData.isPresent()) {
            log.info("Successfully processed customer data from JSON file: {}", jsonFilePath);
            
            // Validate required fields
            Map<String, Object> data = customerData.get();
            if (isValidCustomerData(data)) {
                return customerData;
            } else {
                log.error("Invalid customer data structure in JSON file: {}", jsonFilePath);
                return Optional.empty();
            }
        } else {
            log.error("Failed to extract data from JSON file: {}", jsonFilePath);
            return Optional.empty();
        }
    }

    /**
     * Process customer data from XML file for KYC submission
     * 
     * @param xmlFilePath Path to the XML file containing customer data
     * @return Processed customer data as Map
     */
    public Optional<Map<String, Object>> processCustomerDataFromXml(String xmlFilePath) {
        log.info("Processing customer data from XML file: {}", xmlFilePath);
        
        // Validate file exists and is readable
        if (!fileExtractionUtils.isFileReadable(xmlFilePath)) {
            log.error("XML file is not readable: {}", xmlFilePath);
            return Optional.empty();
        }

        // Extract data from XML file
        Optional<Map<String, Object>> customerData = fileExtractionUtils.extractFromXmlFile(xmlFilePath);
        
        if (customerData.isPresent()) {
            log.info("Successfully processed customer data from XML file: {}", xmlFilePath);
            
            // Validate required fields
            Map<String, Object> data = customerData.get();
            if (isValidCustomerData(data)) {
                return customerData;
            } else {
                log.error("Invalid customer data structure in XML file: {}", xmlFilePath);
                return Optional.empty();
            }
        } else {
            log.error("Failed to extract data from XML file: {}", xmlFilePath);
            return Optional.empty();
        }
    }

    /**
     * Extract specific customer field from JSON file
     * 
     * @param jsonFilePath Path to the JSON file
     * @param fieldPath Dot-separated path to the field
     * @return Field value
     */
    public Optional<Object> getCustomerFieldFromJson(String jsonFilePath, String fieldPath) {
        log.info("Extracting field '{}' from JSON file: {}", fieldPath, jsonFilePath);
        return fileExtractionUtils.extractFieldFromJsonFile(jsonFilePath, fieldPath);
    }

    /**
     * Extract specific customer field from XML file
     * 
     * @param xmlFilePath Path to the XML file
     * @param elementPath Dot-separated path to the element
     * @return Element value
     */
    public Optional<Object> getCustomerFieldFromXml(String xmlFilePath, String elementPath) {
        log.info("Extracting element '{}' from XML file: {}", elementPath, xmlFilePath);
        return fileExtractionUtils.extractElementFromXmlFile(xmlFilePath, elementPath);
    }

    /**
     * Convert customer data from JSON to XML format
     * 
     * @param jsonFilePath Path to the JSON file
     * @return XML content
     */
    public Optional<String> convertCustomerDataToXml(String jsonFilePath) {
        log.info("Converting customer data from JSON to XML: {}", jsonFilePath);
        return fileExtractionUtils.convertJsonFileToXml(jsonFilePath);
    }

    /**
     * Convert customer data from XML to JSON format
     * 
     * @param xmlFilePath Path to the XML file
     * @return JSON content
     */
    public Optional<String> convertCustomerDataToJson(String xmlFilePath) {
        log.info("Converting customer data from XML to JSON: {}", xmlFilePath);
        return fileExtractionUtils.convertXmlFileToJson(xmlFilePath);
    }

    /**
     * Get customer name from file (works with both JSON and XML)
     * 
     * @param filePath Path to the file
     * @param isXmlFile true if XML file, false if JSON file
     * @return Customer name
     */
    public Optional<String> getCustomerName(String filePath, boolean isXmlFile) {
        String namePath = isXmlFile ? "personalInfo.firstName" : "customer.personalInfo.firstName";
        
        if (isXmlFile) {
            return fileExtractionUtils.extractElementFromXmlFile(filePath, namePath)
                    .map(Object::toString);
        } else {
            return fileExtractionUtils.extractFieldFromJsonFile(filePath, namePath)
                    .map(Object::toString);
        }
    }

    /**
     * Get customer email from file (works with both JSON and XML)
     * 
     * @param filePath Path to the file
     * @param isXmlFile true if XML file, false if JSON file
     * @return Customer email
     */
    public Optional<String> getCustomerEmail(String filePath, boolean isXmlFile) {
        String emailPath = isXmlFile ? "personalInfo.email" : "customer.personalInfo.email";
        
        if (isXmlFile) {
            return fileExtractionUtils.extractElementFromXmlFile(filePath, emailPath)
                    .map(Object::toString);
        } else {
            return fileExtractionUtils.extractFieldFromJsonFile(filePath, emailPath)
                    .map(Object::toString);
        }
    }

    /**
     * Validate customer data structure
     * 
     * @param data Customer data map
     * @return true if valid, false otherwise
     */
    private boolean isValidCustomerData(Map<String, Object> data) {
        // Basic validation - check if data contains customer information
        return data != null && !data.isEmpty() && 
               (data.containsKey("customer") || data.containsKey("personalInfo"));
    }

    /**
     * Get file information (size, readability)
     * 
     * @param filePath Path to the file
     * @return File information as Map
     */
    public Map<String, Object> getFileInfo(String filePath) {
        return Map.of(
            "filePath", filePath,
            "readable", fileExtractionUtils.isFileReadable(filePath),
            "sizeBytes", fileExtractionUtils.getFileSize(filePath).orElse(0L)
        );
    }
}
