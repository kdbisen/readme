# Simple Configuration Guide

## 🎯 **Just 3 Environment Variables!**

Instead of complex configuration, just set these 3 simple variables:

```bash
# KYC Operations (timeout in milliseconds)
export KYC_TIMEOUT=30000
export KYC_RETRIES=3
export KYC_AUTH_SCOPE=fenergo-kyc-write

# Compliance Operations
export COMPLIANCE_TIMEOUT=60000
export COMPLIANCE_RETRIES=2
export COMPLIANCE_AUTH_SCOPE=fenergo-compliance-write
```

## 🚀 **Quick Examples**

### **Development (Fast)**
```bash
export KYC_TIMEOUT=10000
export KYC_RETRIES=1
export COMPLIANCE_TIMEOUT=30000
export COMPLIANCE_RETRIES=1
```

### **Production (Reliable)**
```bash
export KYC_TIMEOUT=60000
export KYC_RETRIES=3
export COMPLIANCE_TIMEOUT=120000
export COMPLIANCE_RETRIES=2
```

### **Testing (Minimal)**
```bash
export KYC_TIMEOUT=5000
export KYC_RETRIES=1
export COMPLIANCE_TIMEOUT=10000
export COMPLIANCE_RETRIES=1
```

## 🐳 **Docker Example**

```yaml
version: '3.8'
services:
  banking-service:
    image: banking-onboarding-service:latest
    environment:
      - KYC_TIMEOUT=30000
      - KYC_RETRIES=3
      - COMPLIANCE_TIMEOUT=60000
      - COMPLIANCE_RETRIES=2
    ports:
      - "8080:8080"
```

## ☸️ **Kubernetes Example**

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
        - name: KYC_RETRIES
          value: "3"
        - name: COMPLIANCE_TIMEOUT
          value: "60000"
        - name: COMPLIANCE_RETRIES
          value: "2"
```

## ✅ **That's It!**

No complex configuration files, no multiple environments to manage. Just set the variables you need and you're done!

**Default values work perfectly for most cases.**
