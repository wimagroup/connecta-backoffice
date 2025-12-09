package com.connecta.gestor.service;

import com.connecta.gestor.dto.CategoriaDTO;
import com.connecta.gestor.dto.CreateCategoriaRequestDTO;
import com.connecta.gestor.dto.UpdateCategoriaRequestDTO;
import com.connecta.gestor.exception.ResourceNotFoundException;
import com.connecta.gestor.model.Categoria;
import com.connecta.gestor.repository.CategoriaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoriaService {
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    public List<CategoriaDTO> listarTodas() {
        return categoriaRepository.findAllByOrderByOrdemAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<CategoriaDTO> listarAtivas() {
        return categoriaRepository.findByAtivoTrueOrderByOrdemAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public CategoriaDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        return toDTO(categoria);
    }
    
    @Transactional
    public CategoriaDTO criar(CreateCategoriaRequestDTO request) {
        if (categoriaRepository.existsByNome(request.getNome())) {
            throw new IllegalArgumentException("Já existe uma categoria com este nome");
        }
        
        Categoria categoria = Categoria.builder()
                .nome(request.getNome())
                .icone(request.getIcone())
                .cor(request.getCor() != null ? request.getCor() : "#4CAF50")
                .ordem(request.getOrdem() != null ? request.getOrdem() : 0)
                .ativo(true)
                .build();
        
        categoria = categoriaRepository.save(categoria);
        log.info("Categoria criada: {}", categoria.getNome());
        
        return toDTO(categoria);
    }
    
    @Transactional
    public CategoriaDTO atualizar(Long id, UpdateCategoriaRequestDTO request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        
        if (request.getNome() != null && !request.getNome().equals(categoria.getNome())) {
            if (categoriaRepository.existsByNomeAndIdNot(request.getNome(), id)) {
                throw new IllegalArgumentException("Já existe uma categoria com este nome");
            }
            categoria.setNome(request.getNome());
        }
        
        if (request.getIcone() != null) categoria.setIcone(request.getIcone());
        if (request.getCor() != null) categoria.setCor(request.getCor());
        if (request.getOrdem() != null) categoria.setOrdem(request.getOrdem());
        if (request.getAtivo() != null) categoria.setAtivo(request.getAtivo());
        
        categoria = categoriaRepository.save(categoria);
        log.info("Categoria atualizada: {}", categoria.getNome());
        
        return toDTO(categoria);
    }
    
    @Transactional
    public void deletar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        
        categoriaRepository.delete(categoria);
        log.info("Categoria deletada: {}", categoria.getNome());
    }
    
    @Transactional
    public CategoriaDTO toggleStatus(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        
        categoria.setAtivo(!categoria.getAtivo());
        categoria = categoriaRepository.save(categoria);
        log.info("Status da categoria alterado: {} - Ativo: {}", categoria.getNome(), categoria.getAtivo());
        
        return toDTO(categoria);
    }
    
    private CategoriaDTO toDTO(Categoria categoria) {
        return CategoriaDTO.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .icone(categoria.getIcone())
                .cor(categoria.getCor())
                .ordem(categoria.getOrdem())
                .ativo(categoria.getAtivo())
                .build();
    }
}

