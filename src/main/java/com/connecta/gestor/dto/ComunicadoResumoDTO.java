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
public class ComunicadoResumoDTO {
    private Long id;
    private String titulo;
    private TipoComunicado tipo;
    private String tipoLabel;
    private StatusComunicado status;
    private String statusLabel;
    private CanalComunicacao canal;
    private String canalLabel;
    private String criadoPorNome;
    private Integer totalDestinatarios;
    private Integer totalEnviados;
    private Integer totalErros;
    private LocalDateTime agendadoPara;
    private LocalDateTime enviadoEm;
    private LocalDateTime createdAt;
    private Boolean podeEditar;
    private Boolean podeCancelar;
}



