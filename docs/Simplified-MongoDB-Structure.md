# Simplified MongoDB Collection Structure

## Overview

The banking onboarding service now uses a **much simpler and cleaner MongoDB structure** with only **3 main collections** instead of the previous complex multi-collection approach.

## Collection Structure

### 1. `processes` Collection
**Purpose**: Main collection for tracking onboarding processes

**Fields**:
```json
{
  "_id": "ObjectId",
  "process_id": "String (unique)",
  "correlation_id": "String (indexed)",
  "request_type": "String (ADD_KYC, UPDATE_KYC, etc.)",
  "status": "String (INITIATED, PROCESSING, COMPLETED, FAILED)",
  "current_step": "String",
  "message": "String",
  "error_message": "String",
  "payload": "String (original input)",
  "result_data": "Map<String, Object> (final results)",
  "created_at": "LocalDateTime",
  "updated_at": "LocalDateTime",
  "completed_at": "LocalDateTime"
}
```

**Indexes**:
- `process_id` (unique)
- `correlation_id`
- `status`
- `created_at` (descending)

### 2. `steps` Collection
**Purpose**: Individual step tracking within each process

**Fields**:
```json
{
  "_id": "ObjectId",
  "process_id": "String (indexed)",
  "correlation_id": "String (indexed)",
  "step_name": "String (APIGEE_TRANSFORM, ENTITY_CREATE, etc.)",
  "step_order": "Integer (1, 2, 3, 4, 5)",
  "status": "String (PENDING, IN_PROGRESS, COMPLETED, FAILED)",
  "message": "String",
  "error_message": "String",
  "input_data": "String",
  "output_data": "String",
  "step_data": "Map<String, Object>",
  "duration_ms": "Long",
  "retry_count": "Integer",
  "created_at": "LocalDateTime",
  "updated_at": "LocalDateTime",
  "completed_at": "LocalDateTime"
}
```

**Indexes**:
- `process_id`
- `correlation_id`
- `step_name`
- `status`
- `created_at` (descending)

### 3. `logs` Collection
**Purpose**: Error and audit logs

**Fields**:
```json
{
  "_id": "ObjectId",
  "process_id": "String (indexed)",
  "correlation_id": "String (indexed)",
  "log_type": "String (ERROR, INFO, AUDIT, API_CALL)",
  "level": "String (ERROR, WARN, INFO, DEBUG)",
  "message": "String",
  "error_details": "String",
  "stack_trace": "String",
  "context_data": "Map<String, Object>",
  "service_name": "String",
  "endpoint": "String",
  "http_method": "String",
  "response_code": "Integer",
  "duration_ms": "Long",
  "timestamp": "LocalDateTime"
}
```

**Indexes**:
- `process_id`
- `correlation_id`
- `log_type`
- `level`
- `timestamp` (descending)

## Key Benefits of Simplified Structure

### ✅ **Simplicity**
- Only 3 collections instead of 8+ complex collections
- Clear, intuitive field names
- Easy to understand and maintain

### ✅ **Performance**
- Optimized indexes for common queries
- Minimal joins required
- Fast lookups by process_id and correlation_id

### ✅ **Flexibility**
- Generic `step_data` and `result_data` fields for extensibility
- Easy to add new step types without schema changes
- Contextual logging with flexible metadata

### ✅ **Traceability**
- Complete audit trail with correlation_id
- Step-by-step process tracking
- Comprehensive error logging

## Process Flow

### 1. Process Initiation
```java
OnboardingProcess process = OnboardingProcess.builder()
    .processId("PROC-12345")
    .correlationId("CORR-67890")
    .requestType("ADD_KYC")
    .status("PROCESSING")
    .currentStep("APIGEE_TRANSFORM")
    .payload("<xml>...</xml>")
    .createdAt(LocalDateTime.now())
    .build();
```

### 2. Step Execution
```java
Step step = Step.builder()
    .processId("PROC-12345")
    .correlationId("CORR-67890")
    .stepName("APIGEE_TRANSFORM")
    .stepOrder(1)
    .status("IN_PROGRESS")
    .createdAt(LocalDateTime.now())
    .build();
```

### 3. Step Completion
```java
step.setStatus("COMPLETED");
step.setOutputData("{\"transformed\": \"json\"}");
step.setDurationMs(1500L);
step.setCompletedAt(LocalDateTime.now());
```

### 4. Process Completion
```java
process.setStatus("COMPLETED");
process.setResultData(Map.of("clientId", "CLIENT-123", "journeyId", "JOURNEY-456"));
process.setCompletedAt(LocalDateTime.now());
```

## API Endpoints

### Process Entity
```
POST /api/v1/onboarding/process-entity/{requestType}
```
- Accepts XML/JSON payload
- Returns processId and correlationId immediately
- Starts async processing

### Process Status
```
GET /api/v1/onboarding/status/{processId}
```
- Returns current process status
- Shows all completed steps
- Includes timing and error information

### Journey Details
```
GET /api/v1/onboarding/journey/{processId}
```
- Returns detailed journey information
- Shows step-by-step progress
- Includes final results

## Database Configuration

The simplified structure uses `SimplifiedDatabaseConfig` which:
- Creates optimized indexes automatically
- Uses simple MongoDB operations
- Provides better performance than complex joins

## Migration from Complex Structure

The old complex structure with multiple collections has been **completely removed**:
- ❌ `FenergoProcess` → ✅ `OnboardingProcess`
- ❌ `FenergoProcessStep` → ✅ `Step`
- ❌ `FenergoJourney` → ✅ Integrated into `OnboardingProcess`
- ❌ `FenergoJourneyStep` → ✅ Integrated into `Step`
- ❌ Multiple repositories → ✅ 3 simple repositories

## Benefits Summary

| Aspect | Before | After |
|--------|--------|-------|
| Collections | 8+ complex | 3 simple |
| Models | 15+ classes | 6 classes |
| Repositories | 8+ interfaces | 3 interfaces |
| Complexity | High | Low |
| Maintainability | Difficult | Easy |
| Performance | Variable | Optimized |
| Understanding | Complex | Simple |

This simplified structure provides **better performance**, **easier maintenance**, and **clearer data relationships** while maintaining all the functionality of the original complex system.
