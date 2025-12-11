package com.connecta.gestor.model.enums;

import lombok.Getter;

@Getter
public enum CanalComunicacao {
    EMAIL("E-mail", "Envio por e-mail"),
    SMS("SMS", "Envio por SMS"),
    NOTIFICACAO_APP("Notificação App", "Notificação push no aplicativo"),
    TODOS("Todos os Canais", "Envio por todos os canais disponíveis");

    private final String label;
    private final String descricao;

    CanalComunicacao(String label, String descricao) {
        this.label = label;
        this.descricao = descricao;
    }
}



