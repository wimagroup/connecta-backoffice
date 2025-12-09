package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.PrioridadeProtocolo;
import com.connecta.gestor.model.enums.StatusProtocolo;
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
public class ProtocoloDetalheDTO {
    private Long id;
    private String numeroProtocolo;
    private Long servicoId;
    private String servicoTitulo;
    private String categoriaNome;
    private String cidadaoNome;
    private String cidadaoEmail;
    private String cidadaoTelefone;
    private StatusProtocolo status;
    private String statusLabel;
    private PrioridadeProtocolo prioridade;
    private String prioridadeLabel;
    private Long atendenteId;
    private String atendenteNome;
    private String descricaoProblema;
    private LocalDateTime prazoLimite;
    private Long diasRestantes;
    private Boolean atrasado;
    private LocalDateTime finalizadoEm;
    private String respostaFinal;
    private List<ProtocoloDadoDTO> dados;
    private List<ProtocoloHistoricoDTO> historico;
    private List<ProtocoloComentarioDTO> comentarios;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

