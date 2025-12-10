package com.connecta.gestor.model.enums;

import lombok.Getter;

@Getter
public enum StatusComunicado {
    RASCUNHO("Rascunho", "Comunicado em elaboração"),
    AGENDADO("Agendado", "Agendado para envio futuro"),
    ENVIANDO("Enviando", "Em processo de envio"),
    ENVIADO("Enviado", "Enviado com sucesso"),
    ERRO("Erro", "Erro no envio"),
    CANCELADO("Cancelado", "Envio cancelado");

    private final String label;
    private final String descricao;

    StatusComunicado(String label, String descricao) {
        this.label = label;
        this.descricao = descricao;
    }
}


