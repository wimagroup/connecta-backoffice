package com.connecta.gestor.repository;

import com.connecta.gestor.model.Categoria;
import com.connecta.gestor.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByAtivoTrue();
    List<Servico> findByCategoria(Categoria categoria);
    List<Servico> findByCategoriaAndAtivoTrue(Categoria categoria);
    Boolean existsByTitulo(String titulo);
    Boolean existsByTituloAndIdNot(String titulo, Long id);
}

