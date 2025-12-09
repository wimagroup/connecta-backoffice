package com.connecta.gestor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    
    private String token;
    private String tipo = "Bearer";
    private String email;
    private String nome;
    private String role;
    
    public LoginResponseDTO(String token, String email, String nome, String role) {
        this.token = token;
        this.tipo = "Bearer";
        this.email = email;
        this.nome = nome;
        this.role = role;
    }
}

