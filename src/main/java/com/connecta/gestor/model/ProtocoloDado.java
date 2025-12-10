package com.connecta.gestor.model;

import com.connecta.gestor.model.enums.TipoCampo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "protocolo_dados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProtocoloDado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocolo_id", nullable = false)
    private Protocolo protocolo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoCampo campoTipo;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String valor;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}


