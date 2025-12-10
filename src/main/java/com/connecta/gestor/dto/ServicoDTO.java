package com.connecta.gestor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicoDTO {
    private Long id;
    private Long categoriaId;
    private String categoriaNome;
    private String categoriaIcone;
    private String categoriaCor;
    private String titulo;
    private String descricao;
    private Integer prazoAtendimentoDias;
    private Boolean ativo;
    private List<ServicoCampoDTO> campos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


