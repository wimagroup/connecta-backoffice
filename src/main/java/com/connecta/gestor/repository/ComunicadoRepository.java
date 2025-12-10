package com.connecta.gestor.repository;

import com.connecta.gestor.model.Comunicado;
import com.connecta.gestor.model.User;
import com.connecta.gestor.model.enums.StatusComunicado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComunicadoRepository extends JpaRepository<Comunicado, Long> {
    List<Comunicado> findByStatus(StatusComunicado status);
    List<Comunicado> findByCriadoPor(User usuario);
    List<Comunicado> findByStatusAndAgendadoParaBefore(StatusComunicado status, LocalDateTime dataLimite);
    
    @Query("SELECT COUNT(c) FROM Comunicado c WHERE c.status = :status")
    Long countByStatus(@Param("status") StatusComunicado status);
    
    @Query("SELECT c FROM Comunicado c ORDER BY c.createdAt DESC")
    List<Comunicado> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT SUM(c.totalDestinatarios) FROM Comunicado c")
    Long sumTotalDestinatarios();
    
    @Query("SELECT SUM(c.totalEnviados) FROM Comunicado c")
    Long sumTotalEnviados();
    
    @Query("SELECT SUM(c.totalErros) FROM Comunicado c")
    Long sumTotalErros();
}


