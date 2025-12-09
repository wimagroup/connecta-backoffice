package com.connecta.gestor.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoriaRequestDTO {
    
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;
    
    @Size(max = 50, message = "Ícone deve ter no máximo 50 caracteres")
    private String icone;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato hexadecimal (#RRGGBB)")
    private String cor;
    
    private Integer ordem;
    
    private Boolean ativo;
}

