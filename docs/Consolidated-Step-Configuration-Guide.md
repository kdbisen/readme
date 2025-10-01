# Consolidated Step Configuration - Clean & Readable Setup

## Overview

The Banking Onboarding Service now has a **consolidated step configuration** where all step information (name, priority, description, dependencies) is defined in **one place** for maximum readability and maintainability.

## 🎯 New Consolidated Configuration

### Before (Scattered Configuration)
```java
// StepConfigurationLoader.java - Multiple methods scattered across the class
private String getStepDescription(String stepName) {
    return switch (stepName) {
        case "XML_TO_JSON_TRANSFORMATION" -> "Transform XML data...";
        case "FENERGO_ENTITY_CREATION" -> "Create entity...";
        // ... more cases
    };
}

private String[] getStepDependencies(String stepName) {
    return switch (stepName) {
        case "FENERGO_ENTITY_CREATION" -> new String[]{"XML_TO_JSON_TRANSFORMATION"};
        // ... more cases
    };
}

private int getStepPriority(String stepName) {
    return switch (stepName) {
        case "XML_TO_JSON_TRANSFORMATION" -> 1;
        // ... more cases
    };
}
```

### After (Consolidated Configuration) ✅
```java
// StepConfigurationLoader.java - All step info in one place!
private static final Map<String, StepDefinition> STEP_DEFINITIONS = Map.of(
    
    // Step 1: XML Transformation (No dependencies)
    "XML_TO_JSON_TRANSFORMATION", 
    new StepDefinition(
        "XML_TO_JSON_TRANSFORMATION",
        1, // Priority
        "Transform XML data to JSON format via internal Apigee service",
        new String[]{}, // No dependencies
        "TRANSFORMATION",
        true // Critical
    ),
    
    // Step 2: Entity Creation (Depends on XML transformation)
    "FENERGO_ENTITY_CREATION", 
    new StepDefinition(
        "FENERGO_ENTITY_CREATION",
        2, // Priority
        "Create entity in Fenergo system via Entity API",
        new String[]{"XML_TO_JSON_TRANSFORMATION"}, // Depends on step 1
        "ENTITY_MANAGEMENT",
        true // Critical
    ),
    
    // Step 3: Journey Schema Evaluation (Depends on entity creation)
    "FENERGO_JOURNEY_SCHEMA_EVALUATION", 
    new StepDefinition(
        "FENERGO_JOURNEY_SCHEMA_EVALUATION",
        3, // Priority
        "Evaluate journey schema via Fenergo Logic Engine",
        new String[]{"FENERGO_ENTITY_CREATION"}, // Depends on step 2
        "JOURNEY_MANAGEMENT",
        true // Critical
    ),
    
    // Step 4: Journey Launch (Depends on schema evaluation)
    "FENERGO_JOURNEY_LAUNCH", 
    new StepDefinition(
        "FENERGO_JOURNEY_LAUNCH",
        4, // Priority
        "Launch journey via Fenergo Journey Command API",
        new String[]{"FENERGO_JOURNEY_SCHEMA_EVALUATION"}, // Depends on step 3
        "JOURNEY_MANAGEMENT",
        false // Not critical (last step)
    )
);
```

## 🚀 Benefits of Consolidated Configuration

### 1. **Single Source of Truth**
- ✅ All step information in one place
- ✅ Easy to see dependencies and priorities
- ✅ Clear visualization of the flow

### 2. **Easy Maintenance**
- ✅ Change priority: Update one number
- ✅ Change dependencies: Update one line
- ✅ Add new step: Add one definition

### 3. **Readable Format**
- ✅ Clear structure and formatting
- ✅ Comments and documentation
- ✅ Easy to understand for new developers

### 4. **Version Control Friendly**
- ✅ Easy to see changes in git diffs
- ✅ Clear history of step modifications
- ✅ Easy to review and approve changes

## 📋 Step Definition Structure

### StepDefinition Class
```java
public static class StepDefinition {
    public final String stepName;        // Step identifier
    public final int priority;           // Execution order (1, 2, 3, 4...)
    public final String description;     // Human-readable description
    public final String[] dependencies;  // Required previous steps
    public final String category;        // Step category (TRANSFORMATION, ENTITY_MANAGEMENT, etc.)
    public final boolean critical;       // Whether step is critical for success
}
```

### Current Step Definitions

| Step | Priority | Dependencies | Category | Critical |
|------|----------|--------------|----------|----------|
| `XML_TO_JSON_TRANSFORMATION` | 1 | None | TRANSFORMATION | ✅ Yes |
| `FENERGO_ENTITY_CREATION` | 2 | XML_TO_JSON_TRANSFORMATION | ENTITY_MANAGEMENT | ✅ Yes |
| `FENERGO_JOURNEY_SCHEMA_EVALUATION` | 3 | FENERGO_ENTITY_CREATION | JOURNEY_MANAGEMENT | ✅ Yes |
| `FENERGO_JOURNEY_LAUNCH` | 4 | FENERGO_JOURNEY_SCHEMA_EVALUATION | JOURNEY_MANAGEMENT | ❌ No |

## 🔧 How to Modify Steps

### Example 1: Change Step Priority

**Scenario**: Move `FENERGO_JOURNEY_SCHEMA_EVALUATION` before `FENERGO_ENTITY_CREATION`

**Step 1: Update Priorities**
```java
// In STEP_DEFINITIONS
"FENERGO_JOURNEY_SCHEMA_EVALUATION", 
new StepDefinition(
    "FENERGO_JOURNEY_SCHEMA_EVALUATION",
    2, // Changed from 3 to 2
    "Evaluate journey schema via Fenergo Logic Engine",
    new String[]{"XML_TO_JSON_TRANSFORMATION"}, // Updated dependency
    "JOURNEY_MANAGEMENT",
    true
),

"FENERGO_ENTITY_CREATION", 
new StepDefinition(
    "FENERGO_ENTITY_CREATION",
    3, // Changed from 2 to 3
    "Create entity in Fenergo system via Entity API",
    new String[]{"FENERGO_JOURNEY_SCHEMA_EVALUATION"}, // Updated dependency
    "ENTITY_MANAGEMENT",
    true
),
```

**Result**: New execution order becomes:
1. XML_TO_JSON_TRANSFORMATION
2. FENERGO_JOURNEY_SCHEMA_EVALUATION
3. FENERGO_ENTITY_CREATION
4. FENERGO_JOURNEY_LAUNCH

### Example 2: Add New Step

**Scenario**: Add data validation between XML transformation and entity creation

**Step 1: Add New Step Definition**
```java
// Add to STEP_DEFINITIONS
"DATA_VALIDATION", 
new StepDefinition(
    "DATA_VALIDATION",
    2, // Priority between XML and Entity
    "Validate transformed JSON data",
    new String[]{"XML_TO_JSON_TRANSFORMATION"}, // Depends on XML transformation
    "VALIDATION",
    true // Critical
),
```

**Step 2: Update Existing Step Dependencies**
```java
// Update FENERGO_ENTITY_CREATION
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

**Step 3: Update Application Properties**
```properties
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,DATA_VALIDATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH
```

**Result**: New execution order becomes:
1. XML_TO_JSON_TRANSFORMATION
2. DATA_VALIDATION ← **New step**
3. FENERGO_ENTITY_CREATION
4. FENERGO_JOURNEY_SCHEMA_EVALUATION
5. FENERGO_JOURNEY_LAUNCH

### Example 3: Remove Step

**Scenario**: Remove `FENERGO_JOURNEY_SCHEMA_EVALUATION` step

**Step 1: Remove Step Definition**
```java
// Remove from STEP_DEFINITIONS
// "FENERGO_JOURNEY_SCHEMA_EVALUATION", 
// new StepDefinition(...),
```

**Step 2: Update Dependencies**
```java
// Update FENERGO_JOURNEY_LAUNCH to depend directly on FENERGO_ENTITY_CREATION
"FENERGO_JOURNEY_LAUNCH", 
new StepDefinition(
    "FENERGO_JOURNEY_LAUNCH",
    3, // Priority moved up
    "Launch journey via Fenergo Journey Command API",
    new String[]{"FENERGO_ENTITY_CREATION"}, // Updated dependency
    "JOURNEY_MANAGEMENT",
    false
),
```

**Step 3: Update Application Properties**
```properties
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_LAUNCH
```

**Result**: New execution order becomes:
1. XML_TO_JSON_TRANSFORMATION
2. FENERGO_ENTITY_CREATION
3. FENERGO_JOURNEY_LAUNCH

## 🎯 Step Definition Visualization

### Current Flow
```
XML_TO_JSON_TRANSFORMATION (1)
    ↓
FENERGO_ENTITY_CREATION (2)
    ↓
FENERGO_JOURNEY_SCHEMA_EVALUATION (3)
    ↓
FENERGO_JOURNEY_LAUNCH (4)
```

### With Data Validation Added
```
XML_TO_JSON_TRANSFORMATION (1)
    ↓
DATA_VALIDATION (2) ← New step
    ↓
FENERGO_ENTITY_CREATION (3)
    ↓
FENERGO_JOURNEY_SCHEMA_EVALUATION (4)
    ↓
FENERGO_JOURNEY_LAUNCH (5)
```

## 🔍 Debugging and Inspection

### Print Step Definitions
```java
// In StepConfigurationLoader
public void printStepDefinitions() {
    log.info("=== STEP DEFINITIONS ===");
    log.info("Execution Order: {}", executionOrder);
    log.info("Step Definitions:");
    
    STEP_DEFINITIONS.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e1.getValue().priority, e2.getValue().priority))
            .forEach(entry -> {
                StepDefinition def = entry.getValue();
                log.info("  {} | Priority: {} | Dependencies: {} | Category: {} | Critical: {}", 
                        def.stepName, def.priority, Arrays.toString(def.dependencies), 
                        def.category, def.critical);
            });
    log.info("========================");
}
```

### Get Step Definition
```java
// Get specific step definition
StepDefinition def = stepConfigurationLoader.getStepDefinition("FENERGO_ENTITY_CREATION");
log.info("Step: {}, Priority: {}, Dependencies: {}", 
        def.stepName, def.priority, Arrays.toString(def.dependencies));
```

## 📊 Configuration Comparison

| Aspect | Before (Scattered) | After (Consolidated) |
|--------|-------------------|---------------------|
| **Readability** | ❌ Low | ✅ High |
| **Maintainability** | ❌ Low | ✅ High |
| **Single Source** | ❌ No | ✅ Yes |
| **Easy Changes** | ❌ No | ✅ Yes |
| **Version Control** | ❌ Hard | ✅ Easy |
| **Documentation** | ❌ Scattered | ✅ Centralized |

## 🚀 Best Practices

### 1. **Consistent Naming**
- Use descriptive step names
- Follow naming conventions
- Keep names consistent across definitions

### 2. **Clear Dependencies**
- Document dependency relationships
- Keep dependency chains simple
- Avoid circular dependencies

### 3. **Priority Management**
- Use sequential numbers (1, 2, 3, 4...)
- Leave gaps for future steps (1, 2, 4, 5...)
- Use 99 for non-critical steps

### 4. **Category Organization**
- Group related steps by category
- Use consistent category names
- Document category purposes

## 🎉 Summary

**The consolidated step configuration provides:**

✅ **Single Source of Truth** - All step info in one place
✅ **Easy Maintenance** - Change one definition to update everything
✅ **Readable Format** - Clear structure with comments
✅ **Version Control Friendly** - Easy to see and review changes
✅ **Flexible Configuration** - Easy to add, modify, or remove steps

**To modify steps:**
1. ✅ **Update the StepDefinition** in `STEP_DEFINITIONS`
2. ✅ **Update dependencies** as needed
3. ✅ **Update application.properties** if adding/removing steps
4. ✅ **Test the new configuration**

This makes the Banking Onboarding Service **highly maintainable and easy to configure**! 🚀
