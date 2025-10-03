#!/bin/bash

# Banking Onboarding Service - Quick Start Script

echo "🚀 Starting Banking Onboarding Service..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven."
    exit 1
fi

# Build the application
echo "📦 Building the application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

# Start MongoDB (if not running)
echo "🗄️ Starting MongoDB..."
if ! pgrep -x "mongod" > /dev/null; then
    echo "Starting MongoDB..."
    mongod --fork --logpath /tmp/mongodb.log
    sleep 5
fi

# Start the application
echo "🎯 Starting Banking Onboarding Service..."
java -jar target/banking-onboarding-service-1.0.0.jar --spring.profiles.active=dev

echo "✅ Banking Onboarding Service is running on http://localhost:8080"
echo "📋 API Documentation:"
echo "   POST /api/v1/onboarding/process-entity"
echo "   GET  /api/v1/onboarding/status/{processId}"
echo "   GET  /api/v1/onboarding/health"





