package com.banking.onboarding.step;

import com.banking.onboarding.step.impl.XmlToJsonTransformationStep;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Generic Step Pattern
 */
@SpringBootTest
@TestPropertySource(properties = {
    "onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION",
    "onboarding.steps.retry.enabled=false"
})
public class GenericStepPatternTest {
    
    @Test
    public void testGenericStepContext() {
        // Test generic context creation
        GenericStepContext context = GenericStepContext.create("test-correlation", "test-process", "test-data");
        
        assertNotNull(context);
        assertEquals("test-correlation", context.getCorrelationId());
        assertEquals("test-process", context.getProcessId());
        assertEquals("test-data", context.getInputData());
        assertNotNull(context.getStepResults());
        assertNotNull(context.getMetadata());
    }
    
    @Test
    public void testGenericStepContextDataSharing() {
        GenericStepContext context = GenericStepContext.create("test-correlation", "test-process", "initial-data");
        
        // Add step result
        context.addStepResult("STEP_1", "step1-result");
        context.addStepResult("STEP_2", "step2-result");
        
        // Verify data sharing
        assertEquals("step1-result", context.getStepResult("STEP_1"));
        assertEquals("step2-result", context.getStepResult("STEP_2"));
        assertTrue(context.hasStepResult("STEP_1"));
        assertTrue(context.hasStepResult("STEP_2"));
        assertFalse(context.hasStepResult("STEP_3"));
        
        // Test type casting
        String result1 = context.getStepResult("STEP_1", String.class);
        assertEquals("step1-result", result1);
    }
    
    @Test
    public void testStepConfig() {
        StepConfig config = StepConfig.builder()
                .stepName("TEST_STEP")
                .description("Test step")
                .retryEnabled(true)
                .maxRetries(3)
                .retryDelayMs(1000)
                .backoffMultiplier(2.0)
                .asyncEnabled(true)
                .timeoutMs(30000)
                .dependencies(new String[]{"PREVIOUS_STEP"})
                .build();
        
        assertNotNull(config);
        assertEquals("TEST_STEP", config.getStepName());
        assertEquals("Test step", config.getDescription());
        assertTrue(config.isRetryEnabled());
        assertEquals(3, config.getMaxRetries());
        assertEquals(1000, config.getRetryDelayMs());
        assertEquals(2.0, config.getBackoffMultiplier());
        assertTrue(config.isAsyncEnabled());
        assertEquals(30000, config.getTimeoutMs());
        assertArrayEquals(new String[]{"PREVIOUS_STEP"}, config.getDependencies());
    }
    
    @Test
    public void testStepResult() {
        StepResult<String> successResult = StepResult.success("test-data", "TEST_STEP", "test-correlation");
        
        assertNotNull(successResult);
        assertTrue(successResult.isSuccess());
        assertEquals("test-data", successResult.getData());
        assertEquals("TEST_STEP", successResult.getStepName());
        assertEquals("test-correlation", successResult.getCorrelationId());
        assertNull(successResult.getErrorMessage());
        
        StepResult<String> failureResult = StepResult.failure("test-error", "TEST_STEP", "test-correlation");
        
        assertNotNull(failureResult);
        assertFalse(failureResult.isSuccess());
        assertEquals("test-error", failureResult.getErrorMessage());
        assertEquals("TEST_STEP", failureResult.getStepName());
        assertEquals("test-correlation", failureResult.getCorrelationId());
        assertNull(failureResult.getData());
    }
}




