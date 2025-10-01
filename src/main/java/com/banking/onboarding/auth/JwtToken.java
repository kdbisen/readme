package com.banking.onboarding.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simple JWT Token model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken {
    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
    private String scope;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    
    public LocalDateTime getExpiresAt() {
        if (expiresAt != null) {
            return expiresAt;
        }
        if (issuedAt == null || expiresIn == null) {
            return null;
        }
        return issuedAt.plusSeconds(expiresIn);
    }
    
    public boolean isValid() {
        if (accessToken == null || accessToken.isEmpty() || getExpiresAt() == null) {
            return false;
        }
        return LocalDateTime.now().plusSeconds(60).isBefore(getExpiresAt());
    }
}
