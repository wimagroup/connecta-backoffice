package com.connecta.gestor.repository;

import com.connecta.gestor.model.Comunicado;
import com.connecta.gestor.model.ComunicadoDestinatario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComunicadoDestinatarioRepository extends JpaRepository<ComunicadoDestinatario, Long> {
    List<ComunicadoDestinatario> findByComunicado(Comunicado comunicado);
    List<ComunicadoDestinatario> findByComunicadoAndEnviadoFalse(Comunicado comunicado);
    Long countByComunicadoAndEnviadoTrue(Comunicado comunicado);
    Long countByComunicadoAndErroTrue(Comunicado comunicado);
}


