package com.connecta.gestor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdicionarComentarioRequestDTO {
    
    @NotBlank(message = "Comentário é obrigatório")
    private String comentario;
    
    @NotNull(message = "Campo 'interno' é obrigatório")
    private Boolean interno;
}


