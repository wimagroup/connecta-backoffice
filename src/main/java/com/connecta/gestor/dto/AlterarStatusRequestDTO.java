package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.StatusProtocolo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlterarStatusRequestDTO {
    
    @NotNull(message = "Status é obrigatório")
    private StatusProtocolo status;
    
    private String observacao;
}


