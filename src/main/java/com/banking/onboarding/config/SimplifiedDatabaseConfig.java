package com.banking.onboarding.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Simplified MongoDB configuration - indexes will be created automatically by Spring Data MongoDB
 * based on @Indexed annotations in the model classes
 */
@Slf4j
@Configuration
public class SimplifiedDatabaseConfig {
    
    // Indexes are automatically created based on @Indexed annotations in model classes
    // No manual index creation needed
}
