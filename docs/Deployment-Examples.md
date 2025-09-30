# Deployment Examples

## 🐳 **Docker Deployment**

### **Dockerfile**

```dockerfile
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy application JAR
COPY target/banking-onboarding-service-*.jar app.jar

# Set default environment variables
ENV SUBMIT_KYC_TIMEOUT=45000
ENV SUBMIT_KYC_RETRIES=3
ENV SUBMIT_KYC_AUTH_SCOPE=fenergo-kyc-write
ENV GET_ENTITY_TIMEOUT=15000
ENV GET_ENTITY_RETRIES=2
ENV GET_ENTITY_AUTH_SCOPE=fenergo-entity-read
ENV RUN_COMPLIANCE_CHECK_TIMEOUT=60000
ENV RUN_COMPLIANCE_CHECK_RETRIES=2
ENV RUN_COMPLIANCE_CHECK_AUTH_SCOPE=fenergo-compliance-write

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### **Docker Compose - Development**

```yaml
version: '3.8'

services:
  banking-onboarding-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      # Development settings - faster timeouts
      - SUBMIT_KYC_TIMEOUT=10000
      - SUBMIT_KYC_RETRIES=1
      - GET_KYC_STATUS_TIMEOUT=5000
      - GET_KYC_STATUS_RETRIES=1
      - RUN_COMPLIANCE_CHECK_TIMEOUT=30000
      - RUN_COMPLIANCE_CHECK_RETRIES=1
      
      # Database
      - SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/banking-onboarding
      
      # JWT Token Service
      - AUTH_TOKEN_SERVICE_URL=http://auth-service:8080
      - AUTH_TOKEN_SERVICE_CLIENT_ID=banking-onboarding-service
      - AUTH_TOKEN_SERVICE_CLIENT_SECRET=dev-secret
      
      # Logging
      - LOGGING_LEVEL_COM_BANKING_ONBOARDING=DEBUG
    depends_on:
      - mongo
      - auth-service
    networks:
      - banking-network

  mongo:
    image: mongo:7.0
    ports:
      - "27017:27017"
    environment:
      - MONGO_INITDB_DATABASE=banking-onboarding
    volumes:
      - mongo_data:/data/db
    networks:
      - banking-network

  auth-service:
    image: auth-service:latest
    ports:
      - "8081:8080"
    environment:
      - AUTH_SERVICE_PORT=8080
    networks:
      - banking-network

volumes:
  mongo_data:

networks:
  banking-network:
    driver: bridge
```

### **Docker Compose - Production**

```yaml
version: '3.8'

services:
  banking-onboarding-service:
    image: banking-onboarding-service:latest
    ports:
      - "8080:8080"
    environment:
      # Production settings - longer timeouts for reliability
      - SUBMIT_KYC_TIMEOUT=60000
      - SUBMIT_KYC_RETRIES=3
      - GET_KYC_STATUS_TIMEOUT=20000
      - GET_KYC_STATUS_RETRIES=2
      - RUN_COMPLIANCE_CHECK_TIMEOUT=120000
      - RUN_COMPLIANCE_CHECK_RETRIES=2
      
      # Database
      - SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/banking-onboarding
      
      # JWT Token Service
      - AUTH_TOKEN_SERVICE_URL=http://auth-service:8080
      - AUTH_TOKEN_SERVICE_CLIENT_ID=banking-onboarding-service
      - AUTH_TOKEN_SERVICE_CLIENT_SECRET=${AUTH_CLIENT_SECRET}
      
      # Logging
      - LOGGING_LEVEL_COM_BANKING_ONBOARDING=INFO
      - LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB=WARN
    depends_on:
      - mongo
      - auth-service
    networks:
      - banking-network
    restart: unless-stopped
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
        reservations:
          memory: 512M
          cpus: '0.25'

  mongo:
    image: mongo:7.0
    ports:
      - "27017:27017"
    environment:
      - MONGO_INITDB_DATABASE=banking-onboarding
    volumes:
      - mongo_data:/data/db
    networks:
      - banking-network
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'
        reservations:
          memory: 1G
          cpus: '0.5'

  auth-service:
    image: auth-service:latest
    ports:
      - "8081:8080"
    environment:
      - AUTH_SERVICE_PORT=8080
    networks:
      - banking-network
    restart: unless-stopped
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 512M
          cpus: '0.25'
        reservations:
          memory: 256M
          cpus: '0.1'

volumes:
  mongo_data:

networks:
  banking-network:
    driver: bridge
```

## ☸️ **Kubernetes Deployment**

### **Namespace**

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: banking-onboarding
```

### **ConfigMap**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: banking-onboarding-config
  namespace: banking-onboarding
data:
  # Entity Management
  CREATE_ENTITY_TIMEOUT: "30000"
  CREATE_ENTITY_RETRIES: "3"
  CREATE_ENTITY_AUTH_SCOPE: "fenergo-entity-write"
  GET_ENTITY_TIMEOUT: "15000"
  GET_ENTITY_RETRIES: "2"
  GET_ENTITY_AUTH_SCOPE: "fenergo-entity-read"
  UPDATE_ENTITY_TIMEOUT: "30000"
  UPDATE_ENTITY_RETRIES: "3"
  UPDATE_ENTITY_AUTH_SCOPE: "fenergo-entity-write"
  DELETE_ENTITY_TIMEOUT: "20000"
  DELETE_ENTITY_RETRIES: "2"
  DELETE_ENTITY_AUTH_SCOPE: "fenergo-entity-write"
  
  # KYC Operations
  SUBMIT_KYC_TIMEOUT: "45000"
  SUBMIT_KYC_RETRIES: "3"
  SUBMIT_KYC_AUTH_SCOPE: "fenergo-kyc-write"
  GET_KYC_STATUS_TIMEOUT: "15000"
  GET_KYC_STATUS_RETRIES: "2"
  GET_KYC_STATUS_AUTH_SCOPE: "fenergo-kyc-read"
  
  # Compliance Operations
  RUN_COMPLIANCE_CHECK_TIMEOUT: "60000"
  RUN_COMPLIANCE_CHECK_RETRIES: "2"
  RUN_COMPLIANCE_CHECK_AUTH_SCOPE: "fenergo-compliance-write"
  GET_COMPLIANCE_RESULTS_TIMEOUT: "20000"
  GET_COMPLIANCE_RESULTS_RETRIES: "2"
  GET_COMPLIANCE_RESULTS_AUTH_SCOPE: "fenergo-compliance-read"
  
  # Risk Assessment
  ASSESS_RISK_TIMEOUT: "30000"
  ASSESS_RISK_RETRIES: "3"
  ASSESS_RISK_AUTH_SCOPE: "fenergo-risk-write"
  GET_RISK_PROFILE_TIMEOUT: "15000"
  GET_RISK_PROFILE_RETRIES: "2"
  GET_RISK_PROFILE_AUTH_SCOPE: "fenergo-risk-read"
  
  # Public Endpoints
  HEALTH_CHECK_TIMEOUT: "5000"
  HEALTH_CHECK_RETRIES: "1"
  API_INFO_TIMEOUT: "5000"
  API_INFO_RETRIES: "1"
```

### **Secret**

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: banking-onboarding-secrets
  namespace: banking-onboarding
type: Opaque
data:
  auth-client-secret: <base64-encoded-secret>
  mongodb-uri: <base64-encoded-mongodb-uri>
```

### **Deployment**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: banking-onboarding-service
  namespace: banking-onboarding
  labels:
    app: banking-onboarding-service
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
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: banking-onboarding-config
        - secretRef:
            name: banking-onboarding-secrets
        env:
        - name: SPRING_DATA_MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: banking-onboarding-secrets
              key: mongodb-uri
        - name: AUTH_TOKEN_SERVICE_CLIENT_SECRET
          valueFrom:
            secretKeyRef:
              name: banking-onboarding-secrets
              key: auth-client-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

### **Service**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: banking-onboarding-service
  namespace: banking-onboarding
spec:
  selector:
    app: banking-onboarding-service
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

### **Ingress**

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: banking-onboarding-ingress
  namespace: banking-onboarding
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - banking-onboarding.example.com
    secretName: banking-onboarding-tls
  rules:
  - host: banking-onboarding.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: banking-onboarding-service
            port:
              number: 80
```

## 🚀 **Helm Chart**

### **Chart.yaml**

```yaml
apiVersion: v2
name: banking-onboarding-service
description: Banking Onboarding Service Helm Chart
version: 1.0.0
appVersion: "1.0.0"
```

### **values.yaml**

```yaml
replicaCount: 3

image:
  repository: banking-onboarding-service
  tag: latest
  pullPolicy: IfNotPresent

service:
  type: ClusterIP
  port: 80

ingress:
  enabled: true
  className: nginx
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
  hosts:
    - host: banking-onboarding.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: banking-onboarding-tls
      hosts:
        - banking-onboarding.example.com

resources:
  limits:
    cpu: 500m
    memory: 1Gi
  requests:
    cpu: 250m
    memory: 512Mi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

# Environment-specific configurations
config:
  # Development
  development:
    SUBMIT_KYC_TIMEOUT: "10000"
    SUBMIT_KYC_RETRIES: "1"
    GET_KYC_STATUS_TIMEOUT: "5000"
    GET_KYC_STATUS_RETRIES: "1"
    RUN_COMPLIANCE_CHECK_TIMEOUT: "30000"
    RUN_COMPLIANCE_CHECK_RETRIES: "1"
  
  # Production
  production:
    SUBMIT_KYC_TIMEOUT: "60000"
    SUBMIT_KYC_RETRIES: "3"
    GET_KYC_STATUS_TIMEOUT: "20000"
    GET_KYC_STATUS_RETRIES: "2"
    RUN_COMPLIANCE_CHECK_TIMEOUT: "120000"
    RUN_COMPLIANCE_CHECK_RETRIES: "2"
  
  # Testing
  testing:
    SUBMIT_KYC_TIMEOUT: "5000"
    SUBMIT_KYC_RETRIES: "1"
    GET_KYC_STATUS_TIMEOUT: "2000"
    GET_KYC_STATUS_RETRIES: "1"
    RUN_COMPLIANCE_CHECK_TIMEOUT: "10000"
    RUN_COMPLIANCE_CHECK_RETRIES: "1"
```

### **templates/configmap.yaml**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ include "banking-onboarding-service.fullname" . }}-config
  namespace: {{ .Release.Namespace }}
data:
  {{- range $key, $value := .Values.config }}
  {{ $key }}: {{ $value | toYaml | quote }}
  {{- end }}
```

## 🔧 **Environment-Specific Deployments**

### **Development Environment**

```bash
# Set development environment variables
export ENVIRONMENT=development
export SUBMIT_KYC_TIMEOUT=10000
export SUBMIT_KYC_RETRIES=1
export GET_KYC_STATUS_TIMEOUT=5000
export GET_KYC_STATUS_RETRIES=1
export RUN_COMPLIANCE_CHECK_TIMEOUT=30000
export RUN_COMPLIANCE_CHECK_RETRIES=1

# Deploy with Docker Compose
docker-compose -f docker-compose.dev.yml up -d

# Deploy with Kubernetes
kubectl apply -f k8s/development/
```

### **Production Environment**

```bash
# Set production environment variables
export ENVIRONMENT=production
export SUBMIT_KYC_TIMEOUT=60000
export SUBMIT_KYC_RETRIES=3
export GET_KYC_STATUS_TIMEOUT=20000
export GET_KYC_STATUS_RETRIES=2
export RUN_COMPLIANCE_CHECK_TIMEOUT=120000
export RUN_COMPLIANCE_CHECK_RETRIES=2

# Deploy with Docker Compose
docker-compose -f docker-compose.prod.yml up -d

# Deploy with Kubernetes
kubectl apply -f k8s/production/
```

### **Testing Environment**

```bash
# Set testing environment variables
export ENVIRONMENT=testing
export SUBMIT_KYC_TIMEOUT=5000
export SUBMIT_KYC_RETRIES=1
export GET_KYC_STATUS_TIMEOUT=2000
export GET_KYC_STATUS_RETRIES=1
export RUN_COMPLIANCE_CHECK_TIMEOUT=10000
export RUN_COMPLIANCE_CHECK_RETRIES=1

# Deploy with Docker Compose
docker-compose -f docker-compose.test.yml up -d

# Deploy with Kubernetes
kubectl apply -f k8s/testing/
```

## 📊 **Monitoring and Observability**

### **Prometheus Configuration**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
    scrape_configs:
    - job_name: 'banking-onboarding-service'
      static_configs:
      - targets: ['banking-onboarding-service:8080']
      metrics_path: '/actuator/prometheus'
```

### **Grafana Dashboard**

```json
{
  "dashboard": {
    "title": "Banking Onboarding Service",
    "panels": [
      {
        "title": "Endpoint Response Times",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{job=\"banking-onboarding-service\"}[5m]))"
          }
        ]
      },
      {
        "title": "Endpoint Error Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_total{job=\"banking-onboarding-service\",status=~\"5..\"}[5m])"
          }
        ]
      }
    ]
  }
}
```

## 🎯 **Summary**

The environment variable configuration approach provides:

✅ **Flexible Deployment**: Easy environment-specific customization  
✅ **Container Ready**: Docker and Kubernetes friendly  
✅ **Security**: No hardcoded secrets in configuration files  
✅ **Scalability**: Easy horizontal scaling with different configurations  
✅ **Monitoring**: Built-in observability and health checks  
✅ **CI/CD Ready**: Easy integration with deployment pipelines  

This approach eliminates the need for multiple configuration files while providing maximum flexibility for different deployment environments.
