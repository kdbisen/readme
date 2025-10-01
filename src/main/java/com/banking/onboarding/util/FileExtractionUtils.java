package com.banking.onboarding.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for extracting data from JSON and XML files
 */
@Slf4j
@Component
public class FileExtractionUtils {

    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;

    public FileExtractionUtils() {
        this.jsonMapper = new ObjectMapper();
        this.xmlMapper = new XmlMapper();
    }

    /**
     * Extract data from JSON file by file path
     * 
     * @param filePath Path to the JSON file
     * @return Optional containing the parsed JSON as Map, empty if file not found or invalid
     */
    public Optional<Map<String, Object>> extractFromJsonFile(String filePath) {
        try {
            log.info("Extracting data from JSON file: {}", filePath);
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("JSON file not found: {}", filePath);
                return Optional.empty();
            }

            String jsonContent = Files.readString(path);
            Map<String, Object> data = jsonMapper.readValue(jsonContent, Map.class);
            
            log.info("Successfully extracted {} fields from JSON file: {}", data.size(), filePath);
            return Optional.of(data);
            
        } catch (IOException e) {
            log.error("Error reading JSON file: {}", filePath, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error parsing JSON file: {}", filePath, e);
            return Optional.empty();
        }
    }

    /**
     * Extract data from JSON file by file path with specific field extraction
     * 
     * @param filePath Path to the JSON file
     * @param fieldPath Dot-separated path to the field (e.g., "customer.personalInfo.firstName")
     * @return Optional containing the field value, empty if field not found
     */
    public Optional<Object> extractFieldFromJsonFile(String filePath, String fieldPath) {
        try {
            log.info("Extracting field '{}' from JSON file: {}", fieldPath, filePath);
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("JSON file not found: {}", filePath);
                return Optional.empty();
            }

            String jsonContent = Files.readString(path);
            JsonNode rootNode = jsonMapper.readTree(jsonContent);
            
            String[] pathParts = fieldPath.split("\\.");
            JsonNode currentNode = rootNode;
            
            for (String part : pathParts) {
                if (currentNode.has(part)) {
                    currentNode = currentNode.get(part);
                } else {
                    log.warn("Field '{}' not found in JSON file: {}", fieldPath, filePath);
                    return Optional.empty();
                }
            }
            
            Object value = jsonMapper.treeToValue(currentNode, Object.class);
            log.info("Successfully extracted field '{}' from JSON file: {}", fieldPath, filePath);
            return Optional.of(value);
            
        } catch (IOException e) {
            log.error("Error reading JSON file: {}", filePath, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error extracting field '{}' from JSON file: {}", fieldPath, filePath, e);
            return Optional.empty();
        }
    }

    /**
     * Extract data from XML file by file path
     * 
     * @param filePath Path to the XML file
     * @return Optional containing the parsed XML as Map, empty if file not found or invalid
     */
    public Optional<Map<String, Object>> extractFromXmlFile(String filePath) {
        try {
            log.info("Extracting data from XML file: {}", filePath);
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("XML file not found: {}", filePath);
                return Optional.empty();
            }

            String xmlContent = Files.readString(path);
            Map<String, Object> data = xmlMapper.readValue(xmlContent, Map.class);
            
            log.info("Successfully extracted {} fields from XML file: {}", data.size(), filePath);
            return Optional.of(data);
            
        } catch (IOException e) {
            log.error("Error reading XML file: {}", filePath, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error parsing XML file: {}", filePath, e);
            return Optional.empty();
        }
    }

    /**
     * Extract data from XML file by file path with specific element extraction
     * 
     * @param filePath Path to the XML file
     * @param elementPath Dot-separated path to the element (e.g., "customer.personalInfo.firstName")
     * @return Optional containing the element value, empty if element not found
     */
    public Optional<Object> extractElementFromXmlFile(String filePath, String elementPath) {
        try {
            log.info("Extracting element '{}' from XML file: {}", elementPath, filePath);
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("XML file not found: {}", filePath);
                return Optional.empty();
            }

            String xmlContent = Files.readString(path);
            Map<String, Object> data = xmlMapper.readValue(xmlContent, Map.class);
            
            String[] pathParts = elementPath.split("\\.");
            Object currentValue = data;
            
            for (String part : pathParts) {
                if (currentValue instanceof Map) {
                    Map<String, Object> currentMap = (Map<String, Object>) currentValue;
                    if (currentMap.containsKey(part)) {
                        currentValue = currentMap.get(part);
                    } else {
                        log.warn("Element '{}' not found in XML file: {}", elementPath, filePath);
                        return Optional.empty();
                    }
                } else {
                    log.warn("Cannot traverse element '{}' in XML file: {}", elementPath, filePath);
                    return Optional.empty();
                }
            }
            
            log.info("Successfully extracted element '{}' from XML file: {}", elementPath, filePath);
            return Optional.of(currentValue);
            
        } catch (IOException e) {
            log.error("Error reading XML file: {}", filePath, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error extracting element '{}' from XML file: {}", elementPath, filePath, e);
            return Optional.empty();
        }
    }

    /**
     * Extract raw content from JSON file as String
     * 
     * @param filePath Path to the JSON file
     * @return Optional containing the raw JSON content, empty if file not found
     */
    public Optional<String> extractRawJsonContent(String filePath) {
        try {
            log.info("Extracting raw content from JSON file: {}", filePath);
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("JSON file not found: {}", filePath);
                return Optional.empty();
            }

            String content = Files.readString(path);
            log.info("Successfully extracted raw content from JSON file: {} ({} characters)", filePath, content.length());
            return Optional.of(content);
            
        } catch (IOException e) {
            log.error("Error reading JSON file: {}", filePath, e);
            return Optional.empty();
        }
    }

    /**
     * Extract raw content from XML file as String
     * 
     * @param filePath Path to the XML file
     * @return Optional containing the raw XML content, empty if file not found
     */
    public Optional<String> extractRawXmlContent(String filePath) {
        try {
            log.info("Extracting raw content from XML file: {}", filePath);
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("XML file not found: {}", filePath);
                return Optional.empty();
            }

            String content = Files.readString(path);
            log.info("Successfully extracted raw content from XML file: {} ({} characters)", filePath, content.length());
            return Optional.of(content);
            
        } catch (IOException e) {
            log.error("Error reading XML file: {}", filePath, e);
            return Optional.empty();
        }
    }

    /**
     * Validate if file exists and is readable
     * 
     * @param filePath Path to the file
     * @return true if file exists and is readable, false otherwise
     */
    public boolean isFileReadable(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.exists(path) && Files.isReadable(path);
        } catch (Exception e) {
            log.error("Error checking file readability: {}", filePath, e);
            return false;
        }
    }

    /**
     * Get file size in bytes
     * 
     * @param filePath Path to the file
     * @return Optional containing file size in bytes, empty if file not found
     */
    public Optional<Long> getFileSize(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return Optional.empty();
            }
            return Optional.of(Files.size(path));
        } catch (IOException e) {
            log.error("Error getting file size: {}", filePath, e);
            return Optional.empty();
        }
    }

    /**
     * Convert JSON file to XML format
     * 
     * @param jsonFilePath Path to the JSON file
     * @return Optional containing XML content, empty if conversion fails
     */
    public Optional<String> convertJsonFileToXml(String jsonFilePath) {
        try {
            log.info("Converting JSON file to XML: {}", jsonFilePath);
            
            Optional<Map<String, Object>> jsonData = extractFromJsonFile(jsonFilePath);
            if (jsonData.isEmpty()) {
                return Optional.empty();
            }
            
            String xmlContent = xmlMapper.writeValueAsString(jsonData.get());
            log.info("Successfully converted JSON file to XML: {}", jsonFilePath);
            return Optional.of(xmlContent);
            
        } catch (Exception e) {
            log.error("Error converting JSON file to XML: {}", jsonFilePath, e);
            return Optional.empty();
        }
    }

    /**
     * Convert XML file to JSON format
     * 
     * @param xmlFilePath Path to the XML file
     * @return Optional containing JSON content, empty if conversion fails
     */
    public Optional<String> convertXmlFileToJson(String xmlFilePath) {
        try {
            log.info("Converting XML file to JSON: {}", xmlFilePath);
            
            Optional<Map<String, Object>> xmlData = extractFromXmlFile(xmlFilePath);
            if (xmlData.isEmpty()) {
                return Optional.empty();
            }
            
            String jsonContent = jsonMapper.writeValueAsString(xmlData.get());
            log.info("Successfully converted XML file to JSON: {}", xmlFilePath);
            return Optional.of(jsonContent);
            
        } catch (Exception e) {
            log.error("Error converting XML file to JSON: {}", xmlFilePath, e);
            return Optional.empty();
        }
    }
}
