package com.connecta.gestor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateServicoRequestDTO {
    
    private Long categoriaId;
    
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
    private String titulo;
    
    private String descricao;
    
    @Min(value = 1, message = "Prazo deve ser no mínimo 1 dia")
    private Integer prazoAtendimentoDias;
    
    private Boolean ativo;
    
    private List<ServicoCampoRequestDTO> campos;
}

