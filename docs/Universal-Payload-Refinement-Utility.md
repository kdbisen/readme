# Universal Payload Refinement Utility

## Overview

The **PayloadRefinementUtil** is a universal utility that can be used by every step in the Banking Onboarding Service to refine and transform input payloads. It automatically handles input from previous step outputs or initial payloads, making step development consistent and maintainable.

## 🎯 Key Features

### ✅ **Universal Input Handling**
- **Previous Step Output**: Automatically gets input from previous step
- **Initial Payload**: Falls back to initial input data
- **Specific Step Output**: Can target specific step output
- **Flexible Input Sources**: Choose where to get input from

### ✅ **Payload Refinement**
- **Common Refinements**: Add correlation ID, timestamps, clean null values
- **Custom Transformations**: Apply business-specific transformations
- **Data Validation**: Validate and clean input data
- **Type Safety**: Handle different input types (String, Map, Object)

### ✅ **Reusable Transformers**
- **Pre-built Transformers**: Common transformations for different scenarios
- **Chainable Transformers**: Combine multiple transformations
- **Conditional Transformers**: Apply transformations based on conditions
- **Custom Transformers**: Create your own transformation logic

## 🚀 Usage Examples

### Basic Usage

```java
@Component
@RequiredArgsConstructor
public class MyStep implements GenericStepExecutor {
    
    private final PayloadRefinementUtil payloadRefinementUtil;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // 🎯 Get refined payload automatically
        PayloadRefinementUtil.RefinedPayload refinedPayload = 
            payloadRefinementUtil.getRefinedPayload(context, getStepName());
        
        // Use refined data
        Map<String, Object> refinedData = refinedPayload.getRefinedData();
        
        // Your step logic here...
        return StepResult.success(result, getStepName(), context.getCorrelationId());
    }
}
```

### Specify Input Source

```java
// Use previous step output
PayloadRefinementUtil.RefinedPayload refinedPayload = 
    payloadRefinementUtil.getRefinedPayload(
        context, 
        getStepName(),
        PayloadRefinementUtil.InputSource.PREVIOUS_STEP
    );

// Use initial payload
PayloadRefinementUtil.RefinedPayload refinedPayload = 
    payloadRefinementUtil.getRefinedPayload(
        context, 
        getStepName(),
        PayloadRefinementUtil.InputSource.INITIAL_PAYLOAD
    );

// Use specific step output
PayloadRefinementUtil.RefinedPayload refinedPayload = 
    payloadRefinementUtil.getRefinedPayload(
        context, 
        getStepName(),
        PayloadRefinementUtil.InputSource.SPECIFIC_STEP
    );
```

### Apply Custom Transformations

```java
// Apply single transformation
PayloadRefinementUtil.RefinedPayload refinedPayload = 
    payloadRefinementUtil.getRefinedPayload(
        context,
        getStepName(),
        CommonPayloadTransformers.dataValidationTransformer()
    );

// Apply multiple transformations
PayloadRefinementUtil.RefinedPayload refinedPayload = 
    payloadRefinementUtil.getRefinedPayload(
        context,
        getStepName(),
        CommonPayloadTransformers.chainTransformers(
            CommonPayloadTransformers.dataValidationTransformer(),
            CommonPayloadTransformers.fenergoEntityTransformer(),
            CommonPayloadTransformers.dataEnrichmentTransformer(enrichmentData)
        )
    );
```

## 🔧 Available Transformers

### 1. **XML to JSON Transformer**
```java
CommonPayloadTransformers.xmlToJsonTransformer()
```
- Adds transformation markers
- Timestamps the transformation
- Prepares data for JSON processing

### 2. **Data Validation Transformer**
```java
CommonPayloadTransformers.dataValidationTransformer()
```
- Validates input data
- Removes empty strings
- Adds validation markers

### 3. **Fenergo Entity Transformer**
```java
CommonPayloadTransformers.fenergoEntityTransformer()
```
- Adds Fenergo-specific fields
- Sets entity type and status
- Ensures required fields exist

### 4. **Journey Preparation Transformer**
```java
CommonPayloadTransformers.journeyPreparationTransformer()
```
- Adds journey-specific fields
- Sets journey type and status
- Prepares for journey execution

### 5. **Field Mapping Transformer**
```java
CommonPayloadTransformers.fieldMappingTransformer(fieldMappings)
```
- Maps field names from old to new
- Useful for API integration
- Maintains data structure

### 6. **Data Enrichment Transformer**
```java
CommonPayloadTransformers.dataEnrichmentTransformer(enrichmentData)
```
- Adds additional data to payload
- Enriches with external information
- Timestamps enrichment

### 7. **Data Filtering Transformer**
```java
CommonPayloadTransformers.dataFilteringTransformer(allowedFields)
```
- Keeps only specified fields
- Removes unwanted data
- Ensures data security

### 8. **Data Formatting Transformer**
```java
CommonPayloadTransformers.dataFormattingTransformer(formatters)
```
- Applies custom formatting to fields
- Transforms data types
- Ensures consistent format

### 9. **Conditional Transformer**
```java
CommonPayloadTransformers.conditionalTransformer(
    condition,
    trueTransformer,
    falseTransformer
)
```
- Applies different transformations based on condition
- Flexible transformation logic
- Conditional data processing

### 10. **Chain Transformers**
```java
CommonPayloadTransformers.chainTransformers(
    transformer1,
    transformer2,
    transformer3
)
```
- Combines multiple transformers
- Sequential transformation pipeline
- Reusable transformation chains

## 📋 Step Implementation Examples

### Example 1: First Step (Uses Initial Payload)

```java
@Component("xmlToJsonTransformationStep")
@RequiredArgsConstructor
public class XmlToJsonTransformationStep implements GenericStepExecutor {
    
    private final PayloadRefinementUtil payloadRefinementUtil;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Get refined payload from initial input
        PayloadRefinementUtil.RefinedPayload refinedPayload = 
            payloadRefinementUtil.getRefinedPayload(
                context, 
                getStepName(),
                PayloadRefinementUtil.InputSource.INITIAL_PAYLOAD
            );
        
        // Extract XML data
        Map<String, Object> refinedData = refinedPayload.getRefinedData();
        String xmlData = extractXmlData(refinedData);
        
        // Process transformation...
        return StepResult.success(result, getStepName(), context.getCorrelationId());
    }
}
```

### Example 2: Middle Step (Uses Previous Step Output)

```java
@Component("fenergoEntityCreationStep")
@RequiredArgsConstructor
public class FenergoEntityCreationStep implements GenericStepExecutor {
    
    private final PayloadRefinementUtil payloadRefinementUtil;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Get refined payload from previous step (automatic)
        PayloadRefinementUtil.RefinedPayload refinedPayload = 
            payloadRefinementUtil.getRefinedPayload(context, getStepName());
        
        // Apply Fenergo-specific transformations
        PayloadRefinementUtil.RefinedPayload fenergoPayload = 
            payloadRefinementUtil.getRefinedPayload(
                context,
                getStepName(),
                CommonPayloadTransformers.chainTransformers(
                    CommonPayloadTransformers.dataValidationTransformer(),
                    CommonPayloadTransformers.fenergoEntityTransformer()
                )
            );
        
        // Use transformed data
        Map<String, Object> entityData = fenergoPayload.getRefinedData();
        
        // Process entity creation...
        return StepResult.success(result, getStepName(), context.getCorrelationId());
    }
}
```

### Example 3: Last Step (Uses Previous Step Output)

```java
@Component("fenergoJourneyLaunchStep")
@RequiredArgsConstructor
public class FenergoJourneyLaunchStep implements GenericStepExecutor {
    
    private final PayloadRefinementUtil payloadRefinementUtil;
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Get refined payload from previous step
        PayloadRefinementUtil.RefinedPayload refinedPayload = 
            payloadRefinementUtil.getRefinedPayload(
                context, 
                getStepName(),
                PayloadRefinementUtil.InputSource.PREVIOUS_STEP
            );
        
        // Apply journey-specific transformations
        PayloadRefinementUtil.RefinedPayload journeyPayload = 
            payloadRefinementUtil.getRefinedPayload(
                context,
                getStepName(),
                CommonPayloadTransformers.journeyPreparationTransformer()
            );
        
        // Use transformed data
        Map<String, Object> journeyData = journeyPayload.getRefinedData();
        
        // Process journey launch...
        return StepResult.success(result, getStepName(), context.getCorrelationId());
    }
}
```

## 🎯 RefinedPayload Structure

### RefinedPayload Object
```java
public class RefinedPayload {
    private Object originalData;                    // Original input data
    private Map<String, Object> refinedData;        // Refined/transformed data
    private RefinedPayloadMetadata metadata;       // Metadata about refinement
}
```

### RefinedPayloadMetadata
```java
public class RefinedPayloadMetadata {
    private String source;                          // Input source (PREVIOUS_STEP, INITIAL_PAYLOAD, etc.)
    private String originalType;                    // Original data type
    private String refinedType;                     // Refined data type
    private String correlationId;                   // Correlation ID
    private LocalDateTime refinementTimestamp;     // When refinement occurred
    private boolean customTransformation;           // Whether custom transformation was applied
    private LocalDateTime transformationTimestamp;  // When transformation occurred
}
```

## 🔍 Common Refinements Applied

### Automatic Refinements
The utility automatically applies these refinements to every payload:

1. **Correlation ID**: Adds correlation ID if not present
2. **Timestamp**: Adds current timestamp
3. **Null Cleanup**: Removes null values
4. **String Trimming**: Trims whitespace from strings
5. **Type Conversion**: Converts to Map for easier manipulation

### Custom Refinements
You can apply additional refinements using transformers:

1. **Data Validation**: Validate and clean data
2. **Field Mapping**: Rename fields
3. **Data Enrichment**: Add additional data
4. **Data Filtering**: Keep only specific fields
5. **Data Formatting**: Format field values

## 🚀 Benefits

### 1. **Consistency**
- ✅ Same input handling across all steps
- ✅ Consistent data structure
- ✅ Standardized refinement process

### 2. **Flexibility**
- ✅ Multiple input sources
- ✅ Customizable transformations
- ✅ Chainable transformers

### 3. **Maintainability**
- ✅ Centralized refinement logic
- ✅ Reusable transformers
- ✅ Easy to modify and extend

### 4. **Debugging**
- ✅ Rich metadata for debugging
- ✅ Clear transformation history
- ✅ Input source tracking

### 5. **Type Safety**
- ✅ Handles different input types
- ✅ Safe type conversions
- ✅ Error handling

## 🎯 Best Practices

### 1. **Use Appropriate Input Source**
```java
// First step - use initial payload
PayloadRefinementUtil.InputSource.INITIAL_PAYLOAD

// Middle steps - use previous step output
PayloadRefinementUtil.InputSource.PREVIOUS_STEP

// Specific requirements - use specific step
PayloadRefinementUtil.InputSource.SPECIFIC_STEP
```

### 2. **Chain Transformations**
```java
// Chain related transformations
CommonPayloadTransformers.chainTransformers(
    CommonPayloadTransformers.dataValidationTransformer(),
    CommonPayloadTransformers.fenergoEntityTransformer()
)
```

### 3. **Handle Errors Gracefully**
```java
try {
    PayloadRefinementUtil.RefinedPayload refinedPayload = 
        payloadRefinementUtil.getRefinedPayload(context, getStepName());
    // Use refined payload
} catch (Exception e) {
    log.error("Failed to refine payload: {}", e.getMessage());
    return StepResult.failure("Payload refinement failed", getStepName(), context.getCorrelationId());
}
```

### 4. **Log Refinement Details**
```java
log.info("Refined payload source: {}, type: {}", 
    refinedPayload.getMetadata().getSource(),
    refinedPayload.getMetadata().getOriginalType());
```

## 🎉 Summary

The **PayloadRefinementUtil** provides:

✅ **Universal Input Handling** - Works with any input source
✅ **Automatic Refinements** - Common refinements applied automatically
✅ **Custom Transformations** - Business-specific transformations
✅ **Reusable Components** - Pre-built transformers for common scenarios
✅ **Rich Metadata** - Detailed information about refinement process
✅ **Type Safety** - Handles different input types safely
✅ **Easy Integration** - Simple to use in any step

**This utility makes step development consistent, maintainable, and flexible while providing powerful payload refinement capabilities!** 🚀
