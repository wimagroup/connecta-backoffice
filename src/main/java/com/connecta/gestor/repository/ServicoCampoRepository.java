package com.connecta.gestor.repository;

import com.connecta.gestor.model.Servico;
import com.connecta.gestor.model.ServicoCampo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoCampoRepository extends JpaRepository<ServicoCampo, Long> {
    List<ServicoCampo> findByServicoOrderByOrdemAsc(Servico servico);
    void deleteByServico(Servico servico);
}



