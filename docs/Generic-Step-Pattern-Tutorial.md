# Generic Step Pattern - Step Management Tutorial

## Overview

This tutorial explains how to add, modify, and remove steps in the Banking Onboarding Service's generic step pattern system. The system is designed to be flexible and configurable, allowing easy modification of the onboarding flow without code changes.

## Table of Contents

1. [Understanding the Step Pattern](#understanding-the-step-pattern)
2. [Current Step Configuration](#current-step-configuration)
3. [Adding New Steps](#adding-new-steps)
4. [Modifying Existing Steps](#modifying-existing-steps)
5. [Removing Steps](#removing-steps)
6. [Reordering Steps](#reordering-steps)
7. [Step Dependencies](#step-dependencies)
8. [Configuration Management](#configuration-management)
9. [Testing Steps](#testing-steps)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

## Understanding the Step Pattern

### Architecture Overview

The generic step pattern consists of:

```
GenericStepExecutor (Interface)
├── StepConfig (Configuration)
├── GenericStepContext (Data Sharing)
├── StepResult (Output)
└── GenericStepExecutionEngine (Orchestration)
```

### Key Components

1. **GenericStepExecutor**: Interface that all steps implement
2. **StepConfig**: Configuration for each step (retry, timeout, dependencies)
3. **GenericStepContext**: Shared data container between steps
4. **StepResult**: Standardized step output
5. **GenericStepExecutionEngine**: Executes steps in sequence

## Current Step Configuration

### Current Flow (4 Steps)

```
1. XML_TO_JSON_TRANSFORMATION (Apigee)
2. FENERGO_ENTITY_CREATION (Fenergo Entity API)
3. FENERGO_JOURNEY_SCHEMA_EVALUATION (Fenergo Logic Engine)
4. FENERGO_JOURNEY_LAUNCH (Fenergo Journey Command API)
```

### Configuration Location

**File**: `src/main/resources/application.properties`

```properties
# Step Configuration - Complete Fenergo Flow
onboarding.steps.definition=${ONBOARDING_STEPS_DEFINITION:XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH}
```

## Adding New Steps

### Step 1: Create Step Implementation

Create a new step class implementing `GenericStepExecutor`:

```java
package com.banking.onboarding.step.impl;

import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.GenericStepExecutor;
import com.banking.onboarding.step.StepConfig;
import com.banking.onboarding.step.StepResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Example: New Step Implementation
 */
@Slf4j
@Component("newStepName") // Explicit bean name
@RequiredArgsConstructor
public class NewStepImplementation implements GenericStepExecutor {

    // Inject required services
    // private final SomeService someService;

    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        log.info("[CORRELATION:{}] Executing new step", context.getCorrelationId());

        // Get input data from previous step or initial input
        Object inputData = getInputData(context);

        try {
            // Step implementation logic here
            // Process inputData
            // Call external services if needed
            // Transform data if required

            Object result = processData(inputData);

            // Store result in context for next steps
            context.addStepResult(getStepName(), result);

            return StepResult.success(result, getStepName(), context.getCorrelationId());

        } catch (Exception e) {
            log.error("[CORRELATION:{}] Step execution failed: {}", 
                    context.getCorrelationId(), e.getMessage());
            return StepResult.failure(e.getMessage(), getStepName(), context.getCorrelationId());
        }
    }

    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description("Description of what this step does")
                .retryEnabled(true)
                .maxRetries(3)
                .retryDelayMs(1000)
                .backoffMultiplier(2.0)
                .asyncEnabled(false)
                .timeoutMs(30000)
                .dependencies(new String[]{"PREVIOUS_STEP_NAME"}) // If depends on previous step
                .build();
    }

    @Override
    public String getStepName() {
        return "NEW_STEP_NAME";
    }

    @Override
    public boolean canExecute(GenericStepContext context) {
        // Check if step can be executed based on context
        return getInputData(context) != null;
    }

    private Object processData(Object inputData) {
        // Implementation logic here
        return inputData; // Return processed data
    }
}
```

### Step 2: Update Step Configuration Loader

Add the new step to `StepConfigurationLoader`:

```java
// In StepConfigurationLoader.java

/**
 * Get step description
 */
private String getStepDescription(String stepName) {
    return switch (stepName) {
        case "XML_TO_JSON_TRANSFORMATION" -> "Transform XML data to JSON format via internal Apigee service";
        case "FENERGO_ENTITY_CREATION" -> "Create entity in Fenergo system via Entity API";
        case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> "Evaluate journey schema via Fenergo Logic Engine";
        case "FENERGO_JOURNEY_LAUNCH" -> "Launch journey via Fenergo Journey Command API";
        case "NEW_STEP_NAME" -> "Description of the new step"; // Add this line
        default -> "Step: " + stepName;
    };
}

/**
 * Get step dependencies
 */
private String[] getStepDependencies(String stepName) {
    return switch (stepName) {
        case "FENERGO_ENTITY_CREATION" -> new String[]{"XML_TO_JSON_TRANSFORMATION"};
        case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> new String[]{"FENERGO_ENTITY_CREATION"};
        case "FENERGO_JOURNEY_LAUNCH" -> new String[]{"FENERGO_JOURNEY_SCHEMA_EVALUATION"};
        case "NEW_STEP_NAME" -> new String[]{"PREVIOUS_STEP_NAME"}; // Add this line
        default -> new String[0];
    };
}

/**
 * Get step priority
 */
private int getStepPriority(String stepName) {
    return switch (stepName) {
        case "XML_TO_JSON_TRANSFORMATION" -> 1;
        case "FENERGO_ENTITY_CREATION" -> 2;
        case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> 3;
        case "FENERGO_JOURNEY_LAUNCH" -> 4;
        case "NEW_STEP_NAME" -> 5; // Add this line
        default -> 99;
    };
}
```

### Step 3: Update Application Properties

Add the new step to the step definition:

```properties
# Step Configuration - Complete Fenergo Flow
onboarding.steps.definition=${ONBOARDING_STEPS_DEFINITION:XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,NEW_STEP_NAME,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH}
```

### Step 4: Update Dependencies

If the new step affects other steps, update their dependencies:

```java
// Update subsequent steps' dependencies
case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> new String[]{"NEW_STEP_NAME"}; // Changed from FENERGO_ENTITY_CREATION
```

## Modifying Existing Steps

### Modifying Step Logic

To modify an existing step's logic:

1. **Edit the step implementation class**
2. **Update the step description if needed**
3. **Modify configuration if required**

Example: Modifying XML_TO_JSON_TRANSFORMATION step

```java
// In XmlToJsonTransformationStep.java

@Override
public StepResult<Object> execute(GenericStepContext context) {
    log.info("[CORRELATION:{}] Executing Step 0: XML to JSON transformation", context.getCorrelationId());

    Object inputData = getInputData(context);
    String xmlData = convertToString(inputData);

    if (xmlData == null || xmlData.trim().isEmpty()) {
        return StepResult.failure("No valid XML data found for transformation", getStepName(), context.getCorrelationId());
    }

    // NEW: Add validation logic
    if (!isValidXmlFormat(xmlData)) {
        return StepResult.failure("Invalid XML format", getStepName(), context.getCorrelationId());
    }

    // NEW: Add preprocessing
    String preprocessedXml = preprocessXml(xmlData);

    // Execute the transformation service call
    ApiResponse response = transformationService.transformXmlToJson(preprocessedXml, context.getCorrelationId()).join();

    if (response.isSuccess()) {
        // NEW: Add postprocessing
        Object processedResult = postprocessJson(response.getBody());
        
        log.info("[CORRELATION:{}] Step 0: XML to JSON transformation successful", context.getCorrelationId());
        return StepResult.success(processedResult, getStepName(), context.getCorrelationId());
    } else {
        log.error("[CORRELATION:{}] Step 0: XML to JSON transformation failed: {}", context.getCorrelationId(), response.getErrorMessage());
        return StepResult.failure(response.getErrorMessage(), getStepName(), context.getCorrelationId());
    }
}

// NEW: Add helper methods
private boolean isValidXmlFormat(String xmlData) {
    // Validation logic
    return true;
}

private String preprocessXml(String xmlData) {
    // Preprocessing logic
    return xmlData;
}

private Object postprocessJson(Object jsonData) {
    // Postprocessing logic
    return jsonData;
}
```

### Modifying Step Configuration

To modify step configuration:

```java
@Override
public StepConfig getConfig() {
    return StepConfig.builder()
            .stepName(getStepName())
            .description("Updated description")
            .retryEnabled(true)
            .maxRetries(5) // Changed from 3
            .retryDelayMs(2000) // Changed from 1000
            .backoffMultiplier(1.5) // Changed from 2.0
            .asyncEnabled(false)
            .timeoutMs(60000) // Changed from 30000
            .dependencies(new String[]{"PREVIOUS_STEP"})
            .build();
}
```

## Removing Steps

### Step 1: Remove from Configuration

Remove the step from `application.properties`:

```properties
# Before
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,STEP_TO_REMOVE,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH

# After
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH
```

### Step 2: Update Dependencies

Update dependencies of steps that depended on the removed step:

```java
// Before: STEP_TO_REMOVE was between FENERGO_ENTITY_CREATION and FENERGO_JOURNEY_SCHEMA_EVALUATION
case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> new String[]{"STEP_TO_REMOVE"};

// After: FENERGO_JOURNEY_SCHEMA_EVALUATION now depends directly on FENERGO_ENTITY_CREATION
case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> new String[]{"FENERGO_ENTITY_CREATION"};
```

### Step 3: Remove from StepConfigurationLoader

Remove the step from all switch statements in `StepConfigurationLoader`:

```java
// Remove these lines:
case "STEP_TO_REMOVE" -> "Description of the step to remove";
case "STEP_TO_REMOVE" -> new String[]{"PREVIOUS_STEP"};
case "STEP_TO_REMOVE" -> 3;
```

### Step 4: Delete Implementation Class

Delete the step implementation file:

```bash
rm src/main/java/com/banking/onboarding/step/impl/StepToRemove.java
```

## Reordering Steps

### Method 1: Priority-Based Reordering (Recommended)

Change step priorities in `StepConfigurationLoader`:

```java
/**
 * Get step priority
 */
private int getStepPriority(String stepName) {
    return switch (stepName) {
        case "XML_TO_JSON_TRANSFORMATION" -> 1;
        case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> 2; // Moved up (was 3)
        case "FENERGO_ENTITY_CREATION" -> 3; // Moved down (was 2)
        case "FENERGO_JOURNEY_LAUNCH" -> 4;
        default -> 99;
    };
}
```

**Configuration**: Set execution order to priority-based:

```properties
# In application.properties
onboarding.steps.execution.order=PRIORITY
```

**Result**: Steps will execute in priority order (1, 2, 3, 4) regardless of their order in the definition.

### Method 2: Order-Based Reordering (Legacy)

Change the order in `application.properties`:

```properties
# Original order
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH

# New order (moved FENERGO_JOURNEY_SCHEMA_EVALUATION before FENERGO_ENTITY_CREATION)
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_LAUNCH
```

**Configuration**: Set execution order to order-based:

```properties
# In application.properties
onboarding.steps.execution.order=ORDER
```

### Method 3: Update Dependencies

Update dependencies to reflect the new order:

```java
// Update dependencies in StepConfigurationLoader
case "FENERGO_ENTITY_CREATION" -> new String[]{"FENERGO_JOURNEY_SCHEMA_EVALUATION"}; // Changed dependency
case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> new String[]{"XML_TO_JSON_TRANSFORMATION"}; // Changed dependency
```

### Execution Order Configuration

The system supports two execution order modes:

#### Priority-Based Execution (Default)
```properties
onboarding.steps.execution.order=PRIORITY
```

- Steps execute in priority order (1, 2, 3, 4...)
- Lower numbers = higher priority
- Independent of step definition order
- More flexible and maintainable

#### Order-Based Execution (Legacy)
```properties
onboarding.steps.execution.order=ORDER
```

- Steps execute in the order defined in `onboarding.steps.definition`
- Follows the exact sequence in the property
- Less flexible but simpler

### Priority vs Order Comparison

| Aspect | Priority-Based | Order-Based |
|--------|----------------|-------------|
| **Flexibility** | High - Change priorities only | Low - Must change definition order |
| **Maintainability** | High - Clear priority system | Medium - Order-dependent |
| **Dependencies** | Still respected | Still respected |
| **Configuration** | `onboarding.steps.execution.order=PRIORITY` | `onboarding.steps.execution.order=ORDER` |
| **Reordering** | Change priority numbers | Change definition order |

### Example: Reordering Steps

**Scenario**: Move `FENERGO_JOURNEY_SCHEMA_EVALUATION` before `FENERGO_ENTITY_CREATION`

#### Using Priority-Based (Recommended)

1. **Update priorities**:
```java
private int getStepPriority(String stepName) {
    return switch (stepName) {
        case "XML_TO_JSON_TRANSFORMATION" -> 1;
        case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> 2; // Moved up
        case "FENERGO_ENTITY_CREATION" -> 3; // Moved down
        case "FENERGO_JOURNEY_LAUNCH" -> 4;
        default -> 99;
    };
}
```

2. **Update dependencies**:
```java
case "FENERGO_ENTITY_CREATION" -> new String[]{"FENERGO_JOURNEY_SCHEMA_EVALUATION"}; // Now depends on schema evaluation
case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> new String[]{"XML_TO_JSON_TRANSFORMATION"}; // Still depends on transformation
```

3. **Result**: Execution order becomes:
   - XML_TO_JSON_TRANSFORMATION (priority 1)
   - FENERGO_JOURNEY_SCHEMA_EVALUATION (priority 2)
   - FENERGO_ENTITY_CREATION (priority 3)
   - FENERGO_JOURNEY_LAUNCH (priority 4)

#### Using Order-Based (Legacy)

1. **Update definition order**:
```properties
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_LAUNCH
```

2. **Update dependencies** (same as above)

3. **Result**: Same execution order as priority-based

## Step Dependencies

### Understanding Dependencies

Dependencies define the order of step execution. A step can only execute if all its dependencies have completed successfully.

### Types of Dependencies

1. **Sequential Dependencies**: Step B depends on Step A
2. **Multiple Dependencies**: Step C depends on both Step A and Step B
3. **No Dependencies**: Step can execute immediately

### Example: Adding Multiple Dependencies

```java
@Override
public StepConfig getConfig() {
    return StepConfig.builder()
            .stepName(getStepName())
            .description("Step that depends on multiple previous steps")
            .retryEnabled(true)
            .maxRetries(3)
            .retryDelayMs(1000)
            .backoffMultiplier(2.0)
            .asyncEnabled(false)
            .timeoutMs(30000)
            .dependencies(new String[]{"STEP_A", "STEP_B"}) // Multiple dependencies
            .build();
}
```

### Dependency Validation

The system automatically validates dependencies:

```java
@Override
public boolean canExecute(GenericStepContext context) {
    // Check if all dependencies have completed successfully
    for (String dependency : getConfig().getDependencies()) {
        if (!context.hasStepResult(dependency)) {
            return false;
        }
    }
    return true;
}
```

## Configuration Management

### Environment-Specific Configuration

Use environment variables to override step configurations:

```bash
# Development environment
export ONBOARDING_STEPS_DEFINITION="XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION"

# Production environment  
export ONBOARDING_STEPS_DEFINITION="XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH"
```

### Dynamic Configuration

Steps can be configured dynamically at runtime:

```java
// In application.properties
onboarding.steps.retry.enabled=${ONBOARDING_STEPS_RETRY_ENABLED:true}
onboarding.steps.retry.max-attempts=${ONBOARDING_STEPS_RETRY_MAX_ATTEMPTS:3}
onboarding.steps.retry.delay-ms=${ONBOARDING_STEPS_RETRY_DELAY_MS:1000}
onboarding.steps.timeout-ms=${ONBOARDING_STEPS_TIMEOUT_MS:30000}
```

### Configuration Validation

The system validates step configurations at startup:

```java
@PostConstruct
public void validateStepConfigurations() {
    List<StepConfig> configs = loadStepConfigurations();
    
    for (StepConfig config : configs) {
        // Validate step name
        if (config.getStepName() == null || config.getStepName().trim().isEmpty()) {
            throw new IllegalStateException("Step name cannot be null or empty");
        }
        
        // Validate dependencies
        for (String dependency : config.getDependencies()) {
            boolean dependencyExists = configs.stream()
                    .anyMatch(c -> c.getStepName().equals(dependency));
            if (!dependencyExists) {
                throw new IllegalStateException("Step " + config.getStepName() + 
                        " depends on non-existent step: " + dependency);
            }
        }
    }
}
```

## Testing Steps

### Unit Testing Individual Steps

```java
@Test
class NewStepImplementationTest {
    
    @Mock
    private SomeService someService;
    
    @InjectMocks
    private NewStepImplementation step;
    
    @Test
    void shouldExecuteStepSuccessfully() {
        // Given
        GenericStepContext context = GenericStepContext.create(
                "CORR-123", "PROC-123", "test-input");
        
        when(someService.process(any())).thenReturn("processed-result");
        
        // When
        StepResult<Object> result = step.execute(context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("processed-result");
        assertThat(result.getStepName()).isEqualTo("NEW_STEP_NAME");
    }
    
    @Test
    void shouldHandleStepFailure() {
        // Given
        GenericStepContext context = GenericStepContext.create(
                "CORR-123", "PROC-123", "test-input");
        
        when(someService.process(any())).thenThrow(new RuntimeException("Service error"));
        
        // When
        StepResult<Object> result = step.execute(context);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Service error");
    }
}
```

### Integration Testing Step Flow

```java
@Test
class StepFlowIntegrationTest {
    
    @Autowired
    private GenericStepExecutionEngine stepExecutionEngine;
    
    @Test
    void shouldExecuteCompleteStepFlow() {
        // Given
        List<String> stepNames = List.of(
                "XML_TO_JSON_TRANSFORMATION",
                "FENERGO_ENTITY_CREATION",
                "FENERGO_JOURNEY_SCHEMA_EVALUATION",
                "FENERGO_JOURNEY_LAUNCH"
        );
        
        GenericStepContext context = GenericStepContext.create(
                "CORR-123", "PROC-123", "<xml>test</xml>");
        
        // When
        StepResult<Map<String, Object>> result = stepExecutionEngine.executeSteps(stepNames, context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).containsKeys(
                "XML_TO_JSON_TRANSFORMATION",
                "FENERGO_ENTITY_CREATION",
                "FENERGO_JOURNEY_SCHEMA_EVALUATION",
                "FENERGO_JOURNEY_LAUNCH"
        );
    }
}
```

### Testing Step Dependencies

```java
@Test
void shouldRespectStepDependencies() {
    // Given
    GenericStepContext context = GenericStepContext.create(
            "CORR-123", "PROC-123", "test-input");
    
    // When - Try to execute step without its dependency
    StepResult<Object> result = stepExecutionEngine.executeStep("FENERGO_ENTITY_CREATION", context);
    
    // Then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrorMessage()).contains("dependency");
}
```

## Best Practices

### 1. Step Design

- **Single Responsibility**: Each step should have one clear purpose
- **Idempotent**: Steps should be safe to retry
- **Stateless**: Steps should not maintain state between executions
- **Fail Fast**: Validate inputs early and fail quickly

### 2. Error Handling

- **Specific Exceptions**: Use specific exception types
- **Detailed Logging**: Log with correlation ID and context
- **Graceful Degradation**: Handle failures gracefully
- **Retry Logic**: Implement appropriate retry mechanisms

### 3. Data Sharing

- **Type Safety**: Use strongly typed data where possible
- **Minimal Data**: Only share necessary data between steps
- **Clear Contracts**: Define clear input/output contracts
- **Validation**: Validate data at step boundaries

### 4. Configuration

- **Environment Variables**: Use environment variables for configuration
- **Validation**: Validate configurations at startup
- **Documentation**: Document all configuration options
- **Defaults**: Provide sensible defaults

### 5. Testing

- **Unit Tests**: Test each step in isolation
- **Integration Tests**: Test step interactions
- **Error Scenarios**: Test failure cases
- **Performance Tests**: Test step performance

## Troubleshooting

### Common Issues

#### 1. Step Not Found Error

**Error**: `No step executor found for: STEP_NAME`

**Solution**: 
- Check if step is registered as Spring component
- Verify step name matches exactly
- Ensure step is in the step definition

#### 2. Dependency Not Found Error

**Error**: `Step STEP_A depends on non-existent step: STEP_B`

**Solution**:
- Check dependency name spelling
- Ensure dependent step is defined
- Verify step order in configuration

#### 3. Circular Dependency Error

**Error**: `Circular dependency detected`

**Solution**:
- Review step dependencies
- Remove circular references
- Redesign step flow if necessary

#### 4. Step Execution Timeout

**Error**: `Step execution timeout`

**Solution**:
- Increase timeout configuration
- Optimize step implementation
- Check external service performance

### Debugging Steps

#### 1. Enable Debug Logging

```properties
# In application.properties
logging.level.com.banking.onboarding.step=DEBUG
logging.level.com.banking.onboarding.service.GenericOnboardingFlowService=DEBUG
```

#### 2. Use Step Testing Endpoint

```bash
# Test individual step
curl -X POST http://localhost:8080/api/v1/onboarding/test/step/STEP_NAME \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: CORR-123" \
  -d '{"inputData": "test-input"}'
```

#### 3. Check Step Registry

```java
// In GenericStepExecutionEngine
public String[] getRegisteredSteps() {
    return stepExecutors.keySet().toArray(new String[0]);
}
```

### Performance Optimization

#### 1. Step Optimization

- **Parallel Execution**: Execute independent steps in parallel
- **Caching**: Cache expensive operations
- **Connection Pooling**: Use connection pools for external calls
- **Batch Processing**: Process multiple items together

#### 2. Configuration Optimization

- **Timeout Tuning**: Set appropriate timeouts
- **Retry Strategy**: Optimize retry parameters
- **Resource Limits**: Set appropriate resource limits

## Advanced Topics

### Custom Step Types

Create specialized step types for different use cases:

```java
// Async Step
@Component
public class AsyncStepImplementation implements GenericStepExecutor {
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .asyncEnabled(true) // Enable async execution
                .timeoutMs(60000) // Longer timeout for async
                .build();
    }
}

// Conditional Step
@Component
public class ConditionalStepImplementation implements GenericStepExecutor {
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        // Custom condition logic
        return context.getInputData() != null && 
               context.getInputData().toString().contains("condition");
    }
}
```

### Dynamic Step Loading

Load steps dynamically based on configuration:

```java
@Component
public class DynamicStepLoader {
    
    public List<GenericStepExecutor> loadStepsFromConfiguration() {
        // Load steps based on external configuration
        return stepConfigurations.stream()
                .map(this::createStepInstance)
                .collect(Collectors.toList());
    }
}
```

### Step Monitoring

Monitor step execution metrics:

```java
@Component
public class StepMonitoringService {
    
    public void recordStepExecution(String stepName, long duration, boolean success) {
        // Record metrics for monitoring
        meterRegistry.counter("step.execution", 
                "step", stepName, 
                "success", String.valueOf(success))
                .increment();
        
        meterRegistry.timer("step.duration", "step", stepName)
                .record(duration, TimeUnit.MILLISECONDS);
    }
}
```

---

## Summary

This tutorial provides comprehensive guidance for managing steps in the generic step pattern system. The system is designed to be:

- **Flexible**: Easy to add, modify, and remove steps
- **Configurable**: Environment-specific configurations
- **Testable**: Comprehensive testing strategies
- **Maintainable**: Clear patterns and best practices
- **Scalable**: Support for complex workflows

By following this tutorial, you can effectively manage the onboarding flow and adapt it to changing business requirements without major code changes.

---

*For additional support or questions, refer to the main documentation or contact the development team.*
