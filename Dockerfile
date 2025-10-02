FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/banking-onboarding-service-1.0.0.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV MONGODB_HOST=localhost
ENV MONGODB_PORT=27017
ENV MONGODB_DATABASE=banking_onboarding
ENV FENERGO_API_URL=http://fenergo-service:8080/api

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/onboarding/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

