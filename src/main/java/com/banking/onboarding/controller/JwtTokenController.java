package com.banking.onboarding.controller;

import com.banking.onboarding.auth.JwtToken;
import com.banking.onboarding.auth.FenergoTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for JWT token management and testing
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class JwtTokenController {
    
    private final FenergoTokenService fenergoTokenService;
    
    /**
     * Get JWT token for default scope
     */
    @GetMapping("/token")
    public ResponseEntity<JwtToken> getToken() {
        JwtToken token = fenergoTokenService.getToken();
        if (token != null && token.isValid()) {
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.badRequest().build();
    }
    
    /**
     * Get JWT token for specific scope
     */
    @GetMapping("/token/{scope}")
    public ResponseEntity<JwtToken> getToken(@PathVariable String scope) {
        JwtToken token = fenergoTokenService.getToken(scope);
        if (token != null && token.isValid()) {
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.badRequest().build();
    }
    
    /**
     * Refresh token for specific scope
     */
    @PostMapping("/token/{scope}/refresh")
    public ResponseEntity<JwtToken> refreshToken(@PathVariable String scope) {
        JwtToken token = fenergoTokenService.refreshToken(scope);
        if (token != null && token.isValid()) {
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.badRequest().build();
    }
    
    /**
     * Clear token cache
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, String>> clearCache() {
        fenergoTokenService.clearCache();
        return ResponseEntity.ok(Map.of("message", "Token cache cleared"));
    }
    
    /**
     * Clear token cache for specific scope
     */
    @DeleteMapping("/cache/{scope}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String scope) {
        fenergoTokenService.clearCache(scope);
        return ResponseEntity.ok(Map.of("message", "Token cache cleared for scope: " + scope));
    }
    
    /**
     * Test token validity
     */
    @GetMapping("/token/{scope}/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@PathVariable String scope) {
        JwtToken token = fenergoTokenService.getToken(scope);
        Map<String, Object> response = Map.of(
                "valid", token != null && token.isValid(),
                "expired", token != null && token.isExpired(),
                "scope", scope,
                "expiresAt", token != null ? token.getExpiresAt() : null
        );
        return ResponseEntity.ok(response);
    }
}
