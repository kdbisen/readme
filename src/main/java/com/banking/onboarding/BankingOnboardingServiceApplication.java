package com.banking.onboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;

@SpringBootApplication
@IntegrationComponentScan
public class BankingOnboardingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingOnboardingServiceApplication.class, args);
    }
}
