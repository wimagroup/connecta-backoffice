package com.connecta.gestor.model.enums;

import lombok.Getter;

@Getter
public enum StatusProtocolo {
    ABERTO("Aberto", "Protocolo recém criado, aguardando análise"),
    EM_ANALISE("Em Análise", "Protocolo sendo analisado pela equipe"),
    EM_ANDAMENTO("Em Andamento", "Protocolo em processo de atendimento"),
    AGUARDANDO_INFORMACOES("Aguardando Informações", "Aguardando informações complementares do solicitante"),
    APROVADO("Aprovado", "Protocolo aprovado, será executado"),
    REJEITADO("Rejeitado", "Protocolo rejeitado"),
    FINALIZADO("Finalizado", "Protocolo finalizado com sucesso"),
    CANCELADO("Cancelado", "Protocolo cancelado pelo solicitante ou administração");

    private final String label;
    private final String descricao;

    StatusProtocolo(String label, String descricao) {
        this.label = label;
        this.descricao = descricao;
    }
}


