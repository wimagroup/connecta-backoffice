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
public class ProtocoloDadoDTO {
    private Long id;
    private TipoCampo campoTipo;
    private String campoLabel;
    private String valor;
}



