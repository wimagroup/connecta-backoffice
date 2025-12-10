package com.connecta.gestor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateServicoRequestDTO {
    
    @NotNull(message = "Categoria é obrigatória")
    private Long categoriaId;
    
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
    private String titulo;
    
    private String descricao;
    
    @NotNull(message = "Prazo de atendimento é obrigatório")
    @Min(value = 1, message = "Prazo deve ser no mínimo 1 dia")
    private Integer prazoAtendimentoDias;
    
    private List<ServicoCampoRequestDTO> campos;
}


