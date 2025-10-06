# Clean Database Collections Design

## Overview
Designed a comprehensive, normalized database structure with **7 specialized collections** for optimal data organization, retrieval, and performance.

## 🗄️ Database Collections Architecture

### **1. `processes` Collection**
**Purpose**: Core process information and metadata
```javascript
{
  "_id": "ObjectId",
  "processId": "PROC-12345-67890",
  "correlationId": "CORR-abc123-def456",
  "requestType": "ADD_KYC",
  "status": "COMPLETED",
  "tenantId": "tenant-001",
  "userId": "user-123",
  "clientId": "client-456",
  "businessUnit": "Retail Banking",
  "priority": "HIGH",
  "totalExecutionTimeMs": 15000,
  "totalStepsExecuted": 5,
  "successfulSteps": 5,
  "failedSteps": 0,
  "createdAt": "2024-01-15T10:30:00Z",
  "completedAt": "2024-01-15T10:30:15Z"
}
```

**Indexes**:
- `correlationId` (unique)
- `processId` (unique)
- `{correlationId: 1, requestType: 1}` (compound)
- `{status: 1, createdAt: 1}` (compound)

### **2. `steps` Collection**
**Purpose**: Individual step execution details
```javascript
{
  "_id": "ObjectId",
  "processId": "PROC-12345-67890",
  "correlationId": "CORR-abc123-def456",
  "stepId": "STEP-001",
  "stepName": "XML_TO_JSON_TRANSFORMATION",
  "stepOrder": 1,
  "status": "COMPLETED",
  "startedAt": "2024-01-15T10:30:00Z",
  "completedAt": "2024-01-15T10:30:03Z",
  "durationMs": 3000,
  "priority": 1,
  "dependencies": ["STEP-000"],
  "executorClass": "XmlToJsonTransformationStep",
  "executionMode": "SYNC",
  "retryCount": 0,
  "maxRetries": 3,
  "memoryUsedBytes": 1048576,
  "cpuTimeMs": 2500
}
```

**Indexes**:
- `processId`
- `stepId`
- `{processId: 1, stepOrder: 1}` (compound)
- `{stepName: 1, status: 1}` (compound)
- `{correlationId: 1, stepName: 1}` (compound)

### **3. `step_payloads` Collection**
**Purpose**: Complete input/output data for each step
```javascript
{
  "_id": "ObjectId",
  "stepId": "STEP-001",
  "processId": "PROC-12345-67890",
  "correlationId": "CORR-abc123-def456",
  "stepName": "XML_TO_JSON_TRANSFORMATION",
  "payloadType": "INPUT",
  "payloadData": { /* Complete payload object */ },
  "payloadRawString": "<xml>...</xml>",
  "payloadRawBytes": Buffer,
  "contentType": "application/xml",
  "encoding": "UTF-8",
  "payloadSizeBytes": 2048,
  "payloadHash": "sha256:abc123...",
  "payloadFormat": "XML",
  "payloadSchema": "v1.0",
  "isCompressed": false,
  "isEncrypted": false,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Indexes**:
- `stepId`
- `processId`
- `{stepId: 1, payloadType: 1}` (compound)
- `{processId: 1, stepName: 1}` (compound)

### **4. `process_inputs` Collection**
**Purpose**: Original input data for processes
```javascript
{
  "_id": "ObjectId",
  "processId": "PROC-12345-67890",
  "correlationId": "CORR-abc123-def456",
  "inputType": "XML",
  "inputSource": "API",
  "inputData": { /* Complete input object */ },
  "inputRawString": "<xml>...</xml>",
  "inputRawBytes": Buffer,
  "contentType": "application/xml",
  "encoding": "UTF-8",
  "inputSizeBytes": 4096,
  "inputHash": "sha256:def456...",
  "inputFormat": "XML",
  "inputSchema": "v2.0",
  "sourceSystem": "External API",
  "sourceVersion": "1.2.3",
  "sourceEndpoint": "/api/onboarding",
  "sourceHeaders": {
    "Authorization": "Bearer token",
    "Content-Type": "application/xml"
  },
  "receivedAt": "2024-01-15T10:29:45Z",
  "processedAt": "2024-01-15T10:30:00Z"
}
```

**Indexes**:
- `processId`
- `correlationId`
- `{processId: 1, inputType: 1}` (compound)
- `{correlationId: 1, inputSource: 1}` (compound)

### **5. `process_outputs` Collection**
**Purpose**: Final output data for processes
```javascript
{
  "_id": "ObjectId",
  "processId": "PROC-12345-67890",
  "correlationId": "CORR-abc123-def456",
  "outputType": "SUCCESS",
  "outputStatus": "COMPLETED",
  "outputData": { /* Complete output object */ },
  "outputRawString": "{\"result\": \"success\"}",
  "outputRawBytes": Buffer,
  "contentType": "application/json",
  "encoding": "UTF-8",
  "outputSizeBytes": 1024,
  "outputHash": "sha256:ghi789...",
  "outputFormat": "JSON",
  "outputSchema": "v1.0",
  "resultCode": "SUCCESS",
  "resultMessage": "Process completed successfully",
  "resultSummary": {
    "totalSteps": 5,
    "successfulSteps": 5,
    "executionTime": 15000
  },
  "destinationSystem": "Fenergo",
  "destinationEndpoint": "/api/entities",
  "isDelivered": true,
  "deliveryStatus": "SUCCESS",
  "deliveryAttempts": 1,
  "generatedAt": "2024-01-15T10:30:15Z",
  "deliveredAt": "2024-01-15T10:30:16Z"
}
```

**Indexes**:
- `processId`
- `correlationId`
- `{processId: 1, outputType: 1}` (compound)
- `{correlationId: 1, outputStatus: 1}` (compound)

### **6. `audit_logs` Collection**
**Purpose**: Comprehensive audit trail
```javascript
{
  "_id": "ObjectId",
  "processId": "PROC-12345-67890",
  "correlationId": "CORR-abc123-def456",
  "eventType": "STEP_EXECUTE",
  "eventCategory": "STEP",
  "eventAction": "EXECUTE",
  "eventDescription": "Executed XML to JSON transformation step",
  "eventSource": "SYSTEM",
  "eventTarget": "XmlToJsonTransformationStep",
  "userId": "user-123",
  "userName": "john.doe",
  "userRole": "ADMIN",
  "sessionId": "session-abc123",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "eventData": {
    "stepName": "XML_TO_JSON_TRANSFORMATION",
    "stepOrder": 1,
    "executionTime": 3000
  },
  "beforeState": { /* Previous state */ },
  "afterState": { /* New state */ },
  "eventResult": "SUCCESS",
  "eventMessage": "Step executed successfully",
  "timestamp": "2024-01-15T10:30:03Z",
  "eventDurationMs": 3000,
  "securityLevel": "INTERNAL",
  "complianceFlags": "GDPR,SOX",
  "isSensitiveData": false,
  "systemVersion": "1.0.0",
  "environment": "PROD"
}
```

**Indexes**:
- `processId`
- `correlationId`
- `{correlationId: 1, eventType: 1}` (compound)
- `{processId: 1, timestamp: 1}` (compound)
- `{userId: 1, timestamp: 1}` (compound)

### **7. `error_events` Collection**
**Purpose**: Detailed error tracking
```javascript
{
  "_id": "ObjectId",
  "processId": "PROC-12345-67890",
  "stepId": "STEP-002",
  "correlationId": "CORR-abc123-def456",
  "errorType": "EXTERNAL_API_ERROR",
  "errorCategory": "TECHNICAL",
  "errorSeverity": "HIGH",
  "errorCode": "API_TIMEOUT",
  "errorMessage": "External API call timed out",
  "errorDescription": "Fenergo API did not respond within timeout period",
  "exceptionClass": "java.net.SocketTimeoutException",
  "stackTrace": "java.net.SocketTimeoutException...",
  "errorSource": "API_CALL",
  "errorComponent": "FenergoService",
  "errorMethod": "createEntity",
  "errorLineNumber": 45,
  "errorContext": {
    "apiEndpoint": "/api/entities",
    "timeoutMs": 30000,
    "retryCount": 2
  },
  "isRecoverable": true,
  "recoveryAction": "RETRY",
  "retryCount": 2,
  "maxRetries": 3,
  "retryStrategy": "EXPONENTIAL_BACKOFF",
  "isResolved": true,
  "resolutionAction": "RETRY_SUCCESS",
  "resolvedBy": "system",
  "resolvedAt": "2024-01-15T10:30:10Z",
  "timestamp": "2024-01-15T10:30:05Z",
  "impactLevel": "MEDIUM",
  "businessImpact": "Delayed processing",
  "isUserFacing": false,
  "affectedUsers": 0
}
```

**Indexes**:
- `processId`
- `stepId`
- `correlationId`
- `{correlationId: 1, errorType: 1}` (compound)
- `{processId: 1, stepId: 1}` (compound)
- `{errorSeverity: 1, timestamp: 1}` (compound)

### **8. `process_metrics` Collection**
**Purpose**: Performance and operational metrics
```javascript
{
  "_id": "ObjectId",
  "processId": "PROC-12345-67890",
  "correlationId": "CORR-abc123-def456",
  "requestType": "ADD_KYC",
  "metricType": "PERFORMANCE",
  "metricCategory": "PROCESS",
  "totalExecutionTimeMs": 15000,
  "stepExecutionTimeMs": 12000,
  "waitingTimeMs": 2000,
  "processingTimeMs": 1000,
  "totalSteps": 5,
  "successfulSteps": 5,
  "failedSteps": 0,
  "skippedSteps": 0,
  "retriedSteps": 0,
  "memoryUsedBytes": 52428800,
  "cpuTimeMs": 10000,
  "diskIOMs": 500,
  "networkIOMs": 2000,
  "businessUnit": "Retail Banking",
  "clientId": "client-456",
  "tenantId": "tenant-001",
  "successRate": 100.0,
  "errorRate": 0.0,
  "retryRate": 0.0,
  "averageStepTimeMs": 2400.0,
  "maxStepTimeMs": 5000.0,
  "minStepTimeMs": 1000.0,
  "customMetrics": {
    "documentsProcessed": 3,
    "confidenceScore": 95.5
  },
  "timestamp": "2024-01-15T10:30:15Z",
  "processStartTime": "2024-01-15T10:30:00Z",
  "processEndTime": "2024-01-15T10:30:15Z",
  "percentile50": 12000.0,
  "percentile90": 18000.0,
  "percentile95": 20000.0,
  "percentile99": 25000.0
}
```

**Indexes**:
- `processId`
- `correlationId`
- `{processId: 1, metricType: 1}` (compound)
- `{correlationId: 1, timestamp: 1}` (compound)
- `{requestType: 1, timestamp: 1}` (compound)

## 🎯 Benefits of This Design

### **1. Data Separation & Organization**
- **Process Management**: Core process info in `processes`
- **Step Tracking**: Detailed step execution in `steps`
- **Data Storage**: Complete payloads in `step_payloads`
- **Input/Output**: Original and final data in separate collections
- **Audit Trail**: Complete audit logs in `audit_logs`
- **Error Tracking**: Detailed error information in `error_events`
- **Metrics**: Performance data in `process_metrics`

### **2. Optimized Queries**
```javascript
// Get process with all steps
db.processes.findOne({correlationId: "CORR-123"})
db.steps.find({processId: "PROC-123"}).sort({stepOrder: 1})

// Get step with complete payloads
db.steps.findOne({stepId: "STEP-001"})
db.step_payloads.find({stepId: "STEP-001"})

// Get all errors for a process
db.error_events.find({processId: "PROC-123"})

// Get audit trail
db.audit_logs.find({correlationId: "CORR-123"}).sort({timestamp: 1})

// Get performance metrics
db.process_metrics.find({processId: "PROC-123"})
```

### **3. Efficient Indexing**
- **Compound Indexes**: Optimized for common query patterns
- **Unique Indexes**: Prevent duplicate data
- **Covering Indexes**: Reduce document scans
- **Partial Indexes**: Index only relevant documents

### **4. Scalability**
- **Horizontal Scaling**: Collections can be sharded independently
- **Read Replicas**: Different collections on different replicas
- **Caching**: Frequently accessed data can be cached separately
- **Archiving**: Old data can be archived by collection

### **5. Data Integrity**
- **Referential Integrity**: Process ID links all related data
- **Data Validation**: Schema validation per collection
- **Hash Verification**: Data integrity checks
- **Audit Trail**: Complete change tracking

## 🔍 Query Patterns

### **Process Overview**
```javascript
// Get process summary
const process = db.processes.findOne({correlationId: "CORR-123"})
const steps = db.steps.find({processId: process.processId}).sort({stepOrder: 1})
const metrics = db.process_metrics.findOne({processId: process.processId})
```

### **Step Details**
```javascript
// Get step with complete data
const step = db.steps.findOne({stepId: "STEP-001"})
const inputPayload = db.step_payloads.findOne({stepId: "STEP-001", payloadType: "INPUT"})
const outputPayload = db.step_payloads.findOne({stepId: "STEP-001", payloadType: "OUTPUT"})
```

### **Error Analysis**
```javascript
// Get all errors for analysis
const errors = db.error_events.find({processId: "PROC-123"})
const errorStats = db.error_events.aggregate([
  {$match: {processId: "PROC-123"}},
  {$group: {_id: "$errorType", count: {$sum: 1}}}
])
```

### **Performance Analysis**
```javascript
// Get performance metrics
const metrics = db.process_metrics.find({requestType: "ADD_KYC"})
const avgPerformance = db.process_metrics.aggregate([
  {$match: {requestType: "ADD_KYC"}},
  {$group: {_id: null, avgTime: {$avg: "$totalExecutionTimeMs"}}}
])
```

## 📊 Collection Relationships

```
processes (1) ──→ (N) steps
processes (1) ──→ (1) process_inputs
processes (1) ──→ (1) process_outputs
processes (1) ──→ (N) audit_logs
processes (1) ──→ (N) error_events
processes (1) ──→ (1) process_metrics

steps (1) ──→ (N) step_payloads
steps (1) ──→ (N) audit_logs
steps (1) ──→ (N) error_events
```

## 🚀 Implementation Benefits

✅ **Clean Data Organization** - Each collection has a specific purpose  
✅ **Optimized Queries** - Compound indexes for common patterns  
✅ **Complete Data Storage** - No data truncation or loss  
✅ **Audit Trail** - Complete change tracking  
✅ **Error Tracking** - Detailed error analysis  
✅ **Performance Metrics** - Comprehensive monitoring  
✅ **Scalability** - Independent collection scaling  
✅ **Data Integrity** - Hash verification and validation  
✅ **Easy Retrieval** - Optimized query patterns  
✅ **Maintainability** - Clear separation of concerns  

This design provides a robust, scalable, and maintainable database structure for the banking onboarding service! 🎯
