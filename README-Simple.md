# Banking Onboarding Service - Simple Setup

## 🚀 **Quick Start**

### **1. Run the Service**
```bash
# Just run it - defaults work perfectly!
java -jar banking-onboarding-service.jar
```

### **2. Test It**
```bash
# Health check
curl http://localhost:8080/health

# Submit KYC (example)
curl -X POST http://localhost:8080/api/process-entity/ADD_KYC \
  -H "Content-Type: application/xml" \
  -d "<entity><name>John Doe</name></entity>"
```

## ⚙️ **Simple Configuration**

Only set these if you need different values:

```bash
# KYC Operations
export KYC_TIMEOUT=30000      # milliseconds
export KYC_RETRIES=3          # retry attempts
export KYC_AUTH_SCOPE=fenergo-kyc-write

# Compliance Operations  
export COMPLIANCE_TIMEOUT=60000
export COMPLIANCE_RETRIES=2
export COMPLIANCE_AUTH_SCOPE=fenergo-compliance-write
```

## 🐳 **Docker**

```bash
# Run with Docker
docker run -p 8080:8080 banking-onboarding-service:latest

# Or with custom settings
docker run -p 8080:8080 \
  -e KYC_TIMEOUT=10000 \
  -e KYC_RETRIES=1 \
  banking-onboarding-service:latest
```

## ☸️ **Kubernetes**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: banking-service
spec:
  template:
    spec:
      containers:
      - name: banking-service
        image: banking-onboarding-service:latest
        env:
        - name: KYC_TIMEOUT
          value: "30000"
```

## 📊 **Check Configuration**

```bash
# See current settings
curl http://localhost:8080/api/config/stats

# List all endpoints
curl http://localhost:8080/api/config/endpoints
```

## 🎯 **That's It!**

- **Default values work for most cases**
- **Only 6 environment variables to know**
- **No complex configuration files**
- **Works out of the box**

**Need help?** Check the logs or the `/health` endpoint.
