package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.TipoAcaoProtocolo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProtocoloHistoricoDTO {
    private Long id;
    private String usuarioNome;
    private TipoAcaoProtocolo acao;
    private String acaoLabel;
    private String descricao;
    private String statusAnterior;
    private String statusNovo;
    private LocalDateTime createdAt;
}

