package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.PrioridadeProtocolo;
import com.connecta.gestor.model.enums.StatusProtocolo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProtocoloResumoDTO {
    private Long id;
    private String numeroProtocolo;
    private String servicoTitulo;
    private String categoriaNome;
    private String cidadaoNome;
    private StatusProtocolo status;
    private String statusLabel;
    private PrioridadeProtocolo prioridade;
    private String prioridadeLabel;
    private String prioridadeCor;
    private String atendenteNome;
    private LocalDateTime prazoLimite;
    private Long diasRestantes;
    private Boolean atrasado;
    private LocalDateTime createdAt;
}


