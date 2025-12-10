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
public class ProtocoloComentarioDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNome;
    private String comentario;
    private Boolean interno;
    private LocalDateTime createdAt;
}


