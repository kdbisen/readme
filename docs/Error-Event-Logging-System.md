# Error Event Logging Collection & Service

## Overview
Comprehensive error event logging system with detailed tracking, analysis, and management capabilities using MongoDB collections.

## 🗄️ Error Events Collection Structure

### **Collection: `error_events`**

```javascript
{
  "_id": "ObjectId",
  "processId": "PROC-12345-67890",
  "stepId": "STEP-001",
  "correlationId": "CORR-abc123-def456",
  
  // Error Classification
  "errorType": "EXTERNAL_API_ERROR",
  "errorCategory": "TECHNICAL",
  "errorSeverity": "HIGH",
  "errorCode": "API_TIMEOUT",
  
  // Error Details
  "errorMessage": "External API call timed out",
  "errorDescription": "Fenergo API did not respond within timeout period",
  "exceptionClass": "java.net.SocketTimeoutException",
  "stackTrace": "java.net.SocketTimeoutException...",
  
  // Error Context
  "errorSource": "API_CALL",
  "errorComponent": "FenergoService",
  "errorMethod": "createEntity",
  "errorLineNumber": 45,
  "errorContext": {
    "apiEndpoint": "/api/entities",
    "timeoutMs": 30000,
    "retryCount": 2
  },
  "errorData": {
    "requestPayload": {...},
    "responseHeaders": {...}
  },
  "errorPayload": {...},
  "errorPayloadType": "JSON",
  
  // Recovery Information
  "isRecoverable": true,
  "recoveryAction": "RETRY",
  "retryCount": 2,
  "maxRetries": 3,
  "retryStrategy": "EXPONENTIAL_BACKOFF",
  
  // Resolution Information
  "isResolved": true,
  "resolutionAction": "RETRY_SUCCESS",
  "resolvedBy": "system",
  "resolvedAt": "2024-01-15T10:30:10Z",
  "resolutionNotes": "Error resolved through retry mechanism",
  
  // Timestamps
  "timestamp": "2024-01-15T10:30:05Z",
  "firstOccurrence": "2024-01-15T10:30:05Z",
  "lastOccurrence": "2024-01-15T10:30:08Z",
  
  // Impact Assessment
  "impactLevel": "MEDIUM",
  "businessImpact": "Delayed processing",
  "technicalImpact": "API timeout",
  "isUserFacing": false,
  "affectedUsers": 0,
  
  // System Context
  "systemVersion": "1.0.0",
  "environment": "PROD",
  "region": "US-EAST",
  "systemContext": {
    "instanceId": "instance-123",
    "availabilityZone": "us-east-1a"
  }
}
```

## 🔍 Error Classification System

### **Error Types**
- `VALIDATION_ERROR` - Input validation failures
- `SYSTEM_ERROR` - Internal system errors
- `EXTERNAL_API_ERROR` - External service failures
- `SECURITY_ERROR` - Security violations
- `TIMEOUT_ERROR` - Timeout-related errors
- `CONNECTION_ERROR` - Network connectivity issues
- `RESOURCE_ERROR` - Resource exhaustion (memory, disk, etc.)
- `BUSINESS_ERROR` - Business rule violations
- `CONFIGURATION_ERROR` - Configuration-related errors

### **Error Categories**
- `BUSINESS` - Business logic errors
- `TECHNICAL` - Technical implementation errors
- `INFRASTRUCTURE` - Infrastructure-related errors
- `SECURITY` - Security-related errors
- `DATA` - Data-related errors
- `INTEGRATION` - Integration-related errors

### **Error Severity Levels**
- `LOW` - Minor issues, no immediate impact
- `MEDIUM` - Moderate issues, some impact
- `HIGH` - Significant issues, noticeable impact
- `CRITICAL` - Severe issues, major impact

### **Impact Levels**
- `LOW` - Minimal impact
- `MEDIUM` - Moderate impact
- `HIGH` - Significant impact
- `CRITICAL` - Severe impact

## 🚀 Error Event Logging Service Features

### **1. Comprehensive Error Logging**
```java
// Log error from exception
ErrorEvent errorEvent = errorEventLoggingService.logErrorFromException(
    processId, stepId, correlationId, exception, context);

// Log custom error
ErrorEventRequest request = ErrorEventRequest.builder()
    .processId(processId)
    .stepId(stepId)
    .correlationId(correlationId)
    .errorType("VALIDATION_ERROR")
    .errorSeverity("MEDIUM")
    .errorMessage("Invalid input data")
    .isRecoverable(true)
    .recoveryAction("VALIDATE_INPUT")
    .build();

ErrorEvent errorEvent = errorEventLoggingService.logError(request);
```

### **2. Retry Management**
```java
// Update error with retry information
ErrorEvent updatedEvent = errorEventLoggingService.updateErrorWithRetry(
    errorEventId, retryCount, "SUCCESS");

// Resolve error after successful retry
ErrorEvent resolvedEvent = errorEventLoggingService.resolveError(
    errorEventId, "system", "RETRY_SUCCESS", "Resolved through retry");
```

### **3. Error Analysis**
```java
// Get comprehensive error analysis
ErrorAnalysis analysis = errorEventLoggingService.getErrorAnalysis(processId);

// Get error statistics
ErrorStatistics statistics = errorEventLoggingService.getErrorStatistics(since);

// Get specific error types
List<ErrorEvent> unresolvedErrors = errorEventLoggingService.getUnresolvedErrors();
List<ErrorEvent> criticalErrors = errorEventLoggingService.getCriticalErrors();
List<ErrorEvent> userFacingErrors = errorEventLoggingService.getUserFacingErrors();
```

## 📊 Error Analysis Capabilities

### **Error Analysis Response**
```javascript
{
  "processId": "PROC-12345-67890",
  "totalErrors": 15,
  "errorsByType": {
    "EXTERNAL_API_ERROR": 8,
    "VALIDATION_ERROR": 4,
    "SYSTEM_ERROR": 3
  },
  "errorsBySeverity": {
    "HIGH": 5,
    "MEDIUM": 7,
    "LOW": 3
  },
  "errorsByCategory": {
    "TECHNICAL": 10,
    "BUSINESS": 3,
    "INFRASTRUCTURE": 2
  },
  "recoverableErrors": 12,
  "resolvedErrors": 10,
  "userFacingErrors": 2,
  "criticalErrors": 1,
  "errors": [...]
}
```

### **Error Statistics Response**
```javascript
{
  "totalErrors": 150,
  "errorsByType": {
    "EXTERNAL_API_ERROR": 60,
    "VALIDATION_ERROR": 40,
    "SYSTEM_ERROR": 30,
    "SECURITY_ERROR": 20
  },
  "errorsBySeverity": {
    "CRITICAL": 10,
    "HIGH": 40,
    "MEDIUM": 70,
    "LOW": 30
  },
  "errorsByCategory": {
    "TECHNICAL": 80,
    "BUSINESS": 40,
    "INFRASTRUCTURE": 20,
    "SECURITY": 10
  },
  "resolutionRate": 85.5,
  "averageResolutionTime": 45.2
}
```

## 🔧 REST API Endpoints

### **Error Logging Endpoints**
```http
POST /api/error-events/log
POST /api/error-events/log-exception
PUT /api/error-events/{errorEventId}/retry
PUT /api/error-events/{errorEventId}/resolve
```

### **Error Analysis Endpoints**
```http
GET /api/error-events/analysis/process/{processId}
GET /api/error-events/unresolved
GET /api/error-events/critical
GET /api/error-events/user-facing
GET /api/error-events/correlation/{correlationId}
GET /api/error-events/statistics?since=2024-01-01T00:00:00
GET /api/error-events/{errorEventId}
```

### **Health Check**
```http
GET /api/error-events/health
```

## 📈 Database Indexes

### **Primary Indexes**
- `processId` - Find errors by process
- `stepId` - Find errors by step
- `correlationId` - Find errors by correlation
- `errorType` - Find errors by type
- `errorSeverity` - Find errors by severity
- `errorCode` - Find errors by code

### **Compound Indexes**
- `{correlationId: 1, errorType: 1}` - Correlation + type queries
- `{processId: 1, stepId: 1}` - Process + step queries
- `{errorSeverity: 1, timestamp: 1}` - Severity + time queries
- `{isResolved: 1, timestamp: 1}` - Resolution status + time
- `{isUserFacing: 1, timestamp: 1}` - User-facing + time

## 🎯 Query Patterns

### **Find All Errors for a Process**
```javascript
db.error_events.find({processId: "PROC-123"})
```

### **Find Unresolved Critical Errors**
```javascript
db.error_events.find({
  isResolved: false,
  errorSeverity: "CRITICAL"
})
```

### **Find Errors by Type and Date Range**
```javascript
db.error_events.find({
  errorType: "EXTERNAL_API_ERROR",
  timestamp: {
    $gte: ISODate("2024-01-01"),
    $lt: ISODate("2024-02-01")
  }
})
```

### **Find User-Facing Errors**
```javascript
db.error_events.find({isUserFacing: true})
```

### **Error Statistics Aggregation**
```javascript
db.error_events.aggregate([
  {$match: {timestamp: {$gte: ISODate("2024-01-01")}}},
  {$group: {
    _id: "$errorType",
    count: {$sum: 1},
    avgResolutionTime: {$avg: "$resolutionTime"}
  }}
])
```

## 🔍 Error Recovery Strategies

### **Automatic Recovery**
- **RETRY** - Retry the operation
- **EXPONENTIAL_BACKOFF** - Retry with increasing delays
- **CIRCUIT_BREAKER** - Temporarily disable failing service
- **FALLBACK** - Use alternative implementation

### **Manual Recovery**
- **MANUAL_INTERVENTION** - Requires human intervention
- **CONFIGURATION_CHANGE** - Update configuration
- **CODE_DEPLOYMENT** - Deploy fix
- **INFRASTRUCTURE_CHANGE** - Update infrastructure

## 📊 Monitoring & Alerting

### **Key Metrics**
- **Error Rate** - Errors per minute/hour
- **Resolution Rate** - Percentage of resolved errors
- **Average Resolution Time** - Time to resolve errors
- **Critical Error Count** - Number of critical errors
- **User-Facing Error Count** - Number of user-facing errors

### **Alerting Rules**
- **Critical Error Alert** - Alert on any critical error
- **High Error Rate Alert** - Alert on high error rate
- **Unresolved Error Alert** - Alert on unresolved errors
- **User-Facing Error Alert** - Alert on user-facing errors

## 🚀 Benefits

✅ **Comprehensive Error Tracking** - Complete error lifecycle management  
✅ **Detailed Error Analysis** - Rich analytics and reporting  
✅ **Automatic Error Classification** - Smart error categorization  
✅ **Retry Management** - Built-in retry and recovery mechanisms  
✅ **Performance Monitoring** - Error impact and resolution metrics  
✅ **Audit Trail** - Complete error history and resolution tracking  
✅ **Real-time Monitoring** - Live error tracking and alerting  
✅ **Scalable Architecture** - Handles high-volume error logging  
✅ **REST API** - Easy integration with external systems  
✅ **Rich Querying** - Flexible error search and analysis  

The error event logging system provides enterprise-grade error tracking and management capabilities! 🎯
