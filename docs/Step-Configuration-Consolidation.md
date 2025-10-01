# Step Configuration - External Configuration Support

## Overview

The Banking Onboarding Service now supports **external configuration files** for step definitions, making it even more readable and maintainable.

## Configuration Options

### Option 1: Inline Configuration (Current)
All step definitions in `StepConfigurationLoader.java`:

```java
private static final Map<String, StepDefinition> STEP_DEFINITIONS = Map.of(
    "XML_TO_JSON_TRANSFORMATION", 
    new StepDefinition("XML_TO_JSON_TRANSFORMATION", 1, "Transform XML...", new String[]{}, "TRANSFORMATION", true),
    // ... more steps
);
```

### Option 2: Properties File Configuration
Create `step-definitions.properties`:

```properties
# Step Definitions - All step info in one place
# Format: step.name.priority=value
# Format: step.name.description=value
# Format: step.name.dependencies=value
# Format: step.name.category=value
# Format: step.name.critical=value

# XML_TO_JSON_TRANSFORMATION
step.xml_to_json_transformation.priority=1
step.xml_to_json_transformation.description=Transform XML data to JSON format via internal Apigee service
step.xml_to_json_transformation.dependencies=
step.xml_to_json_transformation.category=TRANSFORMATION
step.xml_to_json_transformation.critical=true

# FENERGO_ENTITY_CREATION
step.fenergo_entity_creation.priority=2
step.fenergo_entity_creation.description=Create entity in Fenergo system via Entity API
step.fenergo_entity_creation.dependencies=XML_TO_JSON_TRANSFORMATION
step.fenergo_entity_creation.category=ENTITY_MANAGEMENT
step.fenergo_entity_creation.critical=true

# FENERGO_JOURNEY_SCHEMA_EVALUATION
step.fenergo_journey_schema_evaluation.priority=3
step.fenergo_journey_schema_evaluation.description=Evaluate journey schema via Fenergo Logic Engine
step.fenergo_journey_schema_evaluation.dependencies=FENERGO_ENTITY_CREATION
step.fenergo_journey_schema_evaluation.category=JOURNEY_MANAGEMENT
step.fenergo_journey_schema_evaluation.critical=true

# FENERGO_JOURNEY_LAUNCH
step.fenergo_journey_launch.priority=4
step.fenergo_journey_launch.description=Launch journey via Fenergo Journey Command API
step.fenergo_journey_launch.dependencies=FENERGO_JOURNEY_SCHEMA_EVALUATION
step.fenergo_journey_launch.category=JOURNEY_MANAGEMENT
step.fenergo_journey_launch.critical=false
```

### Option 3: YAML Configuration
Create `step-definitions.yml`:

```yaml
# Step Definitions - Clean YAML format
steps:
  XML_TO_JSON_TRANSFORMATION:
    priority: 1
    description: "Transform XML data to JSON format via internal Apigee service"
    dependencies: []
    category: "TRANSFORMATION"
    critical: true
    
  FENERGO_ENTITY_CREATION:
    priority: 2
    description: "Create entity in Fenergo system via Entity API"
    dependencies: ["XML_TO_JSON_TRANSFORMATION"]
    category: "ENTITY_MANAGEMENT"
    critical: true
    
  FENERGO_JOURNEY_SCHEMA_EVALUATION:
    priority: 3
    description: "Evaluate journey schema via Fenergo Logic Engine"
    dependencies: ["FENERGO_ENTITY_CREATION"]
    category: "JOURNEY_MANAGEMENT"
    critical: true
    
  FENERGO_JOURNEY_LAUNCH:
    priority: 4
    description: "Launch journey via Fenergo Journey Command API"
    dependencies: ["FENERGO_JOURNEY_SCHEMA_EVALUATION"]
    category: "JOURNEY_MANAGEMENT"
    critical: false
```

## Benefits of Consolidated Configuration

### 1. **Single Source of Truth**
- All step information in one place
- Easy to see dependencies and priorities
- Clear visualization of the flow

### 2. **Easy Maintenance**
- Change priority: Update one number
- Change dependencies: Update one line
- Add new step: Add one definition

### 3. **Readable Format**
- Clear structure and formatting
- Comments and documentation
- Easy to understand for new developers

### 4. **Version Control Friendly**
- Easy to see changes in git diffs
- Clear history of step modifications
- Easy to review and approve changes

## Example: Adding New Step

### Current Flow
```
XML_TO_JSON_TRANSFORMATION (1) → FENERGO_ENTITY_CREATION (2) → FENERGO_JOURNEY_SCHEMA_EVALUATION (3) → FENERGO_JOURNEY_LAUNCH (4)
```

### Add Data Validation Step
```java
// In StepConfigurationLoader.java
"DATA_VALIDATION", 
new StepDefinition(
    "DATA_VALIDATION",
    2, // Priority (between XML and Entity)
    "Validate transformed JSON data",
    new String[]{"XML_TO_JSON_TRANSFORMATION"}, // Depends on XML transformation
    "VALIDATION",
    true // Critical
),
```

### Update Dependencies
```java
// Update FENERGO_ENTITY_CREATION to depend on DATA_VALIDATION
"FENERGO_ENTITY_CREATION", 
new StepDefinition(
    "FENERGO_ENTITY_CREATION",
    3, // Priority moved down
    "Create entity in Fenergo system via Entity API",
    new String[]{"DATA_VALIDATION"}, // Now depends on data validation
    "ENTITY_MANAGEMENT",
    true
),
```

### New Flow
```
XML_TO_JSON_TRANSFORMATION (1) → DATA_VALIDATION (2) → FENERGO_ENTITY_CREATION (3) → FENERGO_JOURNEY_SCHEMA_EVALUATION (4) → FENERGO_JOURNEY_LAUNCH (5)
```

## Configuration Comparison

| Aspect | Inline (Current) | Properties File | YAML File |
|--------|------------------|-----------------|-----------|
| **Readability** | ⚠️ Medium | ✅ High | ✅ Very High |
| **Maintainability** | ⚠️ Medium | ✅ High | ✅ Very High |
| **Version Control** | ⚠️ Medium | ✅ High | ✅ Very High |
| **External Editing** | ❌ No | ✅ Yes | ✅ Yes |
| **Type Safety** | ✅ High | ⚠️ Medium | ⚠️ Medium |
| **Performance** | ✅ High | ⚠️ Medium | ⚠️ Medium |

## Recommended Approach

### For Development: Inline Configuration
- Fast iteration
- Type safety
- Easy debugging

### For Production: External Configuration
- Easy maintenance
- Environment-specific configs
- Non-developer friendly

## Implementation Status

✅ **Current Implementation**: Inline consolidated configuration
- All step info in one place
- Easy to read and maintain
- Clear dependency visualization

🔄 **Future Enhancement**: External configuration support
- Properties file support
- YAML file support
- Environment-specific configurations

---

*The current consolidated inline configuration provides the best balance of readability, maintainability, and performance while keeping all step information in one place.*
