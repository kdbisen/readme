package com.banking.onboarding.config;

import com.banking.onboarding.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Startup Configuration
 * Initializes environment-specific configurations on application startup
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StartupConfiguration implements CommandLineRunner {
    
    private final EnvironmentService environmentService;
    
    @Override
    public void run(String... args) throws Exception {
        // Log environment information on startup
        environmentService.logEnvironmentInfo();
        
        // Log environment-specific configurations
        logEnvironmentSpecificInfo();
    }
    
    private void logEnvironmentSpecificInfo() {
        String activeProfile = environmentService.getActiveProfile();
        
        switch (activeProfile) {
            case "dev":
                log.info("🚀 Development Environment - Debug logging enabled");
                log.info("📊 Database: {}", environmentService.getDatabaseConfig().getHost());
                log.info("🔗 Fenergo URL: {}", environmentService.getFenergoConfig().getEntityApiUrl());
                break;
                
            case "test":
                log.info("🧪 Test Environment - Test logging enabled");
                log.info("📊 Database: {}", environmentService.getDatabaseConfig().getHost());
                log.info("🔗 Fenergo URL: {}", environmentService.getFenergoConfig().getEntityApiUrl());
                break;
                
            case "staging":
                log.info("🎭 Staging Environment - Staging logging enabled");
                log.info("📊 Database: {}", environmentService.getDatabaseConfig().getHost());
                log.info("🔗 Fenergo URL: {}", environmentService.getFenergoConfig().getEntityApiUrl());
                break;
                
            case "prod":
                log.info("🏭 Production Environment - Production logging enabled");
                log.info("📊 Database: {}", environmentService.getDatabaseConfig().getHost());
                log.info("🔗 Fenergo URL: {}", environmentService.getFenergoConfig().getEntityApiUrl());
                log.info("🔒 Security: SSL enabled = {}", environmentService.getSecurityConfig().isSslEnabled());
                break;
                
            default:
                log.warn("⚠️ Unknown environment profile: {}", activeProfile);
                break;
        }
    }
}

