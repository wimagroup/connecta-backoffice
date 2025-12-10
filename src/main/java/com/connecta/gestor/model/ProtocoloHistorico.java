package com.connecta.gestor.model;

import com.connecta.gestor.model.enums.TipoAcaoProtocolo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "protocolo_historico")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProtocoloHistorico {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocolo_id", nullable = false)
    private Protocolo protocolo;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User usuario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoAcaoProtocolo acao;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;
    
    @Column(length = 50)
    private String statusAnterior;
    
    @Column(length = 50)
    private String statusNovo;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}


