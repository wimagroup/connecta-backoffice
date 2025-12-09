package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.CanalComunicacao;
import com.connecta.gestor.model.enums.TipoComunicado;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateComunicadoRequestDTO {
    
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    private String titulo;
    
    private String mensagem;
    
    private TipoComunicado tipo;
    
    private CanalComunicacao canal;
    
    @Size(max = 100, message = "Filtro de bairro deve ter no máximo 100 caracteres")
    private String filtroBairro;
    
    @Size(max = 100, message = "Filtro de categoria deve ter no máximo 100 caracteres")
    private String filtroCategoria;
    
    private LocalDateTime agendadoPara;
}

