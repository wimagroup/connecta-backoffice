package com.connecta.gestor.service;

import com.connecta.gestor.dto.*;
import com.connecta.gestor.exception.ResourceNotFoundException;
import com.connecta.gestor.model.Comunicado;
import com.connecta.gestor.model.ComunicadoDestinatario;
import com.connecta.gestor.model.User;
import com.connecta.gestor.model.enums.CanalComunicacao;
import com.connecta.gestor.model.enums.StatusComunicado;
import com.connecta.gestor.repository.ComunicadoDestinatarioRepository;
import com.connecta.gestor.repository.ComunicadoRepository;
import com.connecta.gestor.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ComunicadoService {
    
    @Autowired
    private ComunicadoRepository comunicadoRepository;
    
    @Autowired
    private ComunicadoDestinatarioRepository comunicadoDestinatarioRepository;
    
    @Autowired
    private EmailService emailService;
    
    public List<ComunicadoResumoDTO> listarTodos() {
        return comunicadoRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    public List<ComunicadoResumoDTO> listarPorStatus(StatusComunicado status) {
        return comunicadoRepository.findByStatus(status)
                .stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    public List<ComunicadoResumoDTO> listarPorUsuario(Long usuarioId, UserRepository userRepository) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        return comunicadoRepository.findByCriadoPor(usuario)
                .stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    public ComunicadoDetalheDTO buscarPorId(Long id) {
        Comunicado comunicado = comunicadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunicado não encontrado"));
        return toDetalheDTO(comunicado);
    }
    
    public List<ComunicadoDestinatarioDTO> buscarDestinatarios(Long comunicadoId) {
        Comunicado comunicado = comunicadoRepository.findById(comunicadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Comunicado não encontrado"));
        
        return comunicadoDestinatarioRepository.findByComunicado(comunicado)
                .stream()
                .map(this::toDestinatarioDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ComunicadoDetalheDTO criar(CreateComunicadoRequestDTO request, User usuarioLogado) {
        Comunicado comunicado = Comunicado.builder()
                .criadoPor(usuarioLogado)
                .titulo(request.getTitulo())
                .mensagem(request.getMensagem())
                .tipo(request.getTipo())
                .canal(request.getCanal())
                .filtroBairro(request.getFiltroBairro())
                .filtroCategoria(request.getFiltroCategoria())
                .agendadoPara(request.getAgendadoPara())
                .status(StatusComunicado.RASCUNHO)
                .totalDestinatarios(0)
                .totalEnviados(0)
                .totalErros(0)
                .build();
        
        if (request.getSalvarRascunho() == null || !request.getSalvarRascunho()) {
            if (request.getAgendadoPara() != null && request.getAgendadoPara().isAfter(LocalDateTime.now())) {
                comunicado.setStatus(StatusComunicado.AGENDADO);
            }
        }
        
        comunicado = comunicadoRepository.save(comunicado);
        
        criarDestinatariosMock(comunicado);
        
        log.info("Comunicado criado: {} - Status: {}", comunicado.getTitulo(), comunicado.getStatus());
        
        if (comunicado.getStatus().equals(StatusComunicado.RASCUNHO) == false && 
            comunicado.getStatus().equals(StatusComunicado.AGENDADO) == false) {
            enviarComunicado(comunicado.getId());
        }
        
        return buscarPorId(comunicado.getId());
    }
    
    @Transactional
    public ComunicadoDetalheDTO atualizar(Long id, UpdateComunicadoRequestDTO request) {
        Comunicado comunicado = comunicadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunicado não encontrado"));
        
        if (!comunicado.isPodeEditar()) {
            throw new IllegalStateException("Este comunicado não pode mais ser editado");
        }
        
        if (request.getTitulo() != null) comunicado.setTitulo(request.getTitulo());
        if (request.getMensagem() != null) comunicado.setMensagem(request.getMensagem());
        if (request.getTipo() != null) comunicado.setTipo(request.getTipo());
        if (request.getCanal() != null) comunicado.setCanal(request.getCanal());
        if (request.getFiltroBairro() != null) comunicado.setFiltroBairro(request.getFiltroBairro());
        if (request.getFiltroCategoria() != null) comunicado.setFiltroCategoria(request.getFiltroCategoria());
        if (request.getAgendadoPara() != null) {
            comunicado.setAgendadoPara(request.getAgendadoPara());
            if (request.getAgendadoPara().isAfter(LocalDateTime.now())) {
                comunicado.setStatus(StatusComunicado.AGENDADO);
            }
        }
        
        comunicado = comunicadoRepository.save(comunicado);
        log.info("Comunicado atualizado: {}", comunicado.getTitulo());
        
        return buscarPorId(comunicado.getId());
    }
    
    @Transactional
    public ComunicadoDetalheDTO enviarComunicado(Long id) {
        Comunicado comunicado = comunicadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunicado não encontrado"));
        
        if (!comunicado.getStatus().equals(StatusComunicado.RASCUNHO) && 
            !comunicado.getStatus().equals(StatusComunicado.AGENDADO)) {
            throw new IllegalStateException("Este comunicado não pode ser enviado");
        }
        
        comunicado.setStatus(StatusComunicado.ENVIANDO);
        comunicado = comunicadoRepository.save(comunicado);
        
        log.info("Iniciando envio do comunicado: {}", comunicado.getTitulo());
        
        processarEnvio(comunicado);
        
        return buscarPorId(comunicado.getId());
    }
    
    @Transactional
    public ComunicadoDetalheDTO cancelar(Long id) {
        Comunicado comunicado = comunicadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunicado não encontrado"));
        
        if (!comunicado.isPodeCancelar()) {
            throw new IllegalStateException("Este comunicado não pode ser cancelado");
        }
        
        comunicado.setStatus(StatusComunicado.CANCELADO);
        comunicado = comunicadoRepository.save(comunicado);
        
        log.info("Comunicado cancelado: {}", comunicado.getTitulo());
        
        return buscarPorId(comunicado.getId());
    }
    
    @Transactional
    public void deletar(Long id) {
        Comunicado comunicado = comunicadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunicado não encontrado"));
        
        if (!comunicado.getStatus().equals(StatusComunicado.RASCUNHO)) {
            throw new IllegalStateException("Apenas rascunhos podem ser deletados");
        }
        
        comunicadoRepository.delete(comunicado);
        log.info("Comunicado deletado: {}", comunicado.getTitulo());
    }
    
    public EstatisticasComunicadoDTO obterEstatisticas() {
        List<Comunicado> todosComunicados = comunicadoRepository.findAll();
        
        Long total = (long) todosComunicados.size();
        Long rascunhos = comunicadoRepository.countByStatus(StatusComunicado.RASCUNHO);
        Long agendados = comunicadoRepository.countByStatus(StatusComunicado.AGENDADO);
        Long enviados = comunicadoRepository.countByStatus(StatusComunicado.ENVIADO);
        Long comErro = comunicadoRepository.countByStatus(StatusComunicado.ERRO);
        
        Long totalDestinatarios = comunicadoRepository.sumTotalDestinatarios();
        Long totalEnviados = comunicadoRepository.sumTotalEnviados();
        Long totalErros = comunicadoRepository.sumTotalErros();
        
        Double taxaSucesso = 0.0;
        if (totalDestinatarios != null && totalDestinatarios > 0 && totalEnviados != null) {
            taxaSucesso = (totalEnviados.doubleValue() / totalDestinatarios.doubleValue()) * 100;
        }
        
        Map<String, Long> porTipo = todosComunicados.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getTipo().getLabel(),
                        Collectors.counting()
                ));
        
        Map<String, Long> porCanal = todosComunicados.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCanal().getLabel(),
                        Collectors.counting()
                ));
        
        return EstatisticasComunicadoDTO.builder()
                .totalComunicados(total)
                .rascunhos(rascunhos)
                .agendados(agendados)
                .enviados(enviados)
                .comErro(comErro)
                .totalDestinatariosGeral(totalDestinatarios != null ? totalDestinatarios : 0L)
                .totalEnviadosGeral(totalEnviados != null ? totalEnviados : 0L)
                .totalErrosGeral(totalErros != null ? totalErros : 0L)
                .taxaSucesso(taxaSucesso)
                .porTipo(porTipo)
                .porCanal(porCanal)
                .build();
    }
    
    @Transactional
    public void processarComunicadosAgendados() {
        List<Comunicado> comunicadosAgendados = comunicadoRepository
                .findByStatusAndAgendadoParaBefore(StatusComunicado.AGENDADO, LocalDateTime.now());
        
        for (Comunicado comunicado : comunicadosAgendados) {
            log.info("Processando comunicado agendado: {}", comunicado.getTitulo());
            enviarComunicado(comunicado.getId());
        }
    }
    
    private void criarDestinatariosMock(Comunicado comunicado) {
        List<String[]> destinatariosMock = Arrays.asList(
            new String[]{"João Silva", "joao@email.com", "(16) 99999-0001"},
            new String[]{"Maria Santos", "maria@email.com", "(16) 99999-0002"},
            new String[]{"Pedro Oliveira", "pedro@email.com", "(16) 99999-0003"}
        );
        
        for (String[] dest : destinatariosMock) {
            ComunicadoDestinatario destinatario = ComunicadoDestinatario.builder()
                    .comunicado(comunicado)
                    .destinatarioNome(dest[0])
                    .destinatarioEmail(dest[1])
                    .destinatarioTelefone(dest[2])
                    .enviado(false)
                    .erro(false)
                    .tentativasEnvio(0)
                    .build();
            comunicadoDestinatarioRepository.save(destinatario);
        }
        
        comunicado.setTotalDestinatarios(destinatariosMock.size());
        comunicadoRepository.save(comunicado);
    }
    
    private void processarEnvio(Comunicado comunicado) {
        List<ComunicadoDestinatario> destinatarios = 
                comunicadoDestinatarioRepository.findByComunicadoAndEnviadoFalse(comunicado);
        
        int enviados = 0;
        int erros = 0;
        
        for (ComunicadoDestinatario destinatario : destinatarios) {
            try {
                if (comunicado.getCanal().equals(CanalComunicacao.EMAIL) || 
                    comunicado.getCanal().equals(CanalComunicacao.TODOS)) {
                    emailService.sendEmail(
                        destinatario.getDestinatarioEmail(),
                        comunicado.getTitulo(),
                        comunicado.getMensagem()
                    );
                }
                
                destinatario.setEnviado(true);
                destinatario.setEnviadoEm(LocalDateTime.now());
                destinatario.setErro(false);
                enviados++;
                
            } catch (Exception e) {
                destinatario.setErro(true);
                destinatario.setMensagemErro(e.getMessage());
                erros++;
                log.error("Erro ao enviar comunicado para {}: {}", 
                        destinatario.getDestinatarioEmail(), e.getMessage());
            }
            
            destinatario.setTentativasEnvio(destinatario.getTentativasEnvio() + 1);
            comunicadoDestinatarioRepository.save(destinatario);
        }
        
        comunicado.setTotalEnviados(enviados);
        comunicado.setTotalErros(erros);
        comunicado.setEnviadoEm(LocalDateTime.now());
        
        if (erros > 0) {
            comunicado.setStatus(StatusComunicado.ERRO);
            comunicado.setMensagemErro(String.format("Falha ao enviar para %d destinatário(s)", erros));
        } else {
            comunicado.setStatus(StatusComunicado.ENVIADO);
        }
        
        comunicadoRepository.save(comunicado);
        
        log.info("Envio do comunicado '{}' concluído. Enviados: {}, Erros: {}", 
                comunicado.getTitulo(), enviados, erros);
    }
    
    private ComunicadoResumoDTO toResumoDTO(Comunicado comunicado) {
        return ComunicadoResumoDTO.builder()
                .id(comunicado.getId())
                .titulo(comunicado.getTitulo())
                .tipo(comunicado.getTipo())
                .tipoLabel(comunicado.getTipo().getLabel())
                .status(comunicado.getStatus())
                .statusLabel(comunicado.getStatus().getLabel())
                .canal(comunicado.getCanal())
                .canalLabel(comunicado.getCanal().getLabel())
                .criadoPorNome(comunicado.getCriadoPor().getNome())
                .totalDestinatarios(comunicado.getTotalDestinatarios())
                .totalEnviados(comunicado.getTotalEnviados())
                .totalErros(comunicado.getTotalErros())
                .agendadoPara(comunicado.getAgendadoPara())
                .enviadoEm(comunicado.getEnviadoEm())
                .createdAt(comunicado.getCreatedAt())
                .podeEditar(comunicado.isPodeEditar())
                .podeCancelar(comunicado.isPodeCancelar())
                .build();
    }
    
    private ComunicadoDetalheDTO toDetalheDTO(Comunicado comunicado) {
        return ComunicadoDetalheDTO.builder()
                .id(comunicado.getId())
                .titulo(comunicado.getTitulo())
                .mensagem(comunicado.getMensagem())
                .tipo(comunicado.getTipo())
                .tipoLabel(comunicado.getTipo().getLabel())
                .status(comunicado.getStatus())
                .statusLabel(comunicado.getStatus().getLabel())
                .canal(comunicado.getCanal())
                .canalLabel(comunicado.getCanal().getLabel())
                .criadoPorId(comunicado.getCriadoPor().getId())
                .criadoPorNome(comunicado.getCriadoPor().getNome())
                .filtroBairro(comunicado.getFiltroBairro())
                .filtroCategoria(comunicado.getFiltroCategoria())
                .agendadoPara(comunicado.getAgendadoPara())
                .enviadoEm(comunicado.getEnviadoEm())
                .totalDestinatarios(comunicado.getTotalDestinatarios())
                .totalEnviados(comunicado.getTotalEnviados())
                .totalErros(comunicado.getTotalErros())
                .mensagemErro(comunicado.getMensagemErro())
                .createdAt(comunicado.getCreatedAt())
                .updatedAt(comunicado.getUpdatedAt())
                .podeEditar(comunicado.isPodeEditar())
                .podeCancelar(comunicado.isPodeCancelar())
                .build();
    }
    
    private ComunicadoDestinatarioDTO toDestinatarioDTO(ComunicadoDestinatario destinatario) {
        return ComunicadoDestinatarioDTO.builder()
                .id(destinatario.getId())
                .destinatarioNome(destinatario.getDestinatarioNome())
                .destinatarioEmail(destinatario.getDestinatarioEmail())
                .destinatarioTelefone(destinatario.getDestinatarioTelefone())
                .enviado(destinatario.getEnviado())
                .enviadoEm(destinatario.getEnviadoEm())
                .erro(destinatario.getErro())
                .mensagemErro(destinatario.getMensagemErro())
                .tentativasEnvio(destinatario.getTentativasEnvio())
                .build();
    }
}

