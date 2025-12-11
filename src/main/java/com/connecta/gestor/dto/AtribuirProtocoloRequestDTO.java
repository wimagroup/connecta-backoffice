package com.connecta.gestor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtribuirProtocoloRequestDTO {
    
    @NotNull(message = "ID do atendente é obrigatório")
    private Long atendenteId;
}



