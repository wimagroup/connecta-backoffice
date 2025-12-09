package com.connecta.gestor.model;

import com.connecta.gestor.model.enums.CanalComunicacao;
import com.connecta.gestor.model.enums.StatusComunicado;
import com.connecta.gestor.model.enums.TipoComunicado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comunicados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comunicado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private User criadoPor;
    
    @Column(nullable = false, length = 200)
    private String titulo;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoComunicado tipo = TipoComunicado.GERAL;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusComunicado status = StatusComunicado.RASCUNHO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CanalComunicacao canal = CanalComunicacao.EMAIL;
    
    @Column(length = 100)
    private String filtroBairro;
    
    @Column(length = 100)
    private String filtroCategoria;
    
    @Column
    private LocalDateTime agendadoPara;
    
    @Column
    private LocalDateTime enviadoEm;
    
    @Column(nullable = false)
    private Integer totalDestinatarios = 0;
    
    @Column(nullable = false)
    private Integer totalEnviados = 0;
    
    @Column(nullable = false)
    private Integer totalErros = 0;
    
    @Column(columnDefinition = "TEXT")
    private String mensagemErro;
    
    @OneToMany(mappedBy = "comunicado", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ComunicadoDestinatario> destinatarios = new ArrayList<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Transient
    public boolean isPodeEditar() {
        return status.equals(StatusComunicado.RASCUNHO) || status.equals(StatusComunicado.AGENDADO);
    }
    
    @Transient
    public boolean isPodeCancelar() {
        return status.equals(StatusComunicado.AGENDADO) || status.equals(StatusComunicado.ENVIANDO);
    }
}

