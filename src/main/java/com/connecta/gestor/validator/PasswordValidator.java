package com.connecta.gestor.validator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PasswordValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    
    public ValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();
        
        if (password == null || password.isBlank()) {
            errors.add("A senha não pode estar vazia");
            return new ValidationResult(false, errors);
        }
        
        if (password.length() < MIN_LENGTH) {
            errors.add("A senha deve ter no mínimo " + MIN_LENGTH + " caracteres");
        }
        
        if (password.length() > MAX_LENGTH) {
            errors.add("A senha deve ter no máximo " + MAX_LENGTH + " caracteres");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            errors.add("A senha deve conter pelo menos uma letra maiúscula");
        }
        
        if (!password.matches(".*[a-z].*")) {
            errors.add("A senha deve conter pelo menos uma letra minúscula");
        }
        
        if (!password.matches(".*\\d.*")) {
            errors.add("A senha deve conter pelo menos um número");
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            errors.add("A senha deve conter pelo menos um caractere especial");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}

