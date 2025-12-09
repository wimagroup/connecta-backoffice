package com.connecta.gestor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComunicadoDestinatarioDTO {
    private Long id;
    private String destinatarioNome;
    private String destinatarioEmail;
    private String destinatarioTelefone;
    private Boolean enviado;
    private LocalDateTime enviadoEm;
    private Boolean erro;
    private String mensagemErro;
    private Integer tentativasEnvio;
}

