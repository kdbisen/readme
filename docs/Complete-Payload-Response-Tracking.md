# 📊 **Complete Payload and Response Tracking System**

## Overview

**YES!** Your Banking Onboarding Service now stores **complete payload and response data** for every step execution. This provides full traceability, debugging capabilities, and audit trail for all data transformations and API calls.

---

## 🎯 **What's Stored for Every Step**

### **1. Input Payload** ✅
- **Complete input data** sent to each step
- **Payload type** (XML, JSON, MAP, STRING, etc.)
- **Payload size** in bytes
- **Payload content** as string for easy viewing

### **2. Output Response** ✅
- **Complete output data** from each step
- **Response type** (JSON, XML, MAP, STRING, etc.)
- **Response size** in bytes
- **Response content** as string for easy viewing

### **3. Execution Context** ✅
- **Step name** and configuration
- **Execution duration** in milliseconds
- **Success/failure status**
- **Error messages** and stack traces
- **Additional context** (correlation ID, timestamps, etc.)

### **4. Complete Trace** ✅
- **All steps** in execution order
- **Data flow** between steps
- **Performance metrics** for each step
- **Error tracking** with full context

---

## 🔧 **Enhanced Data Structures**

### **1. GenericStepContext Enhancement**
```java
// New payload and response tracking
private Map<String, StepPayloadResponse> stepPayloadResponses;

// StepPayloadResponse structure
public static class StepPayloadResponse {
    private String stepName;
    private Object inputPayload;           // Complete input data
    private Object outputResponse;         // Complete output data
    private String inputPayloadType;       // XML, JSON, etc.
    private String outputResponseType;    // JSON, XML, etc.
    private long durationMs;              // Execution time
    private boolean success;              // Success status
    private String errorMessage;          // Error details
    private LocalDateTime timestamp;      // Execution timestamp
    private Map<String, Object> additionalContext; // Extra context
}
```

### **2. OnboardingProcess Enhancement**
```java
// Enhanced process tracking
private Map<String, Object> stepPayloadResponses;  // All step data
private Map<String, Object> executionTrace;        // Complete trace
private long totalExecutionTimeMs;                 // Total time
private int totalStepsExecuted;                     // Step count
private int successfulSteps;                        // Success count
private int failedSteps;                            // Failure count
```

### **3. ProcessStep Enhancement**
```java
// Enhanced step tracking
private String inputPayloadType;      // Input type
private String outputResponseType;    // Output type
private int inputPayloadSize;         // Input size
private int outputResponseSize;       // Output size
private Map<String, Object> additionalContext; // Context
private String stackTrace;            // Error stack trace
private String exceptionClass;        // Exception type
```

---

## 🚀 **Automatic Data Capture**

### **1. Step Execution Engine Integration**
The `GenericStepExecutionEngine` automatically captures:

```java
// Before step execution
Object inputPayload = executor.getInputData(context);
String inputPayloadType = determinePayloadType(inputPayload);

// After successful execution
context.storeSuccessfulStepPayloadResponse(
    stepName,
    inputPayload,                    // Complete input
    result.getData(),               // Complete output
    inputPayloadType,               // Input type
    determinePayloadType(result.getData()), // Output type
    duration,                       // Execution time
    additionalContext               // Extra context
);

// After failed execution
context.storeFailedStepPayloadResponse(
    stepName,
    inputPayload,                   // Complete input
    null,                          // No output due to failure
    inputPayloadType,              // Input type
    null,                          // No output type
    duration,                      // Execution time
    errorMessage,                  // Error details
    additionalContext              // Extra context including stack trace
);
```

### **2. Payload Type Detection**
Automatic detection of payload types:

```java
private String determinePayloadType(Object payload) {
    if (payload == null) return "NULL";
    
    String payloadStr = payload.toString().trim();
    if (payloadStr.startsWith("{") && payloadStr.endsWith("}")) {
        return "JSON";
    } else if (payloadStr.startsWith("<") && payloadStr.endsWith(">")) {
        return "XML";
    } else if (payloadStr.startsWith("[") && payloadStr.endsWith("]")) {
        return "JSON_ARRAY";
    } else if (payload instanceof Map) {
        return "MAP";
    } else if (payload instanceof String) {
        return "STRING";
    } else {
        return payload.getClass().getSimpleName().toUpperCase();
    }
}
```

---

## 📊 **New API Endpoints**

### **1. Get Step Payload and Response**
```http
GET /api/v1/onboarding/process/{processId}/step/{stepName}/payload
```

**Response:**
```json
{
  "processId": "PROC_12345",
  "stepName": "XmlToJsonTransformationStep",
  "correlationId": "CORR_67890",
  "payloadResponse": {
    "stepName": "XmlToJsonTransformationStep",
    "inputPayload": "<customer><name>John Doe</name><email>john@example.com</email></customer>",
    "outputResponse": "{\"customer\":{\"name\":\"John Doe\",\"email\":\"john@example.com\"}}",
    "inputPayloadType": "XML",
    "outputResponseType": "JSON",
    "durationMs": 1500,
    "success": true,
    "timestamp": "2024-01-15T10:30:45",
    "additionalContext": {
      "stepConfig": {...},
      "executionTime": 1500,
      "correlationId": "CORR_67890"
    }
  }
}
```

### **2. Get All Step Payloads**
```http
GET /api/v1/onboarding/process/{processId}/payloads
```

**Response:**
```json
{
  "processId": "PROC_12345",
  "correlationId": "CORR_67890",
  "stepPayloadResponses": {
    "XmlToJsonTransformationStep": {...},
    "FenergoEntityCreationStep": {...},
    "FenergoJourneySchemaEvaluationStep": {...},
    "FenergoJourneyLaunchStep": {...},
    "FenergoJourneyDetailsStep": {...}
  },
  "executionTrace": {
    "correlationId": "CORR_67890",
    "processId": "PROC_12345",
    "startedAt": "2024-01-15T10:30:00",
    "totalSteps": 5,
    "stepTraces": {
      "XmlToJsonTransformationStep": {
        "success": true,
        "durationMs": 1500,
        "inputPayloadSize": 89,
        "outputResponseSize": 67,
        "inputPayloadType": "XML",
        "outputResponseType": "JSON",
        "timestamp": "2024-01-15T10:30:01"
      }
    }
  },
  "totalStepsExecuted": 5,
  "successfulSteps": 5,
  "failedSteps": 0,
  "totalExecutionTimeMs": 7500
}
```

### **3. Get Step Execution Summary**
```http
GET /api/v1/onboarding/process/{processId}/step/{stepName}/summary
```

**Response:**
```json
{
  "processId": "PROC_12345",
  "stepName": "XmlToJsonTransformationStep",
  "correlationId": "CORR_67890",
  "summary": {
    "status": "COMPLETED",
    "durationMs": 1500,
    "inputPayloadType": "XML",
    "outputResponseType": "JSON",
    "inputPayloadSize": 89,
    "outputResponseSize": 67,
    "startedAt": "2024-01-15T10:30:00",
    "completedAt": "2024-01-15T10:30:01",
    "errorMessage": null,
    "exceptionClass": null
  }
}
```

### **4. Get Complete Execution Trace**
```http
GET /api/v1/onboarding/process/{processId}/trace
```

**Response:**
```json
{
  "processId": "PROC_12345",
  "correlationId": "CORR_67890",
  "executionTrace": {
    "correlationId": "CORR_67890",
    "processId": "PROC_12345",
    "startedAt": "2024-01-15T10:30:00",
    "totalSteps": 5,
    "stepTraces": {
      "XmlToJsonTransformationStep": {...},
      "FenergoEntityCreationStep": {...},
      "FenergoJourneySchemaEvaluationStep": {...},
      "FenergoJourneyLaunchStep": {...},
      "FenergoJourneyDetailsStep": {...}
    }
  },
  "processSummary": {
    "status": "COMPLETED",
    "requestType": "ADD_KYC",
    "totalStepsExecuted": 5,
    "successfulSteps": 5,
    "failedSteps": 0,
    "totalExecutionTimeMs": 7500,
    "createdAt": "2024-01-15T10:30:00",
    "completedAt": "2024-01-15T10:30:07"
  }
}
```

---

## 🔍 **Usage Examples**

### **1. Access Step Data in Context**
```java
// Get input payload from previous step
Object xmlData = context.getStepInputPayload("XmlToJsonTransformationStep");
String xmlString = context.getStepInputPayloadAsString("XmlToJsonTransformationStep");

// Get output response from previous step
Object jsonData = context.getStepOutputResponse("XmlToJsonTransformationStep");
String jsonString = context.getStepOutputResponseAsString("XmlToJsonTransformationStep");

// Get complete payload and response
GenericStepContext.StepPayloadResponse payloadResponse = 
    context.getStepPayloadResponse("XmlToJsonTransformationStep");
```

### **2. Get Execution Summary**
```java
// Get step execution summary
Map<String, Object> summary = context.getStepExecutionSummary("XmlToJsonTransformationStep");
// Returns: stepName, success, durationMs, inputPayloadSize, outputResponseSize, etc.

// Get complete execution trace
Map<String, Object> trace = context.getCompleteExecutionTrace();
// Returns: correlationId, processId, startedAt, totalSteps, stepTraces, etc.
```

### **3. Manual Payload Storage**
```java
// Store custom payload and response
context.storeSuccessfulStepPayloadResponse(
    "CustomStep",
    inputData,
    outputData,
    "JSON",
    "XML",
    2000,
    Map.of("customField", "customValue")
);
```

---

## 📈 **Benefits**

### **🛡️ Complete Traceability**
- **Full data visibility** - See exactly what data flows through each step
- **Input/output tracking** - Complete payload and response storage
- **Data transformation audit** - Track how data changes at each step
- **Debugging support** - Full context for troubleshooting

### **📊 Advanced Analytics**
- **Performance analysis** - Track execution times and data sizes
- **Data flow analysis** - Understand how data transforms through steps
- **Error analysis** - Complete context for failed steps
- **Trend analysis** - Track performance over time

### **🔍 Enhanced Debugging**
- **Step-by-step debugging** - See data at each step
- **Payload inspection** - View complete input/output data
- **Error context** - Full stack traces and error details
- **Execution timeline** - Complete execution trace

### **📋 Compliance & Audit**
- **Complete audit trail** - Full record of all data transformations
- **Data lineage** - Track data flow from input to output
- **Regulatory compliance** - Complete data processing records
- **Forensic analysis** - Detailed investigation capabilities

---

## 🎯 **Data Storage**

### **MongoDB Collections**
- **`onboarding_processes`** - Enhanced with payload and response data
- **`audit_events`** - Complete error and execution tracking
- **`step_payload_responses`** - Detailed step data (if separate collection needed)

### **Data Structure**
```json
{
  "processId": "PROC_12345",
  "correlationId": "CORR_67890",
  "stepPayloadResponses": {
    "XmlToJsonTransformationStep": {
      "stepName": "XmlToJsonTransformationStep",
      "inputPayload": "<customer>...</customer>",
      "outputResponse": "{\"customer\":{...}}",
      "inputPayloadType": "XML",
      "outputResponseType": "JSON",
      "durationMs": 1500,
      "success": true,
      "timestamp": "2024-01-15T10:30:01",
      "additionalContext": {...}
    }
  },
  "executionTrace": {...},
  "totalStepsExecuted": 5,
  "successfulSteps": 5,
  "failedSteps": 0,
  "totalExecutionTimeMs": 7500
}
```

---

## ✅ **Summary**

**YES! Complete payload and response tracking is now implemented:**

- ✅ **Every step** stores complete input payload and output response
- ✅ **Automatic capture** - No manual intervention required
- ✅ **Rich metadata** - Payload types, sizes, durations, success status
- ✅ **Complete traceability** - Full execution trace with all data
- ✅ **API endpoints** - Easy access to payload and response data
- ✅ **Enhanced debugging** - Complete context for troubleshooting
- ✅ **Compliance ready** - Full audit trail for regulatory requirements
- ✅ **Performance tracking** - Detailed execution metrics

**Your Banking Onboarding Service now provides complete visibility into all data transformations and API calls!** 🚀
