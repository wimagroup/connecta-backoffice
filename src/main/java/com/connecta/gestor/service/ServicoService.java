package com.connecta.gestor.service;

import com.connecta.gestor.dto.*;
import com.connecta.gestor.exception.ResourceNotFoundException;
import com.connecta.gestor.model.Categoria;
import com.connecta.gestor.model.Servico;
import com.connecta.gestor.model.ServicoCampo;
import com.connecta.gestor.repository.CategoriaRepository;
import com.connecta.gestor.repository.ServicoCampoRepository;
import com.connecta.gestor.repository.ServicoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ServicoService {
    
    @Autowired
    private ServicoRepository servicoRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private ServicoCampoRepository servicoCampoRepository;
    
    public Page<ServicoDTO> listarComFiltros(ServicoFilterDTO filtros) {
        Sort sort = filtros.getDirection().equalsIgnoreCase("desc") 
            ? Sort.by(filtros.getSort()).descending() 
            : Sort.by(filtros.getSort()).ascending();
        
        Pageable pageable = PageRequest.of(filtros.getPage(), filtros.getSize(), sort);
        
        Page<Servico> servicosPage = servicoRepository.findWithFilters(
            filtros.getBusca(),
            (filtros.getCategorias() != null && !filtros.getCategorias().isEmpty()) ? filtros.getCategorias() : null,
            (filtros.getStatus() != null && !filtros.getStatus().isEmpty()) ? filtros.getStatus() : null,
            pageable
        );
        
        return servicosPage.map(this::toDTO);
    }
    
    public List<ServicoDTO> listarTodos() {
        return servicoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ServicoDTO> listarAtivos() {
        return servicoRepository.findByAtivoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ServicoDTO> listarPorCategoria(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        
        return servicoRepository.findByCategoria(categoria)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public ServicoDTO buscarPorId(Long id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
        return toDTO(servico);
    }
    
    @Transactional
    public ServicoDTO criar(CreateServicoRequestDTO request) {
        if (servicoRepository.existsByTitulo(request.getTitulo())) {
            throw new IllegalArgumentException("Já existe um serviço com este título");
        }
        
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        
        Servico servico = Servico.builder()
                .categoria(categoria)
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .prazoAtendimentoDias(request.getPrazoAtendimentoDias())
                .ativo(true)
                .build();
        
        servico = servicoRepository.save(servico);
        
        if (request.getCampos() != null && !request.getCampos().isEmpty()) {
            for (ServicoCampoRequestDTO campoDTO : request.getCampos()) {
                ServicoCampo campo = ServicoCampo.builder()
                        .servico(servico)
                        .campoTipo(campoDTO.getCampoTipo())
                        .obrigatorio(campoDTO.getObrigatorio())
                        .ordem(campoDTO.getOrdem() != null ? campoDTO.getOrdem() : 0)
                        .instrucoes(campoDTO.getInstrucoes())
                        .build();
                servicoCampoRepository.save(campo);
            }
        }
        
        log.info("Serviço criado: {}", servico.getTitulo());
        return buscarPorId(servico.getId());
    }
    
    @Transactional
    public ServicoDTO atualizar(Long id, UpdateServicoRequestDTO request) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
        
        if (request.getTitulo() != null && !request.getTitulo().equals(servico.getTitulo())) {
            if (servicoRepository.existsByTituloAndIdNot(request.getTitulo(), id)) {
                throw new IllegalArgumentException("Já existe um serviço com este título");
            }
            servico.setTitulo(request.getTitulo());
        }
        
        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
            servico.setCategoria(categoria);
        }
        
        if (request.getDescricao() != null) servico.setDescricao(request.getDescricao());
        if (request.getPrazoAtendimentoDias() != null) servico.setPrazoAtendimentoDias(request.getPrazoAtendimentoDias());
        if (request.getAtivo() != null) servico.setAtivo(request.getAtivo());
        
        servico = servicoRepository.save(servico);
        
        if (request.getCampos() != null) {
            servicoCampoRepository.deleteByServico(servico);
            
            for (ServicoCampoRequestDTO campoDTO : request.getCampos()) {
                ServicoCampo campo = ServicoCampo.builder()
                        .servico(servico)
                        .campoTipo(campoDTO.getCampoTipo())
                        .obrigatorio(campoDTO.getObrigatorio())
                        .ordem(campoDTO.getOrdem() != null ? campoDTO.getOrdem() : 0)
                        .instrucoes(campoDTO.getInstrucoes())
                        .build();
                servicoCampoRepository.save(campo);
            }
        }
        
        log.info("Serviço atualizado: {}", servico.getTitulo());
        return buscarPorId(servico.getId());
    }
    
    @Transactional
    public void deletar(Long id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
        
        servicoRepository.delete(servico);
        log.info("Serviço deletado: {}", servico.getTitulo());
    }
    
    @Transactional
    public ServicoDTO toggleStatus(Long id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
        
        servico.setAtivo(!servico.getAtivo());
        servico = servicoRepository.save(servico);
        log.info("Status do serviço alterado: {} - Ativo: {}", servico.getTitulo(), servico.getAtivo());
        
        return toDTO(servico);
    }
    
    private ServicoDTO toDTO(Servico servico) {
        List<ServicoCampoDTO> camposDTO = servicoCampoRepository.findByServicoOrderByOrdemAsc(servico)
                .stream()
                .map(campo -> ServicoCampoDTO.builder()
                        .id(campo.getId())
                        .campoTipo(campo.getCampoTipo())
                        .campoLabel(campo.getCampoTipo().getLabel())
                        .campoDescricao(campo.getCampoTipo().getDescricao())
                        .obrigatorio(campo.getObrigatorio())
                        .ordem(campo.getOrdem())
                        .instrucoes(campo.getInstrucoes())
                        .build())
                .collect(Collectors.toList());
        
        return ServicoDTO.builder()
                .id(servico.getId())
                .categoriaId(servico.getCategoria().getId())
                .categoriaNome(servico.getCategoria().getNome())
                .categoriaIcone(servico.getCategoria().getIcone())
                .categoriaCor(servico.getCategoria().getCor())
                .titulo(servico.getTitulo())
                .descricao(servico.getDescricao())
                .prazoAtendimentoDias(servico.getPrazoAtendimentoDias())
                .ativo(servico.getAtivo())
                .campos(camposDTO)
                .createdAt(servico.getCreatedAt())
                .updatedAt(servico.getUpdatedAt())
                .build();
    }
}


