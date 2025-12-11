package com.connecta.gestor.repository;

import com.connecta.gestor.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByAtivoTrueOrderByOrdemAsc();
    List<Categoria> findAllByOrderByOrdemAsc();
    Boolean existsByNome(String nome);
    Boolean existsByNomeAndIdNot(String nome, Long id);
    Optional<Categoria> findByNome(String nome);
}



