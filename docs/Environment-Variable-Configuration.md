# Environment Variable Configuration Guide

## 🎯 **Overview**

The banking onboarding service uses environment variables for flexible configuration across different environments. This approach eliminates the need for multiple configuration files and allows for easy deployment customization.

## 📋 **Environment Variable Format**

```bash
# Format: ${ENV_VAR:default_value}
endpoint.SUBMIT_KYC.timeoutMs=${SUBMIT_KYC_TIMEOUT:45000}
endpoint.SUBMIT_KYC.authScope=${SUBMIT_KYC_AUTH_SCOPE:fenergo-kyc-write}
```

## 🔧 **Available Environment Variables**

### **Entity Management Endpoints**

| Variable | Default | Description |
|----------|---------|-------------|
| `CREATE_ENTITY_TIMEOUT` | `30000` | Timeout for entity creation (ms) |
| `CREATE_ENTITY_RETRIES` | `3` | Retry attempts for entity creation |
| `CREATE_ENTITY_AUTH_SCOPE` | `fenergo-entity-write` | Auth scope for entity creation |
| `GET_ENTITY_TIMEOUT` | `15000` | Timeout for entity retrieval (ms) |
| `GET_ENTITY_RETRIES` | `2` | Retry attempts for entity retrieval |
| `GET_ENTITY_AUTH_SCOPE` | `fenergo-entity-read` | Auth scope for entity retrieval |
| `UPDATE_ENTITY_TIMEOUT` | `30000` | Timeout for entity updates (ms) |
| `UPDATE_ENTITY_RETRIES` | `3` | Retry attempts for entity updates |
| `UPDATE_ENTITY_AUTH_SCOPE` | `fenergo-entity-write` | Auth scope for entity updates |
| `DELETE_ENTITY_TIMEOUT` | `20000` | Timeout for entity deletion (ms) |
| `DELETE_ENTITY_RETRIES` | `2` | Retry attempts for entity deletion |
| `DELETE_ENTITY_AUTH_SCOPE` | `fenergo-entity-write` | Auth scope for entity deletion |

### **KYC Operations**

| Variable | Default | Description |
|----------|---------|-------------|
| `SUBMIT_KYC_TIMEOUT` | `45000` | Timeout for KYC submission (ms) |
| `SUBMIT_KYC_RETRIES` | `3` | Retry attempts for KYC submission |
| `SUBMIT_KYC_AUTH_SCOPE` | `fenergo-kyc-write` | Auth scope for KYC submission |
| `GET_KYC_STATUS_TIMEOUT` | `15000` | Timeout for KYC status check (ms) |
| `GET_KYC_STATUS_RETRIES` | `2` | Retry attempts for KYC status check |
| `GET_KYC_STATUS_AUTH_SCOPE` | `fenergo-kyc-read` | Auth scope for KYC status check |

### **Compliance Operations**

| Variable | Default | Description |
|----------|---------|-------------|
| `RUN_COMPLIANCE_CHECK_TIMEOUT` | `60000` | Timeout for compliance checks (ms) |
| `RUN_COMPLIANCE_CHECK_RETRIES` | `2` | Retry attempts for compliance checks |
| `RUN_COMPLIANCE_CHECK_AUTH_SCOPE` | `fenergo-compliance-write` | Auth scope for compliance checks |
| `GET_COMPLIANCE_RESULTS_TIMEOUT` | `20000` | Timeout for compliance results (ms) |
| `GET_COMPLIANCE_RESULTS_RETRIES` | `2` | Retry attempts for compliance results |
| `GET_COMPLIANCE_RESULTS_AUTH_SCOPE` | `fenergo-compliance-read` | Auth scope for compliance results |

### **Risk Assessment**

| Variable | Default | Description |
|----------|---------|-------------|
| `ASSESS_RISK_TIMEOUT` | `30000` | Timeout for risk assessment (ms) |
| `ASSESS_RISK_RETRIES` | `3` | Retry attempts for risk assessment |
| `ASSESS_RISK_AUTH_SCOPE` | `fenergo-risk-write` | Auth scope for risk assessment |
| `GET_RISK_PROFILE_TIMEOUT` | `15000` | Timeout for risk profile retrieval (ms) |
| `GET_RISK_PROFILE_RETRIES` | `2` | Retry attempts for risk profile retrieval |
| `GET_RISK_PROFILE_AUTH_SCOPE` | `fenergo-risk-read` | Auth scope for risk profile retrieval |

### **Public Endpoints**

| Variable | Default | Description |
|----------|---------|-------------|
| `HEALTH_CHECK_TIMEOUT` | `5000` | Timeout for health checks (ms) |
| `HEALTH_CHECK_RETRIES` | `1` | Retry attempts for health checks |
| `API_INFO_TIMEOUT` | `5000` | Timeout for API info (ms) |
| `API_INFO_RETRIES` | `1` | Retry attempts for API info |

## 🚀 **Environment-Specific Configurations**

### **Development Environment**

```bash
# Faster timeouts for development
export SUBMIT_KYC_TIMEOUT=10000
export SUBMIT_KYC_RETRIES=1
export GET_KYC_STATUS_TIMEOUT=5000
export GET_KYC_STATUS_RETRIES=1
export RUN_COMPLIANCE_CHECK_TIMEOUT=30000
export RUN_COMPLIANCE_CHECK_RETRIES=1
```

### **Production Environment**

```bash
# Longer timeouts for production reliability
export SUBMIT_KYC_TIMEOUT=60000
export SUBMIT_KYC_RETRIES=3
export GET_KYC_STATUS_TIMEOUT=20000
export GET_KYC_STATUS_RETRIES=2
export RUN_COMPLIANCE_CHECK_TIMEOUT=120000
export RUN_COMPLIANCE_CHECK_RETRIES=2
```

### **Testing Environment**

```bash
# Minimal timeouts for testing
export SUBMIT_KYC_TIMEOUT=5000
export SUBMIT_KYC_RETRIES=1
export GET_KYC_STATUS_TIMEOUT=2000
export GET_KYC_STATUS_RETRIES=1
export RUN_COMPLIANCE_CHECK_TIMEOUT=10000
export RUN_COMPLIANCE_CHECK_RETRIES=1
```

## 🐳 **Docker Configuration**

### **Docker Compose Example**

```yaml
version: '3.8'
services:
  banking-onboarding-service:
    image: banking-onboarding-service:latest
    environment:
      # Entity Management
      - CREATE_ENTITY_TIMEOUT=30000
      - CREATE_ENTITY_RETRIES=3
      - CREATE_ENTITY_AUTH_SCOPE=fenergo-entity-write
      
      # KYC Operations
      - SUBMIT_KYC_TIMEOUT=45000
      - SUBMIT_KYC_RETRIES=3
      - SUBMIT_KYC_AUTH_SCOPE=fenergo-kyc-write
      
      # Compliance Operations
      - RUN_COMPLIANCE_CHECK_TIMEOUT=60000
      - RUN_COMPLIANCE_CHECK_RETRIES=2
      - RUN_COMPLIANCE_CHECK_AUTH_SCOPE=fenergo-compliance-write
      
      # Risk Assessment
      - ASSESS_RISK_TIMEOUT=30000
      - ASSESS_RISK_RETRIES=3
      - ASSESS_RISK_AUTH_SCOPE=fenergo-risk-write
      
      # Public Endpoints
      - HEALTH_CHECK_TIMEOUT=5000
      - HEALTH_CHECK_RETRIES=1
    ports:
      - "8080:8080"
```

### **Dockerfile Example**

```dockerfile
FROM openjdk:17-jdk-slim

COPY target/banking-onboarding-service-*.jar app.jar

# Set default environment variables
ENV SUBMIT_KYC_TIMEOUT=45000
ENV SUBMIT_KYC_RETRIES=3
ENV SUBMIT_KYC_AUTH_SCOPE=fenergo-kyc-write

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## ☸️ **Kubernetes Configuration**

### **ConfigMap Example**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: banking-onboarding-config
data:
  # Entity Management
  CREATE_ENTITY_TIMEOUT: "30000"
  CREATE_ENTITY_RETRIES: "3"
  CREATE_ENTITY_AUTH_SCOPE: "fenergo-entity-write"
  
  # KYC Operations
  SUBMIT_KYC_TIMEOUT: "45000"
  SUBMIT_KYC_RETRIES: "3"
  SUBMIT_KYC_AUTH_SCOPE: "fenergo-kyc-write"
  
  # Compliance Operations
  RUN_COMPLIANCE_CHECK_TIMEOUT: "60000"
  RUN_COMPLIANCE_CHECK_RETRIES: "2"
  RUN_COMPLIANCE_CHECK_AUTH_SCOPE: "fenergo-compliance-write"
```

### **Deployment Example**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: banking-onboarding-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: banking-onboarding-service
  template:
    metadata:
      labels:
        app: banking-onboarding-service
    spec:
      containers:
      - name: banking-onboarding-service
        image: banking-onboarding-service:latest
        envFrom:
        - configMapRef:
            name: banking-onboarding-config
        ports:
        - containerPort: 8080
```

## 🔍 **Runtime Configuration Inspection**

### **Check Current Configuration**

```bash
# Get all endpoint configurations
curl http://localhost:8080/api/config/endpoints

# Get specific endpoint configuration
curl http://localhost:8080/api/config/endpoints/SUBMIT_KYC

# Get configuration statistics
curl http://localhost:8080/api/config/stats
```

### **Reload Configuration**

```bash
# Reload configurations (useful for testing)
curl -X POST http://localhost:8080/api/config/reload
```

## 🛠️ **Best Practices**

### **1. Environment-Specific Values**

```bash
# Development - Fast feedback
export SUBMIT_KYC_TIMEOUT=10000
export SUBMIT_KYC_RETRIES=1

# Production - Reliability
export SUBMIT_KYC_TIMEOUT=60000
export SUBMIT_KYC_RETRIES=3

# Testing - Minimal overhead
export SUBMIT_KYC_TIMEOUT=5000
export SUBMIT_KYC_RETRIES=1
```

### **2. Security Considerations**

```bash
# Use environment-specific auth scopes
export SUBMIT_KYC_AUTH_SCOPE=fenergo-kyc-write-prod
export GET_ENTITY_AUTH_SCOPE=fenergo-entity-read-prod
```

### **3. Monitoring and Alerting**

```bash
# Set appropriate timeouts for monitoring
export HEALTH_CHECK_TIMEOUT=5000
export API_INFO_TIMEOUT=5000
```

### **4. Load Testing**

```bash
# Adjust timeouts for load testing
export SUBMIT_KYC_TIMEOUT=30000
export SUBMIT_KYC_RETRIES=1
export RUN_COMPLIANCE_CHECK_TIMEOUT=45000
export RUN_COMPLIANCE_CHECK_RETRIES=1
```

## 🔧 **Troubleshooting**

### **Common Issues**

1. **Environment Variable Not Found**
   ```bash
   # Check if variable is set
   echo $SUBMIT_KYC_TIMEOUT
   
   # Set default value
   export SUBMIT_KYC_TIMEOUT=45000
   ```

2. **Invalid Timeout Values**
   ```bash
   # Ensure numeric values
   export SUBMIT_KYC_TIMEOUT=45000  # ✅ Correct
   export SUBMIT_KYC_TIMEOUT=abc    # ❌ Invalid
   ```

3. **Configuration Not Loading**
   ```bash
   # Check application logs
   tail -f logs/application.log | grep "endpoint configuration"
   
   # Reload configuration
   curl -X POST http://localhost:8080/api/config/reload
   ```

### **Debug Commands**

```bash
# Check all environment variables
env | grep -E "(TIMEOUT|RETRIES|AUTH_SCOPE)"

# Check specific endpoint configuration
curl http://localhost:8080/api/config/endpoints/SUBMIT_KYC

# Check configuration statistics
curl http://localhost:8080/api/config/stats
```

## 📊 **Configuration Validation**

The service automatically validates configuration values:

- **Timeouts**: Must be positive integers (milliseconds)
- **Retries**: Must be non-negative integers
- **Auth Scopes**: Must be non-empty strings
- **Methods**: Must be valid HTTP methods (GET, POST, PUT, DELETE)

Invalid configurations will be logged and default values will be used.

## 🎯 **Summary**

Environment variable configuration provides:

✅ **Flexibility**: Easy environment-specific customization  
✅ **Security**: No hardcoded values in configuration files  
✅ **Maintainability**: Single configuration file with environment overrides  
✅ **Deployment**: Easy Docker/Kubernetes configuration  
✅ **Testing**: Simple environment-specific test configurations  

This approach eliminates the need for multiple configuration files while providing maximum flexibility for different deployment environments.
