package com.connecta.gestor.repository;

import com.connecta.gestor.model.Categoria;
import com.connecta.gestor.model.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long>, JpaSpecificationExecutor<Servico> {
    List<Servico> findByAtivoTrue();
    List<Servico> findByCategoria(Categoria categoria);
    List<Servico> findByCategoriaAndAtivoTrue(Categoria categoria);
    Boolean existsByTitulo(String titulo);
    Boolean existsByTituloAndIdNot(String titulo, Long id);
    
    @Query("SELECT s FROM Servico s WHERE " +
           "(:busca IS NULL OR :busca = '' OR LOWER(s.titulo) LIKE LOWER(CONCAT('%', :busca, '%'))) AND " +
           "(:categorias IS NULL OR s.categoria.id IN :categorias) AND " +
           "(:status IS NULL OR s.ativo IN :status)")
    Page<Servico> findWithFilters(
        @Param("busca") String busca,
        @Param("categorias") List<Long> categorias,
        @Param("status") List<Boolean> status,
        Pageable pageable
    );
}


