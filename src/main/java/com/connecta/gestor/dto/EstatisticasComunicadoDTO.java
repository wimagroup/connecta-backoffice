package com.connecta.gestor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstatisticasComunicadoDTO {
    private Long totalComunicados;
    private Long rascunhos;
    private Long agendados;
    private Long enviados;
    private Long comErro;
    private Long totalDestinatariosGeral;
    private Long totalEnviadosGeral;
    private Long totalErrosGeral;
    private Double taxaSucesso;
    private Map<String, Long> porTipo;
    private Map<String, Long> porCanal;
}


