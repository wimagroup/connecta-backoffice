package com.connecta.gestor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comunicado_destinatarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComunicadoDestinatario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunicado_id", nullable = false)
    private Comunicado comunicado;
    
    @Column(nullable = false, length = 150)
    private String destinatarioNome;
    
    @Column(nullable = false, length = 150)
    private String destinatarioEmail;
    
    @Column(length = 20)
    private String destinatarioTelefone;
    
    @Column(nullable = false)
    private Boolean enviado = false;
    
    @Column
    private LocalDateTime enviadoEm;
    
    @Column(nullable = false)
    private Boolean erro = false;
    
    @Column(columnDefinition = "TEXT")
    private String mensagemErro;
    
    @Column(nullable = false)
    private Integer tentativasEnvio = 0;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}



