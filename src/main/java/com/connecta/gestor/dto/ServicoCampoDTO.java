package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.TipoCampo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicoCampoDTO {
    private Long id;
    private TipoCampo campoTipo;
    private String campoLabel;
    private String campoDescricao;
    private Boolean obrigatorio;
    private Integer ordem;
    private String instrucoes;
}

