package com.connecta.gestor.repository;

import com.connecta.gestor.model.Protocolo;
import com.connecta.gestor.model.Servico;
import com.connecta.gestor.model.User;
import com.connecta.gestor.model.enums.PrioridadeProtocolo;
import com.connecta.gestor.model.enums.StatusProtocolo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProtocoloRepository extends JpaRepository<Protocolo, Long> {
    Optional<Protocolo> findByNumeroProtocolo(String numeroProtocolo);
    List<Protocolo> findByStatus(StatusProtocolo status);
    List<Protocolo> findByAtendente(User atendente);
    List<Protocolo> findByAtendenteAndStatus(User atendente, StatusProtocolo status);
    List<Protocolo> findByServico(Servico servico);
    List<Protocolo> findByPrioridade(PrioridadeProtocolo prioridade);
    
    @Query("SELECT p FROM Protocolo p WHERE p.prazoLimite < :dataAtual AND p.status NOT IN :statusExcluidos")
    List<Protocolo> findAtrasados(
        @Param("dataAtual") LocalDateTime dataAtual,
        @Param("statusExcluidos") List<StatusProtocolo> statusExcluidos
    );
    
    @Query("SELECT COUNT(p) FROM Protocolo p WHERE p.status = :status")
    Long countByStatus(@Param("status") StatusProtocolo status);
    
    @Query("SELECT p FROM Protocolo p WHERE YEAR(p.createdAt) = :ano ORDER BY p.id DESC")
    List<Protocolo> findByAno(@Param("ano") int ano);
    
    @Query("SELECT MAX(p.id) FROM Protocolo p WHERE YEAR(p.createdAt) = :ano")
    Long findMaxIdByAno(@Param("ano") int ano);
}

