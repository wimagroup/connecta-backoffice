package com.connecta.gestor.repository;

import com.connecta.gestor.model.Protocolo;
import com.connecta.gestor.model.ProtocoloDado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocoloDadoRepository extends JpaRepository<ProtocoloDado, Long> {
    List<ProtocoloDado> findByProtocolo(Protocolo protocolo);
    void deleteByProtocolo(Protocolo protocolo);
}



