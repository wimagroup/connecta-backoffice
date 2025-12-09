package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.PrioridadeProtocolo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlterarPrioridadeRequestDTO {
    
    @NotNull(message = "Prioridade é obrigatória")
    private PrioridadeProtocolo prioridade;
}

