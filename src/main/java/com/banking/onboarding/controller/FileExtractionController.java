package com.banking.onboarding.controller;

import com.banking.onboarding.service.CustomerDataFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controller for file extraction operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/file-extraction")
@RequiredArgsConstructor
public class FileExtractionController {

    private final CustomerDataFileService customerDataFileService;

    /**
     * Extract customer data from JSON file
     * 
     * @param filePath Path to the JSON file
     * @return Extracted customer data
     */
    @GetMapping("/json/{filePath}")
    public ResponseEntity<Map<String, Object>> extractFromJsonFile(@PathVariable String filePath) {
        try {
            log.info("Extracting data from JSON file: {}", filePath);
            
            Optional<Map<String, Object>> data = customerDataFileService.processCustomerDataFromJson(filePath);
            
            if (data.isPresent()) {
                return ResponseEntity.ok(data.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "File not found or invalid format", "filePath", filePath));
            }
            
        } catch (Exception e) {
            log.error("Error extracting data from JSON file: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Extract customer data from XML file
     * 
     * @param filePath Path to the XML file
     * @return Extracted customer data
     */
    @GetMapping("/xml/{filePath}")
    public ResponseEntity<Map<String, Object>> extractFromXmlFile(@PathVariable String filePath) {
        try {
            log.info("Extracting data from XML file: {}", filePath);
            
            Optional<Map<String, Object>> data = customerDataFileService.processCustomerDataFromXml(filePath);
            
            if (data.isPresent()) {
                return ResponseEntity.ok(data.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "File not found or invalid format", "filePath", filePath));
            }
            
        } catch (Exception e) {
            log.error("Error extracting data from XML file: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Extract specific field from JSON file
     * 
     * @param filePath Path to the JSON file
     * @param fieldPath Dot-separated path to the field
     * @return Field value
     */
    @GetMapping("/json/{filePath}/field/{fieldPath}")
    public ResponseEntity<Object> extractFieldFromJsonFile(
            @PathVariable String filePath, 
            @PathVariable String fieldPath) {
        try {
            log.info("Extracting field '{}' from JSON file: {}", fieldPath, filePath);
            
            Optional<Object> fieldValue = customerDataFileService.getCustomerFieldFromJson(filePath, fieldPath);
            
            if (fieldValue.isPresent()) {
                return ResponseEntity.ok(Map.of("fieldPath", fieldPath, "value", fieldValue.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Field not found", "fieldPath", fieldPath, "filePath", filePath));
            }
            
        } catch (Exception e) {
            log.error("Error extracting field from JSON file: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Extract specific element from XML file
     * 
     * @param filePath Path to the XML file
     * @param elementPath Dot-separated path to the element
     * @return Element value
     */
    @GetMapping("/xml/{filePath}/element/{elementPath}")
    public ResponseEntity<Object> extractElementFromXmlFile(
            @PathVariable String filePath, 
            @PathVariable String elementPath) {
        try {
            log.info("Extracting element '{}' from XML file: {}", elementPath, filePath);
            
            Optional<Object> elementValue = customerDataFileService.getCustomerFieldFromXml(filePath, elementPath);
            
            if (elementValue.isPresent()) {
                return ResponseEntity.ok(Map.of("elementPath", elementPath, "value", elementValue.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Element not found", "elementPath", elementPath, "filePath", filePath));
            }
            
        } catch (Exception e) {
            log.error("Error extracting element from XML file: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Convert JSON file to XML format
     * 
     * @param filePath Path to the JSON file
     * @return XML content
     */
    @GetMapping("/json/{filePath}/convert-to-xml")
    public ResponseEntity<String> convertJsonToXml(@PathVariable String filePath) {
        try {
            log.info("Converting JSON file to XML: {}", filePath);
            
            Optional<String> xmlContent = customerDataFileService.convertCustomerDataToXml(filePath);
            
            if (xmlContent.isPresent()) {
                return ResponseEntity.ok()
                        .header("Content-Type", "application/xml")
                        .body(xmlContent.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("File not found or conversion failed");
            }
            
        } catch (Exception e) {
            log.error("Error converting JSON to XML: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Conversion failed: " + e.getMessage());
        }
    }

    /**
     * Convert XML file to JSON format
     * 
     * @param filePath Path to the XML file
     * @return JSON content
     */
    @GetMapping("/xml/{filePath}/convert-to-json")
    public ResponseEntity<String> convertXmlToJson(@PathVariable String filePath) {
        try {
            log.info("Converting XML file to JSON: {}", filePath);
            
            Optional<String> jsonContent = customerDataFileService.convertCustomerDataToJson(filePath);
            
            if (jsonContent.isPresent()) {
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json")
                        .body(jsonContent.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("File not found or conversion failed");
            }
            
        } catch (Exception e) {
            log.error("Error converting XML to JSON: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Conversion failed: " + e.getMessage());
        }
    }

    /**
     * Get file information
     * 
     * @param filePath Path to the file
     * @return File information
     */
    @GetMapping("/info/{filePath}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String filePath) {
        try {
            log.info("Getting file information: {}", filePath);
            
            Map<String, Object> fileInfo = customerDataFileService.getFileInfo(filePath);
            return ResponseEntity.ok(fileInfo);
            
        } catch (Exception e) {
            log.error("Error getting file information: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Get customer name from file
     * 
     * @param filePath Path to the file
     * @param isXmlFile true if XML file, false if JSON file
     * @return Customer name
     */
    @GetMapping("/customer-name/{filePath}")
    public ResponseEntity<Object> getCustomerName(
            @PathVariable String filePath, 
            @RequestParam(defaultValue = "false") boolean isXmlFile) {
        try {
            log.info("Getting customer name from {} file: {}", isXmlFile ? "XML" : "JSON", filePath);
            
            Optional<String> customerName = customerDataFileService.getCustomerName(filePath, isXmlFile);
            
            if (customerName.isPresent()) {
                return ResponseEntity.ok(Map.of("customerName", customerName.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer name not found", "filePath", filePath));
            }
            
        } catch (Exception e) {
            log.error("Error getting customer name from file: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
}
