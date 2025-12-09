package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.CanalComunicacao;
import com.connecta.gestor.model.enums.StatusComunicado;
import com.connecta.gestor.model.enums.TipoComunicado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComunicadoDetalheDTO {
    private Long id;
    private String titulo;
    private String mensagem;
    private TipoComunicado tipo;
    private String tipoLabel;
    private StatusComunicado status;
    private String statusLabel;
    private CanalComunicacao canal;
    private String canalLabel;
    private Long criadoPorId;
    private String criadoPorNome;
    private String filtroBairro;
    private String filtroCategoria;
    private LocalDateTime agendadoPara;
    private LocalDateTime enviadoEm;
    private Integer totalDestinatarios;
    private Integer totalEnviados;
    private Integer totalErros;
    private String mensagemErro;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean podeEditar;
    private Boolean podeCancelar;
}

