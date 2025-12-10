package com.connecta.gestor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    
    private String accessToken;
    private String refreshToken;
    private String tipo = "Bearer";
    private Long expiresIn;
    private String email;
    private String nome;
    private String role;
}

