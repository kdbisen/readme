package com.banking.onboarding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Validation Service for onboarding data
 */
@Slf4j
@Service
public class ValidationService {
    
    /**
     * Validate XML data structure
     */
    public ValidationResult validateXmlData(String xmlData) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            if (xmlData == null || xmlData.trim().isEmpty()) {
                errors.add("XML data is null or empty");
                return new ValidationResult(false, errors, warnings);
            }
            
            // Parse XML to check structure
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xmlData.getBytes()));
            
            // Check for required elements
            NodeList customerNodes = document.getElementsByTagName("customer");
            if (customerNodes.getLength() == 0) {
                errors.add("Missing required 'customer' element");
            } else {
                Element customer = (Element) customerNodes.item(0);
                
                // Check required fields
                if (!hasElement(customer, "name")) {
                    errors.add("Missing required 'name' field");
                }
                if (!hasElement(customer, "email")) {
                    errors.add("Missing required 'email' field");
                }
                if (!hasElement(customer, "phone")) {
                    warnings.add("Missing optional 'phone' field");
                }
                
                // Validate email format
                String email = getElementText(customer, "email");
                if (email != null && !isValidEmail(email)) {
                    errors.add("Invalid email format: " + email);
                }
            }
            
            log.info("XML validation completed. Errors: {}, Warnings: {}", errors.size(), warnings.size());
            
        } catch (Exception e) {
            errors.add("Invalid XML format: " + e.getMessage());
            log.error("XML validation failed", e);
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Validate JSON data structure
     */
    public ValidationResult validateJsonData(String jsonData) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            if (jsonData == null || jsonData.trim().isEmpty()) {
                errors.add("JSON data is null or empty");
                return new ValidationResult(false, errors, warnings);
            }
            
            // Basic JSON structure validation
            if (!jsonData.trim().startsWith("{") || !jsonData.trim().endsWith("}")) {
                errors.add("Invalid JSON format - must be an object");
            }
            
            // Check for required fields using simple string matching
            if (!jsonData.contains("\"name\"")) {
                errors.add("Missing required 'name' field");
            }
            if (!jsonData.contains("\"email\"")) {
                errors.add("Missing required 'email' field");
            }
            if (!jsonData.contains("\"phone\"")) {
                warnings.add("Missing optional 'phone' field");
            }
            
            log.info("JSON validation completed. Errors: {}, Warnings: {}", errors.size(), warnings.size());
            
        } catch (Exception e) {
            errors.add("JSON validation error: " + e.getMessage());
            log.error("JSON validation failed", e);
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Validate process request
     */
    public ValidationResult validateProcessRequest(String xmlData, String requestType) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Validate XML data
        ValidationResult xmlValidation = validateXmlData(xmlData);
        errors.addAll(xmlValidation.getErrors());
        warnings.addAll(xmlValidation.getWarnings());
        
        // Validate request type
        if (requestType == null || requestType.trim().isEmpty()) {
            errors.add("Request type is required");
        } else if (!isValidRequestType(requestType)) {
            errors.add("Invalid request type: " + requestType);
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    private boolean hasElement(Element parent, String tagName) {
        return parent.getElementsByTagName(tagName).getLength() > 0;
    }
    
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    private boolean isValidRequestType(String requestType) {
        return requestType != null && 
               (requestType.equals("ADD_KYC") || 
                requestType.equals("UPDATE_KYC") || 
                requestType.equals("VERIFY_KYC"));
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
            this.warnings = warnings != null ? warnings : new ArrayList<>();
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
        
        public String getWarningMessage() {
            return String.join("; ", warnings);
        }
    }
}
