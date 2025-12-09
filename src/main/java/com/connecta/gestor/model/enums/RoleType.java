package com.connecta.gestor.model.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    ROLE_SUPER_ADMIN("Super Administrador"),
    ROLE_GESTOR("Gestor"),
    ROLE_ATENDENTE("Atendente"),
    ROLE_FINANCEIRO("Financeiro"),
    ROLE_VISUALIZADOR("Visualizador");

    private final String descricao;

    RoleType(String descricao) {
        this.descricao = descricao;
    }
}

