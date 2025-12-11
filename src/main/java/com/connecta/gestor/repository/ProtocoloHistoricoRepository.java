package com.connecta.gestor.repository;

import com.connecta.gestor.model.Protocolo;
import com.connecta.gestor.model.ProtocoloHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocoloHistoricoRepository extends JpaRepository<ProtocoloHistorico, Long> {
    List<ProtocoloHistorico> findByProtocoloOrderByCreatedAtAsc(Protocolo protocolo);
}



