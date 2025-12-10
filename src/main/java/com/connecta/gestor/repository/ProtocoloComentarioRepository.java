package com.connecta.gestor.repository;

import com.connecta.gestor.model.Protocolo;
import com.connecta.gestor.model.ProtocoloComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocoloComentarioRepository extends JpaRepository<ProtocoloComentario, Long> {
    List<ProtocoloComentario> findByProtocoloOrderByCreatedAtAsc(Protocolo protocolo);
    List<ProtocoloComentario> findByProtocoloAndInternoFalseOrderByCreatedAtAsc(Protocolo protocolo);
}


