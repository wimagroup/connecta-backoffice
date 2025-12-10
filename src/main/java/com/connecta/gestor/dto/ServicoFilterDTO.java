package com.connecta.gestor.dto;

import lombok.Data;

import java.util.List;

@Data
public class ServicoFilterDTO {
    private String busca;
    private List<Long> categorias;
    private List<Boolean> status;
    private Integer page = 0;
    private Integer size = 10;
    private String sort = "titulo";
    private String direction = "asc";
}

