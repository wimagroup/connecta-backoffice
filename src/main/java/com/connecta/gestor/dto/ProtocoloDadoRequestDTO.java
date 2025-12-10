package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.TipoCampo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProtocoloDadoRequestDTO {
    
    @NotNull(message = "Tipo do campo é obrigatório")
    private TipoCampo campoTipo;
    
    @NotBlank(message = "Valor é obrigatório")
    private String valor;
}


