# 📊 **Comprehensive Logging System Documentation**

## 🎯 **Overview**

This banking onboarding service implements a comprehensive logging system designed for production environments with Kibana integration, MongoDB error storage, and automatic request/response logging.

## 🏗️ **Architecture Components**

### **1. Logback Configuration (`logback-spring.xml`)**
- **JSON-structured logs** for Kibana compatibility
- **Multiple appenders**: Console, File, Error-specific, Request/Response
- **MongoDB appender** for error persistence
- **Environment-specific** configurations (dev/prod)
- **Rolling file policies** with size and time-based rotation

### **2. Correlation ID Management**
- **Automatic generation** if not provided in headers
- **MDC integration** for thread-local correlation tracking
- **Multi-ID support**: Correlation ID, Trace ID, Process ID
- **Header propagation** in all HTTP responses

### **3. Error Logging Service**
- **MongoDB persistence** of all errors with full context
- **Categorized error types**: API, Processing, Proxy, Validation
- **Rich context** including correlation IDs, stack traces, and metadata
- **Automatic error categorization** and indexing

### **4. Request/Response Logging**
- **Automatic interception** of all HTTP requests/responses
- **Content sanitization** (removes sensitive data)
- **Performance metrics** (duration tracking)
- **Structured logging** with correlation IDs

## 📋 **Logging Features**

### **🔍 Correlation Tracking**
```json
{
  "correlationId": "CORR-A1B2C3D4",
  "traceId": "TRACE-E5F6G7H8",
  "processId": "PROC-I9J0K1L2",
  "timestamp": "2025-09-30T08:15:30.123Z",
  "service": "banking-onboarding-service"
}
```

### **📊 Kibana-Ready JSON Format**
```json
{
  "timestamp": "2025-09-30T08:15:30.123Z",
  "level": "INFO",
  "logger": "com.banking.onboarding.controller.OnboardingController",
  "message": "Request processed successfully",
  "correlationId": "CORR-A1B2C3D4",
  "traceId": "TRACE-E5F6G7H8",
  "processId": "PROC-I9J0K1L2",
  "service": "banking-onboarding-service",
  "environment": "prod",
  "version": "1.0.0",
  "thread": "http-nio-8080-exec-1",
  "class": "OnboardingController"
}
```

### **🗄️ MongoDB Error Storage**
```json
{
  "timestamp": "2025-09-30T08:15:30.123Z",
  "errorType": "API_ERROR",
  "errorMessage": "Fenergo API call failed",
  "correlationId": "CORR-A1B2C3D4",
  "exceptionClass": "WebClientResponseException",
  "exceptionMessage": "500 Internal Server Error",
  "stackTrace": "com.banking.onboarding...",
  "endpoint": "/onboarding/process-entity/ADD_KYC",
  "method": "POST",
  "statusCode": 500,
  "service": "banking-onboarding-service",
  "environment": "prod"
}
```

## 🚀 **Usage Examples**

### **1. Automatic Request Logging**
```java
// Automatically logged by RequestResponseLoggingInterceptor
POST /api/v1/onboarding/process-entity/ADD_KYC
Headers: {X-Correlation-ID: CORR-A1B2C3D4, Content-Type: application/json}
Body: {"entityData": "..."}
Response: 202 Accepted (150ms)
```

### **2. Error Logging to MongoDB**
```java
@Service
public class OnboardingService {
    
    @Autowired
    private ErrorLoggingService errorLoggingService;
    
    public void processEntity(String payload) {
        try {
            // Processing logic
        } catch (Exception e) {
            // Automatically logs to MongoDB with full context
            errorLoggingService.logProcessingError(
                processId, "TRANSFORM", e.getMessage(), e);
        }
    }
}
```

### **3. Manual Error Logging**
```java
// Log API errors
errorLoggingService.logApiError(
    "/fenergo/api/submit", "POST", 500, 
    "Service unavailable", requestPayload, responsePayload);

// Log validation errors
errorLoggingService.logValidationError(
    "KYC_ENTITY", "REQUIRED_FIELD", 
    "Missing required field: customerId", entityData);
```

## 🔧 **Configuration**

### **Environment Variables**
```bash
# Logging environment
export ENVIRONMENT=prod
export APP_VERSION=1.2.0

# MongoDB for error logging
export MONGODB_URI=mongodb://localhost:27017/banking-onboarding

# Log levels
export LOG_LEVEL_ROOT=INFO
export LOG_LEVEL_BANKING=DEBUG
```

### **Application Properties**
```properties
# Logging configuration
logging.level.com.banking.onboarding=INFO
logging.level.com.banking.onboarding.logging=DEBUG
logging.config=classpath:logback-spring.xml

# Environment variables
ENVIRONMENT=${ENVIRONMENT:dev}
APP_VERSION=${APP_VERSION:1.0.0}
MONGODB_URI=${MONGODB_URI:mongodb://localhost:27017/banking-onboarding}
```

## 📁 **Log Files Structure**

```
logs/
├── banking-onboarding-service.log          # All application logs
├── banking-onboarding-service.2025-09-30.0.log  # Daily rotated logs
├── errors.log                              # Error-specific logs
├── errors.2025-09-30.0.log                # Daily rotated error logs
├── requests-responses.log                  # Request/response logs
└── requests-responses.2025-09-30.0.log    # Daily rotated request logs
```

## 🔍 **Kibana Queries**

### **Find All Logs for a Correlation ID**
```json
{
  "query": {
    "term": {
      "correlationId": "CORR-A1B2C3D4"
    }
  }
}
```

### **Find All Errors in Last Hour**
```json
{
  "query": {
    "bool": {
      "must": [
        {"term": {"level": "ERROR"}},
        {"range": {"timestamp": {"gte": "now-1h"}}}
      ]
    }
  }
}
```

### **Find Slow Requests (>5 seconds)**
```json
{
  "query": {
    "bool": {
      "must": [
        {"term": {"logType": "RESPONSE"}},
        {"range": {"durationMs": {"gte": 5000}}}
      ]
    }
  }
}
```

### **Find All Proxy Errors**
```json
{
  "query": {
    "term": {
      "errorType": "PROXY_ERROR"
    }
  }
}
```

## 📊 **Monitoring & Alerting**

### **Key Metrics to Monitor**
- **Error Rate**: Percentage of failed requests
- **Response Time**: P50, P95, P99 percentiles
- **Correlation Coverage**: Percentage of requests with correlation IDs
- **MongoDB Error Storage**: Success rate of error persistence

### **Recommended Alerts**
- Error rate > 5% in 5 minutes
- Response time P95 > 10 seconds
- MongoDB error storage failures
- Missing correlation IDs

## 🛠️ **Troubleshooting**

### **Common Issues**

1. **Missing Correlation IDs**
   - Check if `CorrelationIdInterceptor` is registered
   - Verify `WebConfig` includes the interceptor

2. **MongoDB Error Logging Fails**
   - Check MongoDB connection string
   - Verify `ErrorLoggingService` is properly configured
   - Check MongoDB permissions

3. **Log Files Not Created**
   - Verify log directory permissions
   - Check `logback-spring.xml` configuration
   - Ensure proper file paths

### **Debug Commands**
```bash
# Check log files
tail -f logs/banking-onboarding-service.log
tail -f logs/errors.log

# Check MongoDB error logs
mongo banking-onboarding --eval "db.error_logs.find().sort({timestamp: -1}).limit(10)"

# Test correlation ID
curl -H "X-Correlation-ID: TEST-123" http://localhost:8080/api/v1/health
```

## 🎯 **Best Practices**

1. **Always use correlation IDs** for request tracing
2. **Log errors immediately** when they occur
3. **Sanitize sensitive data** before logging
4. **Use structured logging** for better searchability
5. **Monitor log file sizes** and implement rotation
6. **Set up alerts** for critical error patterns
7. **Regularly review** MongoDB error logs for patterns

## 📈 **Performance Impact**

- **Minimal overhead**: Async logging and MongoDB writes
- **Memory efficient**: Content caching with size limits
- **Disk space**: Configurable rotation policies
- **Network**: MongoDB writes are batched and async

This comprehensive logging system provides full observability for the banking onboarding service with production-ready features for monitoring, debugging, and compliance.
