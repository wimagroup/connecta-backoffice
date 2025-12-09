package com.connecta.gestor.model;

import com.connecta.gestor.model.enums.PrioridadeProtocolo;
import com.connecta.gestor.model.enums.StatusProtocolo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "protocolos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Protocolo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String numeroProtocolo;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;
    
    @Column(nullable = false, length = 150)
    private String cidadaoNome;
    
    @Column(length = 150)
    private String cidadaoEmail;
    
    @Column(length = 20)
    private String cidadaoTelefone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusProtocolo status = StatusProtocolo.ABERTO;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "atendente_id")
    private User atendente;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadeProtocolo prioridade = PrioridadeProtocolo.MEDIA;
    
    @Column(nullable = false)
    private LocalDateTime prazoLimite;
    
    @Column(columnDefinition = "TEXT")
    private String descricaoProblema;
    
    @Column
    private LocalDateTime finalizadoEm;
    
    @Column(columnDefinition = "TEXT")
    private String respostaFinal;
    
    @OneToMany(mappedBy = "protocolo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProtocoloDado> dados = new ArrayList<>();
    
    @OneToMany(mappedBy = "protocolo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ProtocoloHistorico> historico = new ArrayList<>();
    
    @OneToMany(mappedBy = "protocolo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ProtocoloComentario> comentarios = new ArrayList<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Transient
    public boolean isAtrasado() {
        return LocalDateTime.now().isAfter(prazoLimite) && 
               !status.equals(StatusProtocolo.FINALIZADO) && 
               !status.equals(StatusProtocolo.CANCELADO);
    }
    
    @Transient
    public long getDiasRestantes() {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), prazoLimite);
    }
}

