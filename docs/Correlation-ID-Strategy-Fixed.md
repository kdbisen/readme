# 🔄 **Correlation ID Strategy - Proper Duplicate Handling**

## Overview

**You were absolutely correct!** The original implementation had a **critical design flaw** where duplicate correlation IDs would cause issues. This has now been **completely fixed** with a proper correlation ID strategy.

---

## 🚨 **The Original Problem**

### **What Was Wrong:**
1. **`correlationId` was indexed but NOT unique** in MongoDB
2. **Multiple processes could have the same correlation ID**
3. **Repository `findByCorrelationId()` returned `Optional<OnboardingProcess>`** - only found the first one
4. **No strategy for handling duplicate correlation IDs**
5. **Could cause data loss, overwrites, or failed lookups**

### **Is this correct practice?** ❌ **NO!**

---

## ✅ **The Solution - Proper Correlation ID Strategy**

### **1. Enhanced Data Model**
```java
@Document(collection = "onboarding_processes")
public class OnboardingProcess {
    @Indexed(unique = true)
    private String processId;        // UNIQUE - Each process has unique ID
    
    @Indexed
    private String correlationId;   // NOT UNIQUE - Multiple processes can share correlation ID
}
```

### **2. Enhanced Repository**
```java
@Repository
public interface OnboardingProcessRepository extends MongoRepository<OnboardingProcess, String> {
    
    // Find by process ID - UNIQUE constraint ensures only one result
    Optional<OnboardingProcess> findByProcessId(String processId);
    
    // Find by correlation ID - Returns ALL processes with same correlation ID
    List<OnboardingProcess> findByCorrelationId(String correlationId);
    
    // Find by correlation ID ordered by creation time (newest first)
    List<OnboardingProcess> findByCorrelationIdOrderByCreatedAtDesc(String correlationId);
    
    // Find active processes by correlation ID
    @Query("{ 'correlationId': ?0, 'status': { $nin: ['COMPLETED', 'FAILED', 'CANCELLED'] } }")
    List<OnboardingProcess> findActiveByCorrelationId(String correlationId);
    
    // Check if correlation ID has any active processes
    @Query(value = "{ 'correlationId': ?0, 'status': { $nin: ['COMPLETED', 'FAILED', 'CANCELLED'] } }", exists = true)
    boolean existsActiveByCorrelationId(String correlationId);
    
    // Count processes by correlation ID
    long countByCorrelationId(String correlationId);
}
```

### **3. Correlation ID Strategy Service**
```java
@Service
public class CorrelationIdStrategyService {
    
    /**
     * Handle correlation ID strategy when processing new request
     */
    public CorrelationIdStrategyResult handleCorrelationIdStrategy(String correlationId, String requestType) {
        List<OnboardingProcess> existingProcesses = processRepository.findByCorrelationIdOrderByCreatedAtDesc(correlationId);
        
        if (existingProcesses.isEmpty()) {
            return CorrelationIdStrategyResult.builder()
                    .strategy(CorrelationIdStrategy.NEW_PROCESS)
                    .shouldProceed(true)
                    .message("New correlation ID - creating new process")
                    .build();
        }
        
        // Analyze existing processes and determine strategy
        long activeCount = existingProcesses.stream()
                .filter(p -> !isTerminalStatus(p.getStatus()))
                .count();
        
        // Determine strategy based on existing processes
        CorrelationIdStrategy strategy = determineStrategy(requestType, activeCount);
        boolean shouldProceed = strategy != CorrelationIdStrategy.REJECT_REQUEST;
        
        return CorrelationIdStrategyResult.builder()
                .strategy(strategy)
                .existingProcesses(existingProcesses)
                .activeProcessCount(activeCount)
                .shouldProceed(shouldProceed)
                .build();
    }
}
```

---

## 🎯 **Correlation ID Strategies**

### **1. NEW_PROCESS** ✅
- **When:** No existing processes with this correlation ID
- **Action:** Create new process normally
- **Use Case:** First request with this correlation ID

### **2. RETRY_PROCESS** ✅
- **When:** Only failed processes exist with this correlation ID
- **Action:** Allow new process (retry scenario)
- **Use Case:** Retrying failed operations

### **3. ALLOW_CONCURRENT** ✅
- **When:** Limited active processes exist (non-critical operations)
- **Action:** Allow new concurrent process
- **Use Case:** Multiple operations for same correlation ID

### **4. REJECT_REQUEST** ✅
- **When:** Too many active processes or critical operations
- **Action:** Reject the request and return existing process
- **Use Case:** Prevent duplicate critical operations

---

## 🔧 **Strategy Rules**

### **Critical Operations (Always Reject Duplicates):**
- **`ADD_KYC`** - Critical onboarding operation
- **`UPDATE_KYC`** - Critical update operation

### **Non-Critical Operations (Allow Limited Concurrent):**
- **`QUERY_KYC`** - Read-only operations
- **`VALIDATE_KYC`** - Validation operations
- **Other operations** - Allow up to 3 concurrent processes

### **Retry Scenarios:**
- **Only failed processes exist** → Allow retry
- **Completed processes exist** → Reject for critical operations, allow for others

---

## 📊 **API Behavior Examples**

### **Scenario 1: New Correlation ID**
```http
POST /api/v1/onboarding/process-entity
X-Correlation-ID: CORR-12345
{
  "xmlData": "<customer>...</customer>",
  "requestType": "ADD_KYC"
}

Response: 200 OK
{
  "processId": "PROC-ABC12345",
  "correlationId": "CORR-12345",
  "status": "IN_PROGRESS",
  "message": "New correlation ID - creating new process"
}
```

### **Scenario 2: Duplicate Critical Operation**
```http
POST /api/v1/onboarding/process-entity
X-Correlation-ID: CORR-12345  # Same correlation ID
{
  "xmlData": "<customer>...</customer>",
  "requestType": "ADD_KYC"     # Critical operation
}

Response: 200 OK
{
  "processId": "PROC-ABC12345",  # Returns existing process
  "correlationId": "CORR-12345",
  "status": "IN_PROGRESS",
  "message": "Active process exists - rejecting duplicate request"
}
```

### **Scenario 3: Retry After Failure**
```http
POST /api/v1/onboarding/process-entity
X-Correlation-ID: CORR-67890
{
  "xmlData": "<customer>...</customer>",
  "requestType": "ADD_KYC"
}

Response: 200 OK
{
  "processId": "PROC-DEF67890",  # New process ID
  "correlationId": "CORR-67890",
  "status": "IN_PROGRESS",
  "message": "Only failed processes exist - allowing retry"
}
```

### **Scenario 4: Multiple Non-Critical Operations**
```http
POST /api/v1/onboarding/process-entity
X-Correlation-ID: CORR-99999
{
  "xmlData": "<customer>...</customer>",
  "requestType": "QUERY_KYC"     # Non-critical operation
}

Response: 200 OK
{
  "processId": "PROC-GHI99999",  # New process ID
  "correlationId": "CORR-99999",
  "status": "IN_PROGRESS",
  "message": "Active process exists - allowing concurrent process"
}
```

---

## 🔍 **Enhanced API Endpoints**

### **1. Get All Processes by Correlation ID**
```http
GET /api/v1/onboarding/correlation/{correlationId}/processes

Response:
{
  "correlationId": "CORR-12345",
  "processes": [
    {
      "processId": "PROC-ABC12345",
      "status": "COMPLETED",
      "createdAt": "2024-01-15T10:30:00",
      "completedAt": "2024-01-15T10:35:00"
    },
    {
      "processId": "PROC-DEF12345",
      "status": "IN_PROGRESS",
      "createdAt": "2024-01-15T11:00:00"
    }
  ],
  "totalProcesses": 2,
  "activeProcesses": 1,
  "completedProcesses": 1
}
```

### **2. Get Latest Process by Correlation ID**
```http
GET /api/v1/onboarding/correlation/{correlationId}/latest

Response:
{
  "processId": "PROC-DEF12345",
  "correlationId": "CORR-12345",
  "status": "IN_PROGRESS",
  "createdAt": "2024-01-15T11:00:00",
  "isLatest": true
}
```

### **3. Check Correlation ID Status**
```http
GET /api/v1/onboarding/correlation/{correlationId}/status

Response:
{
  "correlationId": "CORR-12345",
  "hasActiveProcesses": true,
  "activeProcessCount": 1,
  "totalProcessCount": 2,
  "latestStatus": "IN_PROGRESS",
  "canCreateNewProcess": false,
  "reason": "Active process exists for critical operation"
}
```

---

## 📈 **Benefits**

### **🛡️ Data Integrity**
- **No data loss** - Each process has unique processId
- **No overwrites** - Multiple processes can coexist
- **Proper tracking** - Complete audit trail for all processes

### **🔄 Flexible Retry Logic**
- **Automatic retry** - Failed processes can be retried
- **Smart rejection** - Prevents duplicate critical operations
- **Concurrent support** - Allows multiple non-critical operations

### **📊 Complete Traceability**
- **All processes tracked** - Even duplicates are preserved
- **Correlation history** - Complete history of correlation ID usage
- **Status tracking** - Clear status for each process

### **🎯 Business Logic Compliance**
- **Critical operation protection** - Prevents duplicate KYC operations
- **Retry support** - Allows retry of failed operations
- **Concurrent operations** - Supports multiple queries/validations

---

## ✅ **Summary**

**The correlation ID strategy is now properly implemented:**

- ✅ **No data loss** - Each process has unique processId
- ✅ **Smart duplicate handling** - Different strategies for different scenarios
- ✅ **Critical operation protection** - Prevents duplicate KYC operations
- ✅ **Retry support** - Allows retry of failed operations
- ✅ **Concurrent operations** - Supports multiple non-critical operations
- ✅ **Complete traceability** - All processes tracked and preserved
- ✅ **Business logic compliance** - Follows proper banking operation patterns

**Your Banking Onboarding Service now handles correlation IDs correctly with proper duplicate management!** 🚀

