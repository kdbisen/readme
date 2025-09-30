package com.banking.onboarding.auth;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * JWT Token information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken {
    
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String scope;
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt.minusMinutes(5)); // 5 min buffer
    }
    
    public boolean isValid() {
        return accessToken != null && !accessToken.isEmpty() && !isExpired();
    }
}
