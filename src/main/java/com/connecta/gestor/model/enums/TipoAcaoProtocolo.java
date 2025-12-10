package com.connecta.gestor.model.enums;

import lombok.Getter;

@Getter
public enum TipoAcaoProtocolo {
    CRIADO("Criado", "Protocolo foi criado"),
    ATRIBUIDO("Atribuído", "Protocolo foi atribuído a um atendente"),
    STATUS_ALTERADO("Status Alterado", "Status do protocolo foi alterado"),
    PRIORIDADE_ALTERADA("Prioridade Alterada", "Prioridade foi alterada"),
    COMENTARIO_ADICIONADO("Comentário Adicionado", "Novo comentário foi adicionado"),
    INFORMACAO_SOLICITADA("Informação Solicitada", "Solicitação de informação complementar"),
    FINALIZADO("Finalizado", "Protocolo foi finalizado");

    private final String label;
    private final String descricao;

    TipoAcaoProtocolo(String label, String descricao) {
        this.label = label;
        this.descricao = descricao;
    }
}


