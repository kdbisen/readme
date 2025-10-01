package com.banking.onboarding.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Apigee Token information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApigeeToken {
    
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String scope;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    
    /**
     * Check if token is valid (not expired)
     */
    public boolean isValid() {
        if (expiresAt == null) {
            return true; // No expiration set
        }
        return LocalDateTime.now().isBefore(expiresAt);
    }
    
    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return !isValid();
    }
}
