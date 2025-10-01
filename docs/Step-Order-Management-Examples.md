# Step Order Management - Priority vs Order Examples

## Overview

The Banking Onboarding Service now supports **two methods** for controlling step execution order:

1. **Priority-Based Execution** (Default & Recommended)
2. **Order-Based Execution** (Legacy)

## Current Configuration

### Default Setup (Priority-Based)
```properties
# application.properties
onboarding.steps.execution.order=PRIORITY
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH
```

### Current Priorities
```java
// StepConfigurationLoader.java
private int getStepPriority(String stepName) {
    return switch (stepName) {
        case "XML_TO_JSON_TRANSFORMATION" -> 1;
        case "FENERGO_ENTITY_CREATION" -> 2;
        case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> 3;
        case "FENERGO_JOURNEY_LAUNCH" -> 4;
        default -> 99;
    };
}
```

**Current Execution Order**: 1 → 2 → 3 → 4

## Example 1: Reorder Using Priorities

### Scenario
Move `FENERGO_JOURNEY_SCHEMA_EVALUATION` before `FENERGO_ENTITY_CREATION`

### Step 1: Update Priorities
```java
// StepConfigurationLoader.java
private int getStepPriority(String stepName) {
    return switch (stepName) {
        case "XML_TO_JSON_TRANSFORMATION" -> 1;        // Stays first
        case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> 2;  // Moved up (was 3)
        case "FENERGO_ENTITY_CREATION" -> 3;           // Moved down (was 2)
        case "FENERGO_JOURNEY_LAUNCH" -> 4;            // Stays last
        default -> 99;
    };
}
```

### Step 2: Update Dependencies
```java
// StepConfigurationLoader.java
private String[] getStepDependencies(String stepName) {
    return switch (stepName) {
        case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> new String[]{"XML_TO_JSON_TRANSFORMATION"};
        case "FENERGO_ENTITY_CREATION" -> new String[]{"FENERGO_JOURNEY_SCHEMA_EVALUATION"}; // Now depends on schema evaluation
        case "FENERGO_JOURNEY_LAUNCH" -> new String[]{"FENERGO_ENTITY_CREATION"};
        default -> new String[0];
    };
}
```

### Step 3: Ensure Priority-Based Execution
```properties
# application.properties
onboarding.steps.execution.order=PRIORITY
```

### Result
**New Execution Order**: 
1. XML_TO_JSON_TRANSFORMATION (priority 1)
2. FENERGO_JOURNEY_SCHEMA_EVALUATION (priority 2)
3. FENERGO_ENTITY_CREATION (priority 3)
4. FENERGO_JOURNEY_LAUNCH (priority 4)

## Example 2: Reorder Using Order-Based (Legacy)

### Scenario
Same reordering using the legacy method

### Step 1: Update Definition Order
```properties
# application.properties
onboarding.steps.execution.order=ORDER
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_LAUNCH
```

### Step 2: Update Dependencies (Same as above)
```java
case "FENERGO_ENTITY_CREATION" -> new String[]{"FENERGO_JOURNEY_SCHEMA_EVALUATION"};
case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> new String[]{"XML_TO_JSON_TRANSFORMATION"};
```

### Result
**Same Execution Order** as priority-based method

## Example 3: Add New Step with Priority

### Scenario
Add a data validation step between XML transformation and entity creation

### Step 1: Create Step Implementation
```java
@Component("dataValidationStep")
@RequiredArgsConstructor
public class DataValidationStep implements GenericStepExecutor {
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Validation logic here
        Object inputData = getInputData(context);
        // Validate the JSON data
        Object validatedData = validateData(inputData);
        return StepResult.success(validatedData, getStepName(), context.getCorrelationId());
    }
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName("DATA_VALIDATION")
                .description("Validate transformed JSON data")
                .dependencies(new String[]{"XML_TO_JSON_TRANSFORMATION"})
                .build();
    }
    
    @Override
    public String getStepName() {
        return "DATA_VALIDATION";
    }
}
```

### Step 2: Update StepConfigurationLoader
```java
// Add to getStepDescription
case "DATA_VALIDATION" -> "Validate transformed JSON data";

// Add to getStepDependencies
case "DATA_VALIDATION" -> new String[]{"XML_TO_JSON_TRANSFORMATION"};
case "FENERGO_ENTITY_CREATION" -> new String[]{"DATA_VALIDATION"}; // Updated dependency

// Add to getStepPriority
case "DATA_VALIDATION" -> 2;
case "FENERGO_ENTITY_CREATION" -> 3; // Moved down
case "FENERGO_JOURNEY_SCHEMA_EVALUATION" -> 4; // Moved down
case "FENERGO_JOURNEY_LAUNCH" -> 5; // Moved down
```

### Step 3: Update Application Properties
```properties
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,DATA_VALIDATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH
```

### Result
**New Execution Order**:
1. XML_TO_JSON_TRANSFORMATION (priority 1)
2. DATA_VALIDATION (priority 2) ← **New step**
3. FENERGO_ENTITY_CREATION (priority 3)
4. FENERGO_JOURNEY_SCHEMA_EVALUATION (priority 4)
5. FENERGO_JOURNEY_LAUNCH (priority 5)

## Environment-Specific Ordering

### Development Environment
```bash
# Skip some steps for faster development
export ONBOARDING_STEPS_DEFINITION="XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION"
export ONBOARDING_STEPS_EXECUTION_ORDER="PRIORITY"
```

### Production Environment
```bash
# Full flow with all steps
export ONBOARDING_STEPS_DEFINITION="XML_TO_JSON_TRANSFORMATION,DATA_VALIDATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH"
export ONBOARDING_STEPS_EXECUTION_ORDER="PRIORITY"
```

## Testing Step Order

### Test Individual Step
```bash
curl -X POST http://localhost:8080/api/v1/onboarding/test/step/DATA_VALIDATION \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: CORR-123" \
  -d '{"inputData": "{\"test\": \"data\"}"}'
```

### Test Complete Flow
```bash
curl -X POST http://localhost:8080/api/v1/onboarding/process-entity \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: CORR-123" \
  -d '{"xmlData": "<xml>test</xml>", "requestType": "ADD_KYC"}'
```

### Check Execution Order in Logs
```bash
# Look for these log messages:
# [CORRELATION:CORR-123] Using priority-based execution order: [XML_TO_JSON_TRANSFORMATION, DATA_VALIDATION, FENERGO_ENTITY_CREATION, ...]
# [CORRELATION:CORR-123] Executing step: XML_TO_JSON_TRANSFORMATION
# [CORRELATION:CORR-123] Executing step: DATA_VALIDATION
# [CORRELATION:CORR-123] Executing step: FENERGO_ENTITY_CREATION
```

## Best Practices

### 1. Use Priority-Based Execution
- **Recommended**: More flexible and maintainable
- **Configuration**: `onboarding.steps.execution.order=PRIORITY`
- **Benefits**: Easy reordering, clear priority system

### 2. Consistent Priority Numbers
- Use sequential numbers (1, 2, 3, 4...)
- Leave gaps for future steps (1, 2, 4, 5...)
- Use 99 for non-critical steps

### 3. Update Dependencies
- Always update dependencies when reordering
- Ensure dependency chain is valid
- Test dependency validation

### 4. Environment-Specific Configuration
- Use environment variables for different environments
- Keep production configuration in properties files
- Document environment-specific settings

## Troubleshooting

### Issue: Steps Not Executing in Expected Order
**Cause**: Wrong execution order configuration
**Solution**: Check `onboarding.steps.execution.order` setting

### Issue: Dependency Errors
**Cause**: Dependencies not updated after reordering
**Solution**: Update `getStepDependencies()` method

### Issue: Step Not Found
**Cause**: Step not added to step definition
**Solution**: Add step to `onboarding.steps.definition`

### Issue: Priority Conflicts
**Cause**: Multiple steps with same priority
**Solution**: Ensure unique priority numbers

---

## Summary

**Yes, you can change step order by changing priorities!** 

The system now supports:
- ✅ **Priority-based execution** (recommended)
- ✅ **Order-based execution** (legacy)
- ✅ **Environment-specific configuration**
- ✅ **Flexible reordering**
- ✅ **Dependency management**

**To reorder steps using priorities:**
1. Change priority numbers in `StepConfigurationLoader`
2. Update dependencies accordingly
3. Set `onboarding.steps.execution.order=PRIORITY`
4. Test the new order

This makes the system highly flexible and maintainable! 🚀
