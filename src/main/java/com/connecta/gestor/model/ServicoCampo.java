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
@Table(name = "servico_campos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicoCampo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoCampo campoTipo;
    
    @Column(nullable = false)
    private Boolean obrigatorio = false;
    
    @Column(nullable = false)
    private Integer ordem = 0;
    
    @Column(length = 500)
    private String instrucoes;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}



