package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.CanalComunicacao;
import com.connecta.gestor.model.enums.TipoComunicado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateComunicadoRequestDTO {
    
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    private String titulo;
    
    @NotBlank(message = "Mensagem é obrigatória")
    private String mensagem;
    
    @NotNull(message = "Tipo é obrigatório")
    private TipoComunicado tipo;
    
    @NotNull(message = "Canal é obrigatório")
    private CanalComunicacao canal;
    
    @Size(max = 100, message = "Filtro de bairro deve ter no máximo 100 caracteres")
    private String filtroBairro;
    
    @Size(max = 100, message = "Filtro de categoria deve ter no máximo 100 caracteres")
    private String filtroCategoria;
    
    private LocalDateTime agendadoPara;
    
    private Boolean salvarRascunho;
}



