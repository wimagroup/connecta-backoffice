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
public class EstatisticasProtocoloDTO {
    private Long total;
    private Long abertos;
    private Long emAnalise;
    private Long emAndamento;
    private Long finalizados;
    private Long atrasados;
    private Double tempoMedioAtendimento;
    private Map<String, Long> porCategoria;
    private Map<String, Long> porStatus;
}



