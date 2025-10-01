# Step Management Quick Reference

## Current Steps (4)

| Step | Description | Dependencies | Priority | Execution Order |
|------|-------------|--------------|----------|-----------------|
| `XML_TO_JSON_TRANSFORMATION` | Transform XML to JSON via Apigee | None | 1 | 1st |
| `FENERGO_ENTITY_CREATION` | Create entity in Fenergo | XML_TO_JSON_TRANSFORMATION | 2 | 2nd |
| `FENERGO_JOURNEY_SCHEMA_EVALUATION` | Evaluate journey schema | FENERGO_ENTITY_CREATION | 3 | 3rd |
| `FENERGO_JOURNEY_LAUNCH` | Launch Fenergo journey | FENERGO_JOURNEY_SCHEMA_EVALUATION | 4 | 4th |

## Execution Order Modes

### Priority-Based (Default) ✅
```properties
onboarding.steps.execution.order=PRIORITY
```
- Steps execute by priority number (1, 2, 3, 4...)
- Lower number = higher priority
- **Recommended**: More flexible and maintainable

### Order-Based (Legacy)
```properties
onboarding.steps.execution.order=ORDER
```
- Steps execute in definition order
- Follows exact sequence in `onboarding.steps.definition`
- **Legacy**: Less flexible but simpler

## Quick Actions

### Add New Step
1. Create implementation class in `step.impl` package
2. Add to `StepConfigurationLoader` switch statements
3. Update `application.properties` step definition
4. Update dependencies

### Remove Step
1. Remove from `application.properties`
2. Update dependencies in `StepConfigurationLoader`
3. Delete implementation class

### Reorder Steps (Priority-Based) ⭐
1. Change priority numbers in `StepConfigurationLoader`
2. Update dependencies accordingly
3. Set `onboarding.steps.execution.order=PRIORITY`

### Reorder Steps (Order-Based)
1. Change order in `application.properties`
2. Update dependencies accordingly
3. Set `onboarding.steps.execution.order=ORDER`

## Configuration Files

- **Step Definition**: `application.properties` → `onboarding.steps.definition`
- **Step Config**: `StepConfigurationLoader.java`
- **Step Implementation**: `step.impl` package

## Testing

```bash
# Test individual step
curl -X POST http://localhost:8080/api/v1/onboarding/test/step/STEP_NAME \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: CORR-123" \
  -d '{"inputData": "test-input"}'

# Test complete flow
curl -X POST http://localhost:8080/api/v1/onboarding/process-entity \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: CORR-123" \
  -d '{"xmlData": "<xml>test</xml>", "requestType": "ADD_KYC"}'
```

## Environment Variables

```bash
# Override step definition
export ONBOARDING_STEPS_DEFINITION="XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION"

# Override retry settings
export ONBOARDING_STEPS_RETRY_ENABLED=true
export ONBOARDING_STEPS_RETRY_MAX_ATTEMPTS=3
export ONBOARDING_STEPS_TIMEOUT_MS=30000
```

## Common Patterns

### Step Implementation Template
```java
@Component("stepName")
@RequiredArgsConstructor
public class StepImplementation implements GenericStepExecutor {
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Implementation logic
        return StepResult.success(result, getStepName(), context.getCorrelationId());
    }
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description("Step description")
                .dependencies(new String[]{"PREVIOUS_STEP"})
                .build();
    }
    
    @Override
    public String getStepName() {
        return "STEP_NAME";
    }
}
```

### Dependency Update Pattern
```java
// In StepConfigurationLoader
case "NEW_STEP" -> new String[]{"PREVIOUS_STEP"};
case "NEXT_STEP" -> new String[]{"NEW_STEP"}; // Updated dependency
```

---

*For detailed instructions, see `Generic-Step-Pattern-Tutorial.md`*
