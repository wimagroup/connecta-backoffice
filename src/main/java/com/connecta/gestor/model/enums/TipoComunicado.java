package com.connecta.gestor.model.enums;

import lombok.Getter;

@Getter
public enum TipoComunicado {
    GERAL("Geral", "Comunicado geral para todos os cidadãos"),
    INFORMATIVO("Informativo", "Informação importante"),
    ALERTA("Alerta", "Alerta ou aviso urgente"),
    MANUTENCAO("Manutenção", "Comunicado sobre manutenção de serviços"),
    EVENTO("Evento", "Divulgação de evento"),
    URGENTE("Urgente", "Comunicado urgente");

    private final String label;
    private final String descricao;

    TipoComunicado(String label, String descricao) {
        this.label = label;
        this.descricao = descricao;
    }
}

