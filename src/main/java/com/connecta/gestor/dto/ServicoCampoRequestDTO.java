package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.TipoCampo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ServicoCampoRequestDTO {
    
    @NotNull(message = "Tipo do campo é obrigatório")
    private TipoCampo campoTipo;
    
    @NotNull(message = "Campo obrigatório deve ser informado")
    private Boolean obrigatorio;
    
    private Integer ordem;
    
    @Size(max = 500, message = "Instruções devem ter no máximo 500 caracteres")
    private String instrucoes;
}

