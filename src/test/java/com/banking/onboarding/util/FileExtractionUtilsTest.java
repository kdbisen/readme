package com.banking.onboarding.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FileExtractionUtils
 */
@Slf4j
@SpringBootTest
public class FileExtractionUtilsTest {

    @Autowired
    private FileExtractionUtils fileExtractionUtils;

    @Test
    public void testExtractFromJsonFile() {
        // Create a test JSON file
        String testJsonContent = """
            {
                "customer": {
                    "personalInfo": {
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john.doe@example.com"
                    },
                    "address": {
                        "street": "123 Main St",
                        "city": "New York",
                        "zipCode": "10001"
                    }
                },
                "requestType": "ADD_KYC"
            }
            """;

        try {
            Path testFile = Paths.get("test-customer.json");
            Files.write(testFile, testJsonContent.getBytes());

            // Test extraction
            Optional<Map<String, Object>> result = fileExtractionUtils.extractFromJsonFile("test-customer.json");
            
            assertTrue(result.isPresent());
            Map<String, Object> data = result.get();
            assertTrue(data.containsKey("customer"));
            assertTrue(data.containsKey("requestType"));
            assertEquals("ADD_KYC", data.get("requestType"));

            // Clean up
            Files.deleteIfExists(testFile);
            
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception");
        }
    }

    @Test
    public void testExtractFieldFromJsonFile() {
        // Create a test JSON file
        String testJsonContent = """
            {
                "customer": {
                    "personalInfo": {
                        "firstName": "Jane",
                        "lastName": "Smith"
                    }
                }
            }
            """;

        try {
            Path testFile = Paths.get("test-customer-field.json");
            Files.write(testFile, testJsonContent.getBytes());

            // Test field extraction
            Optional<Object> firstName = fileExtractionUtils.extractFieldFromJsonFile("test-customer-field.json", "customer.personalInfo.firstName");
            Optional<Object> lastName = fileExtractionUtils.extractFieldFromJsonFile("test-customer-field.json", "customer.personalInfo.lastName");
            Optional<Object> nonExistent = fileExtractionUtils.extractFieldFromJsonFile("test-customer-field.json", "customer.nonExistent");

            assertTrue(firstName.isPresent());
            assertEquals("Jane", firstName.get());
            
            assertTrue(lastName.isPresent());
            assertEquals("Smith", lastName.get());
            
            assertFalse(nonExistent.isPresent());

            // Clean up
            Files.deleteIfExists(testFile);
            
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception");
        }
    }

    @Test
    public void testExtractFromXmlFile() {
        // Create a test XML file
        String testXmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <customer>
                <personalInfo>
                    <firstName>Bob</firstName>
                    <lastName>Johnson</lastName>
                    <email>bob.johnson@example.com</email>
                </personalInfo>
                <address>
                    <street>456 Oak Ave</street>
                    <city>Los Angeles</city>
                    <zipCode>90210</zipCode>
                </address>
                <requestType>UPDATE_KYC</requestType>
            </customer>
            """;

        try {
            Path testFile = Paths.get("test-customer.xml");
            Files.write(testFile, testXmlContent.getBytes());

            // Test extraction
            Optional<Map<String, Object>> result = fileExtractionUtils.extractFromXmlFile("test-customer.xml");
            
            assertTrue(result.isPresent());
            Map<String, Object> data = result.get();
            assertTrue(data.containsKey("personalInfo"));
            assertTrue(data.containsKey("requestType"));
            assertEquals("UPDATE_KYC", data.get("requestType"));

            // Clean up
            Files.deleteIfExists(testFile);
            
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception");
        }
    }

    @Test
    public void testExtractElementFromXmlFile() {
        // Create a test XML file
        String testXmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <customer>
                <personalInfo>
                    <firstName>Alice</firstName>
                    <lastName>Brown</lastName>
                </personalInfo>
            </customer>
            """;

        try {
            Path testFile = Paths.get("test-customer-element.xml");
            Files.write(testFile, testXmlContent.getBytes());

            // Test element extraction
            Optional<Object> firstName = fileExtractionUtils.extractElementFromXmlFile("test-customer-element.xml", "personalInfo.firstName");
            Optional<Object> lastName = fileExtractionUtils.extractElementFromXmlFile("test-customer-element.xml", "personalInfo.lastName");
            Optional<Object> nonExistent = fileExtractionUtils.extractElementFromXmlFile("test-customer-element.xml", "personalInfo.nonExistent");

            assertTrue(firstName.isPresent());
            assertEquals("Alice", firstName.get());
            
            assertTrue(lastName.isPresent());
            assertEquals("Brown", lastName.get());
            
            assertFalse(nonExistent.isPresent());

            // Clean up
            Files.deleteIfExists(testFile);
            
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception");
        }
    }

    @Test
    public void testFileConversion() {
        // Create a test JSON file
        String testJsonContent = """
            {
                "customer": {
                    "name": "Test User",
                    "id": "12345"
                }
            }
            """;

        try {
            Path testJsonFile = Paths.get("test-conversion.json");
            Files.write(testJsonFile, testJsonContent.getBytes());

            // Test JSON to XML conversion
            Optional<String> xmlContent = fileExtractionUtils.convertJsonFileToXml("test-conversion.json");
            assertTrue(xmlContent.isPresent());
            assertTrue(xmlContent.get().contains("<customer>"));

            // Test XML to JSON conversion
            Optional<String> jsonContent = fileExtractionUtils.convertXmlFileToJson("test-conversion.json");
            assertTrue(jsonContent.isPresent());
            assertTrue(jsonContent.get().contains("\"customer\""));

            // Clean up
            Files.deleteIfExists(testJsonFile);
            
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception");
        }
    }

    @Test
    public void testFileValidation() {
        // Test with non-existent file
        assertFalse(fileExtractionUtils.isFileReadable("non-existent-file.json"));
        assertFalse(fileExtractionUtils.getFileSize("non-existent-file.json").isPresent());

        // Create a test file
        try {
            Path testFile = Paths.get("test-validation.json");
            Files.write(testFile, "{\"test\": \"data\"}".getBytes());

            // Test with existing file
            assertTrue(fileExtractionUtils.isFileReadable("test-validation.json"));
            assertTrue(fileExtractionUtils.getFileSize("test-validation.json").isPresent());
            assertTrue(fileExtractionUtils.getFileSize("test-validation.json").get() > 0);

            // Clean up
            Files.deleteIfExists(testFile);
            
        } catch (Exception e) {
            log.error("Test failed", e);
            fail("Test should not throw exception");
        }
    }
}
