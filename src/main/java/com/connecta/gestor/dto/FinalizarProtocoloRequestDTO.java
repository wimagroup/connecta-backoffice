package com.connecta.gestor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FinalizarProtocoloRequestDTO {
    
    @NotBlank(message = "Resposta final é obrigatória")
    private String respostaFinal;
}

