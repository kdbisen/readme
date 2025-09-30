package com.banking.onboarding.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.time.Instant;
import java.util.Map;

/**
 * Custom MongoDB appender for error logging
 */
@Slf4j
public class MongoDbAppender extends AppenderBase<ILoggingEvent> {
    
    private String connectionString;
    private String database;
    private String collection;
    
    private MongoClient mongoClient;
    private MongoCollection<Document> mongoCollection;
    
    @Override
    public void start() {
        try {
            if (connectionString == null || database == null || collection == null) {
                addError("MongoDB connection parameters not set");
                return;
            }
            
            mongoClient = MongoClients.create(connectionString);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
            mongoCollection = mongoDatabase.getCollection(collection);
            
            super.start();
            addInfo("MongoDB appender started successfully");
            
        } catch (Exception e) {
            addError("Failed to start MongoDB appender", e);
        }
    }
    
    @Override
    public void stop() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
            }
            super.stop();
            addInfo("MongoDB appender stopped");
        } catch (Exception e) {
            addError("Error stopping MongoDB appender", e);
        }
    }
    
    @Override
    protected void append(ILoggingEvent event) {
        try {
            Document logDocument = createLogDocument(event);
            mongoCollection.insertOne(logDocument);
        } catch (Exception e) {
            addError("Failed to write log to MongoDB", e);
        }
    }
    
    private Document createLogDocument(ILoggingEvent event) {
        Document doc = new Document();
        
        // Basic log information
        doc.append("timestamp", Instant.ofEpochMilli(event.getTimeStamp()));
        doc.append("level", event.getLevel().toString());
        doc.append("logger", event.getLoggerName());
        doc.append("message", event.getFormattedMessage());
        doc.append("thread", event.getThreadName());
        
        // Correlation ID from MDC
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc.containsKey("correlationId")) {
            doc.append("correlationId", mdc.get("correlationId"));
        }
        if (mdc.containsKey("traceId")) {
            doc.append("traceId", mdc.get("traceId"));
        }
        if (mdc.containsKey("processId")) {
            doc.append("processId", mdc.get("processId"));
        }
        
        // Exception information
        if (event.getThrowableProxy() != null) {
            doc.append("exception", event.getThrowableProxy().getClassName());
            doc.append("exceptionMessage", event.getThrowableProxy().getMessage());
            doc.append("stackTrace", event.getThrowableProxy().getStackTraceElementProxyArray());
        }
        
        // Additional metadata
        doc.append("service", "banking-onboarding-service");
        doc.append("environment", System.getProperty("spring.profiles.active", "dev"));
        doc.append("version", System.getProperty("app.version", "1.0.0"));
        
        return doc;
    }
    
    // Getters and setters
    public String getConnectionString() {
        return connectionString;
    }
    
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getCollection() {
        return collection;
    }
    
    public void setCollection(String collection) {
        this.collection = collection;
    }
}
