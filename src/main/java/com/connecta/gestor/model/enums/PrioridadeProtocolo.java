package com.connecta.gestor.model.enums;

import lombok.Getter;

@Getter
public enum PrioridadeProtocolo {
    BAIXA("Baixa", "#4CAF50"),
    MEDIA("Média", "#FF9800"),
    ALTA("Alta", "#FF5722"),
    URGENTE("Urgente", "#F44336");

    private final String label;
    private final String cor;

    PrioridadeProtocolo(String label, String cor) {
        this.label = label;
        this.cor = cor;
    }
}

