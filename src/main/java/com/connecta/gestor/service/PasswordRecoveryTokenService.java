package com.connecta.gestor.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordRecoveryTokenService {
    
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;
    
    public String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        return hashToken(rawToken);
    }
    
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash do token", e);
        }
    }
    
    public boolean validateTokenFormat(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        
        if (token.length() < 40 || token.length() > 50) {
            return false;
        }
        
        return token.matches("^[A-Za-z0-9_-]+$");
    }
}

