package com.connecta.gestor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    
    private Long id;
    private String nome;
    private String email;
    private String role;
    private Boolean ativo;
    private LocalDateTime createdAt;
}

