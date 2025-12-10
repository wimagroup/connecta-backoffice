package com.connecta.gestor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {
    
    @NotBlank(message = "Refresh token é obrigatório")
    private String refreshToken;
}

