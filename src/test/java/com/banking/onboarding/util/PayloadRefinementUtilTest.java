package com.banking.onboarding.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for PayloadRefinementUtil
 */
@ExtendWith(MockitoExtension.class)
class PayloadRefinementUtilTest {

    private PayloadRefinementUtil payloadRefinementUtil;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        payloadRefinementUtil = new PayloadRefinementUtil(objectMapper);
    }

    @Test
    void testRefinePayloadWithMap() {
        // Given
        Map<String, Object> inputData = Map.of(
            "name", "John Doe",
            "email", "john@example.com",
            "age", 30
        );

        // When
        PayloadRefinementUtil.RefinedPayload result = payloadRefinementUtil.refinePayload(
            inputData, "test-correlation-id", "TEST_SOURCE"
        );

        // Then
        assertNotNull(result);
        assertNotNull(result.getRefinedData());
        assertNotNull(result.getMetadata());
        
        assertEquals("TEST_SOURCE", result.getMetadata().getSource());
        assertEquals("test-correlation-id", result.getMetadata().getCorrelationId());
        assertEquals("HashMap", result.getMetadata().getOriginalType());
        
        // Check that correlation ID was added
        assertTrue(result.getRefinedData().containsKey("correlationId"));
        assertEquals("test-correlation-id", result.getRefinedData().get("correlationId"));
        
        // Check that timestamp was added
        assertTrue(result.getRefinedData().containsKey("timestamp"));
        
        // Check that original data is preserved
        assertEquals("John Doe", result.getRefinedData().get("name"));
        assertEquals("john@example.com", result.getRefinedData().get("email"));
        assertEquals(30, result.getRefinedData().get("age"));
    }

    @Test
    void testRefinePayloadWithString() {
        // Given
        String inputData = "{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}";

        // When
        PayloadRefinementUtil.RefinedPayload result = payloadRefinementUtil.refinePayload(
            inputData, "test-correlation-id", "TEST_SOURCE"
        );

        // Then
        assertNotNull(result);
        assertNotNull(result.getRefinedData());
        
        // Check that JSON was parsed correctly
        assertEquals("Jane Doe", result.getRefinedData().get("name"));
        assertEquals("jane@example.com", result.getRefinedData().get("email"));
        
        // Check that correlation ID was added
        assertEquals("test-correlation-id", result.getRefinedData().get("correlationId"));
    }

    @Test
    void testRefinePayloadWithCustomTransformation() {
        // Given
        Map<String, Object> inputData = Map.of(
            "name", "John Doe",
            "email", "john@example.com"
        );

        // When
        PayloadRefinementUtil.RefinedPayload result = payloadRefinementUtil.getRefinedPayload(
            null, // context would be mocked in real test
            "test-step",
            CommonPayloadTransformers.dataValidationTransformer()
        );

        // Then
        assertNotNull(result);
        assertNotNull(result.getRefinedData());
        
        // Check that validation markers were added
        assertTrue(result.getRefinedData().containsKey("validationStatus"));
        assertEquals("VALIDATED", result.getRefinedData().get("validationStatus"));
    }

    @Test
    void testChainTransformers() {
        // Given
        Map<String, Object> inputData = Map.of(
            "name", "John Doe",
            "email", "john@example.com"
        );

        // When
        PayloadRefinementUtil.PayloadTransformer chainedTransformer = 
            CommonPayloadTransformers.chainTransformers(
                CommonPayloadTransformers.dataValidationTransformer(),
                CommonPayloadTransformers.dataEnrichmentTransformer(Map.of("source", "test"))
            );

        Map<String, Object> result = chainedTransformer.transform(inputData);

        // Then
        assertNotNull(result);
        
        // Check that both transformations were applied
        assertTrue(result.containsKey("validationStatus"));
        assertTrue(result.containsKey("source"));
        assertTrue(result.containsKey("enrichmentTimestamp"));
        
        assertEquals("VALIDATED", result.get("validationStatus"));
        assertEquals("test", result.get("source"));
    }

    @Test
    void testFieldMappingTransformer() {
        // Given
        Map<String, Object> inputData = Map.of(
            "firstName", "John",
            "lastName", "Doe",
            "emailAddress", "john@example.com"
        );

        Map<String, String> fieldMappings = Map.of(
            "firstName", "first_name",
            "lastName", "last_name",
            "emailAddress", "email"
        );

        // When
        PayloadRefinementUtil.PayloadTransformer transformer = 
            CommonPayloadTransformers.fieldMappingTransformer(fieldMappings);
        
        Map<String, Object> result = transformer.transform(inputData);

        // Then
        assertNotNull(result);
        
        // Check that fields were mapped correctly
        assertTrue(result.containsKey("first_name"));
        assertTrue(result.containsKey("last_name"));
        assertTrue(result.containsKey("email"));
        
        assertFalse(result.containsKey("firstName"));
        assertFalse(result.containsKey("lastName"));
        assertFalse(result.containsKey("emailAddress"));
        
        assertEquals("John", result.get("first_name"));
        assertEquals("Doe", result.get("last_name"));
        assertEquals("john@example.com", result.get("email"));
    }

    @Test
    void testDataFilteringTransformer() {
        // Given
        Map<String, Object> inputData = Map.of(
            "name", "John Doe",
            "email", "john@example.com",
            "password", "secret123",
            "age", 30
        );

        java.util.Set<String> allowedFields = java.util.Set.of("name", "email", "age");

        // When
        PayloadRefinementUtil.PayloadTransformer transformer = 
            CommonPayloadTransformers.dataFilteringTransformer(allowedFields);
        
        Map<String, Object> result = transformer.transform(inputData);

        // Then
        assertNotNull(result);
        
        // Check that only allowed fields are present
        assertTrue(result.containsKey("name"));
        assertTrue(result.containsKey("email"));
        assertTrue(result.containsKey("age"));
        
        assertFalse(result.containsKey("password"));
        
        assertEquals("John Doe", result.get("name"));
        assertEquals("john@example.com", result.get("email"));
        assertEquals(30, result.get("age"));
    }
}




