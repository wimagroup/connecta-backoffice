package com.connecta.gestor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {
    
    @NotBlank(message = "Senha atual é obrigatória")
    private String senhaAtual;
    
    @NotBlank(message = "Nova senha é obrigatória")
    private String novaSenha;
}

