package com.banking.onboarding.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Sort;

@Slf4j
@Configuration
public class DatabaseConfig {
    
    @Bean
    public CommandLineRunner initDatabase(MongoTemplate mongoTemplate) {
        return args -> {
            log.info("Initializing MongoDB database for banking onboarding service");
            
            // Create indexes for better performance
            try {
                mongoTemplate.indexOps("onboarding_processes")
                        .ensureIndex(new Index().on("processId", Sort.Direction.ASC).unique());
                
                mongoTemplate.indexOps("onboarding_processes")
                        .ensureIndex(new Index().on("correlationId", Sort.Direction.ASC).unique());
                
                mongoTemplate.indexOps("onboarding_processes")
                        .ensureIndex(new Index().on("status", Sort.Direction.ASC));
                
                mongoTemplate.indexOps("onboarding_processes")
                        .ensureIndex(new Index().on("createdAt", Sort.Direction.DESC));
                
                mongoTemplate.indexOps("onboarding_processes")
                        .ensureIndex(new Index().on("entityData.entityId", Sort.Direction.ASC));
                
                log.info("MongoDB indexes created successfully");
                
            } catch (Exception e) {
                log.warn("Failed to create MongoDB indexes: {}", e.getMessage());
            }
        };
    }
}
