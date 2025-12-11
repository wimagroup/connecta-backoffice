package com.connecta.gestor.service;

import com.connecta.gestor.dto.*;
import com.connecta.gestor.exception.ResourceNotFoundException;
import com.connecta.gestor.model.*;
import com.connecta.gestor.model.enums.StatusProtocolo;
import com.connecta.gestor.model.enums.TipoAcaoProtocolo;
import com.connecta.gestor.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProtocoloService {
    
    @Autowired
    private ProtocoloRepository protocoloRepository;
    
    @Autowired
    private ProtocoloDadoRepository protocoloDadoRepository;
    
    @Autowired
    private ProtocoloHistoricoRepository protocoloHistoricoRepository;
    
    @Autowired
    private ProtocoloComentarioRepository protocoloComentarioRepository;
    
    @Autowired
    private ServicoRepository servicoRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    public List<ProtocoloResumoDTO> listarTodos() {
        return protocoloRepository.findAll()
                .stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProtocoloResumoDTO> listarPorStatus(StatusProtocolo status) {
        return protocoloRepository.findByStatus(status)
                .stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProtocoloResumoDTO> listarPorAtendente(Long atendenteId) {
        User atendente = userRepository.findById(atendenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Atendente não encontrado"));
        
        return protocoloRepository.findByAtendente(atendente)
                .stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProtocoloResumoDTO> listarAtrasados() {
        List<StatusProtocolo> statusExcluidos = Arrays.asList(
            StatusProtocolo.FINALIZADO, 
            StatusProtocolo.CANCELADO
        );
        
        return protocoloRepository.findAtrasados(LocalDateTime.now(), statusExcluidos)
                .stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    public ProtocoloDetalheDTO buscarPorId(Long id) {
        Protocolo protocolo = protocoloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Protocolo não encontrado"));
        return toDetalheDTO(protocolo);
    }
    
    public ProtocoloDetalheDTO buscarPorNumero(String numeroProtocolo) {
        Protocolo protocolo = protocoloRepository.findByNumeroProtocolo(numeroProtocolo)
                .orElseThrow(() -> new ResourceNotFoundException("Protocolo não encontrado"));
        return toDetalheDTO(protocolo);
    }
    
    @Transactional
    public ProtocoloDetalheDTO criar(CreateProtocoloRequestDTO request) {
        Servico servico = servicoRepository.findById(request.getServicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
        
        if (!servico.getAtivo()) {
            throw new IllegalArgumentException("Este serviço não está disponível no momento");
        }
        
        String numeroProtocolo = gerarNumeroProtocolo();
        LocalDateTime prazoLimite = LocalDateTime.now().plusDays(servico.getPrazoAtendimentoDias());
        
        Protocolo protocolo = Protocolo.builder()
                .numeroProtocolo(numeroProtocolo)
                .servico(servico)
                .cidadaoNome(request.getCidadaoNome())
                .cidadaoEmail(request.getCidadaoEmail())
                .cidadaoTelefone(request.getCidadaoTelefone())
                .status(StatusProtocolo.ABERTO)
                .prioridade(request.getPrioridade() != null ? request.getPrioridade() : com.connecta.gestor.model.enums.PrioridadeProtocolo.MEDIA)
                .prazoLimite(prazoLimite)
                .descricaoProblema(request.getDescricaoProblema())
                .build();
        
        protocolo = protocoloRepository.save(protocolo);
        
        for (ProtocoloDadoRequestDTO dadoDTO : request.getDados()) {
            ProtocoloDado dado = ProtocoloDado.builder()
                    .protocolo(protocolo)
                    .campoTipo(dadoDTO.getCampoTipo())
                    .valor(dadoDTO.getValor())
                    .build();
            protocoloDadoRepository.save(dado);
        }
        
        registrarHistorico(protocolo, null, TipoAcaoProtocolo.CRIADO, 
                "Protocolo criado por: " + request.getCidadaoNome(), null, null);
        
        log.info("Protocolo criado: {} - {}", numeroProtocolo, servico.getTitulo());
        
        return buscarPorId(protocolo.getId());
    }
    
    @Transactional
    public ProtocoloDetalheDTO atribuir(Long protocoloId, AtribuirProtocoloRequestDTO request, User usuarioLogado) {
        Protocolo protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Protocolo não encontrado"));
        
        User atendente = userRepository.findById(request.getAtendenteId())
                .orElseThrow(() -> new ResourceNotFoundException("Atendente não encontrado"));
        
        String atendenteAnterior = protocolo.getAtendente() != null ? protocolo.getAtendente().getNome() : "Nenhum";
        
        protocolo.setAtendente(atendente);
        
        if (protocolo.getStatus().equals(StatusProtocolo.ABERTO)) {
            protocolo.setStatus(StatusProtocolo.EM_ANALISE);
        }
        
        protocolo = protocoloRepository.save(protocolo);
        
        registrarHistorico(protocolo, usuarioLogado, TipoAcaoProtocolo.ATRIBUIDO,
                String.format("Protocolo atribuído de '%s' para '%s'", atendenteAnterior, atendente.getNome()),
                null, null);
        
        log.info("Protocolo {} atribuído para: {}", protocolo.getNumeroProtocolo(), atendente.getNome());
        
        return buscarPorId(protocolo.getId());
    }
    
    @Transactional
    public ProtocoloDetalheDTO alterarStatus(Long protocoloId, AlterarStatusRequestDTO request, User usuarioLogado) {
        Protocolo protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Protocolo não encontrado"));
        
        StatusProtocolo statusAnterior = protocolo.getStatus();
        protocolo.setStatus(request.getStatus());
        
        protocolo = protocoloRepository.save(protocolo);
        
        String descricao = String.format("Status alterado de '%s' para '%s'", 
                statusAnterior.getLabel(), request.getStatus().getLabel());
        
        if (request.getObservacao() != null && !request.getObservacao().isEmpty()) {
            descricao += ". Observação: " + request.getObservacao();
        }
        
        registrarHistorico(protocolo, usuarioLogado, TipoAcaoProtocolo.STATUS_ALTERADO,
                descricao, statusAnterior.name(), request.getStatus().name());
        
        log.info("Status do protocolo {} alterado para: {}", protocolo.getNumeroProtocolo(), request.getStatus());
        
        return buscarPorId(protocolo.getId());
    }
    
    @Transactional
    public ProtocoloDetalheDTO alterarPrioridade(Long protocoloId, AlterarPrioridadeRequestDTO request, User usuarioLogado) {
        Protocolo protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Protocolo não encontrado"));
        
        com.connecta.gestor.model.enums.PrioridadeProtocolo prioridadeAnterior = protocolo.getPrioridade();
        protocolo.setPrioridade(request.getPrioridade());
        
        protocolo = protocoloRepository.save(protocolo);
        
        registrarHistorico(protocolo, usuarioLogado, TipoAcaoProtocolo.PRIORIDADE_ALTERADA,
                String.format("Prioridade alterada de '%s' para '%s'", 
                        prioridadeAnterior.getLabel(), request.getPrioridade().getLabel()),
                null, null);
        
        log.info("Prioridade do protocolo {} alterada para: {}", protocolo.getNumeroProtocolo(), request.getPrioridade());
        
        return buscarPorId(protocolo.getId());
    }
    
    @Transactional
    public ProtocoloComentarioDTO adicionarComentario(Long protocoloId, AdicionarComentarioRequestDTO request, User usuarioLogado) {
        Protocolo protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Protocolo não encontrado"));
        
        ProtocoloComentario comentario = ProtocoloComentario.builder()
                .protocolo(protocolo)
                .usuario(usuarioLogado)
                .comentario(request.getComentario())
                .interno(request.getInterno())
                .build();
        
        comentario = protocoloComentarioRepository.save(comentario);
        
        registrarHistorico(protocolo, usuarioLogado, TipoAcaoProtocolo.COMENTARIO_ADICIONADO,
                "Comentário " + (request.getInterno() ? "interno" : "público") + " adicionado",
                null, null);
        
        log.info("Comentário adicionado ao protocolo: {}", protocolo.getNumeroProtocolo());
        
        return toComentarioDTO(comentario);
    }
    
    @Transactional
    public ProtocoloDetalheDTO finalizar(Long protocoloId, FinalizarProtocoloRequestDTO request, User usuarioLogado) {
        Protocolo protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Protocolo não encontrado"));
        
        protocolo.setStatus(StatusProtocolo.FINALIZADO);
        protocolo.setRespostaFinal(request.getRespostaFinal());
        protocolo.setFinalizadoEm(LocalDateTime.now());
        
        protocolo = protocoloRepository.save(protocolo);
        
        registrarHistorico(protocolo, usuarioLogado, TipoAcaoProtocolo.FINALIZADO,
                "Protocolo finalizado. Resposta: " + request.getRespostaFinal(),
                null, null);
        
        log.info("Protocolo {} finalizado", protocolo.getNumeroProtocolo());
        
        return buscarPorId(protocolo.getId());
    }
    
    public EstatisticasProtocoloDTO obterEstatisticas() {
        List<Protocolo> todosProtocolos = protocoloRepository.findAll();
        
        Long total = (long) todosProtocolos.size();
        Long abertos = protocoloRepository.countByStatus(StatusProtocolo.ABERTO);
        Long emAnalise = protocoloRepository.countByStatus(StatusProtocolo.EM_ANALISE);
        Long emAndamento = protocoloRepository.countByStatus(StatusProtocolo.EM_ANDAMENTO);
        Long finalizados = protocoloRepository.countByStatus(StatusProtocolo.FINALIZADO);
        
        List<StatusProtocolo> statusExcluidos = Arrays.asList(StatusProtocolo.FINALIZADO, StatusProtocolo.CANCELADO);
        Long atrasados = (long) protocoloRepository.findAtrasados(LocalDateTime.now(), statusExcluidos).size();
        
        Double tempoMedio = todosProtocolos.stream()
                .filter(p -> p.getFinalizadoEm() != null)
                .mapToDouble(p -> ChronoUnit.DAYS.between(p.getCreatedAt(), p.getFinalizadoEm()))
                .average()
                .orElse(0.0);
        
        Map<String, Long> porCategoria = todosProtocolos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getServico().getCategoria().getNome(),
                        Collectors.counting()
                ));
        
        Map<String, Long> porStatus = todosProtocolos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatus().getLabel(),
                        Collectors.counting()
                ));
        
        return EstatisticasProtocoloDTO.builder()
                .total(total)
                .abertos(abertos)
                .emAnalise(emAnalise)
                .emAndamento(emAndamento)
                .finalizados(finalizados)
                .atrasados(atrasados)
                .tempoMedioAtendimento(tempoMedio)
                .porCategoria(porCategoria)
                .porStatus(porStatus)
                .build();
    }
    
    private String gerarNumeroProtocolo() {
        int anoAtual = LocalDateTime.now().getYear();
        Long ultimoId = protocoloRepository.findMaxIdByAno(anoAtual);
        
        int proximoNumero = (ultimoId != null) ? ultimoId.intValue() + 1 : 1;
        
        return String.format("#%d%04d", anoAtual, proximoNumero);
    }
    
    private void registrarHistorico(Protocolo protocolo, User usuario, TipoAcaoProtocolo acao, 
            String descricao, String statusAnterior, String statusNovo) {
        ProtocoloHistorico historico = ProtocoloHistorico.builder()
                .protocolo(protocolo)
                .usuario(usuario)
                .acao(acao)
                .descricao(descricao)
                .statusAnterior(statusAnterior)
                .statusNovo(statusNovo)
                .build();
        
        protocoloHistoricoRepository.save(historico);
    }
    
    private ProtocoloResumoDTO toResumoDTO(Protocolo protocolo) {
        return ProtocoloResumoDTO.builder()
                .id(protocolo.getId())
                .numeroProtocolo(protocolo.getNumeroProtocolo())
                .servicoTitulo(protocolo.getServico().getTitulo())
                .categoriaNome(protocolo.getServico().getCategoria().getNome())
                .cidadaoNome(protocolo.getCidadaoNome())
                .status(protocolo.getStatus())
                .statusLabel(protocolo.getStatus().getLabel())
                .prioridade(protocolo.getPrioridade())
                .prioridadeLabel(protocolo.getPrioridade().getLabel())
                .prioridadeCor(protocolo.getPrioridade().getCor())
                .atendenteNome(protocolo.getAtendente() != null ? protocolo.getAtendente().getNome() : null)
                .prazoLimite(protocolo.getPrazoLimite())
                .diasRestantes(protocolo.getDiasRestantes())
                .atrasado(protocolo.isAtrasado())
                .createdAt(protocolo.getCreatedAt())
                .build();
    }
    
    private ProtocoloDetalheDTO toDetalheDTO(Protocolo protocolo) {
        List<ProtocoloDadoDTO> dadosDTO = protocoloDadoRepository.findByProtocolo(protocolo)
                .stream()
                .map(dado -> ProtocoloDadoDTO.builder()
                        .id(dado.getId())
                        .campoTipo(dado.getCampoTipo())
                        .campoLabel(dado.getCampoTipo().getLabel())
                        .valor(dado.getValor())
                        .build())
                .collect(Collectors.toList());
        
        List<ProtocoloHistoricoDTO> historicoDTO = protocoloHistoricoRepository
                .findByProtocoloOrderByCreatedAtAsc(protocolo)
                .stream()
                .map(hist -> ProtocoloHistoricoDTO.builder()
                        .id(hist.getId())
                        .usuarioNome(hist.getUsuario() != null ? hist.getUsuario().getNome() : "Sistema")
                        .acao(hist.getAcao())
                        .acaoLabel(hist.getAcao().getLabel())
                        .descricao(hist.getDescricao())
                        .statusAnterior(hist.getStatusAnterior())
                        .statusNovo(hist.getStatusNovo())
                        .createdAt(hist.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        List<ProtocoloComentarioDTO> comentariosDTO = protocoloComentarioRepository
                .findByProtocoloOrderByCreatedAtAsc(protocolo)
                .stream()
                .map(this::toComentarioDTO)
                .collect(Collectors.toList());
        
        return ProtocoloDetalheDTO.builder()
                .id(protocolo.getId())
                .numeroProtocolo(protocolo.getNumeroProtocolo())
                .servicoId(protocolo.getServico().getId())
                .servicoTitulo(protocolo.getServico().getTitulo())
                .categoriaNome(protocolo.getServico().getCategoria().getNome())
                .cidadaoNome(protocolo.getCidadaoNome())
                .cidadaoEmail(protocolo.getCidadaoEmail())
                .cidadaoTelefone(protocolo.getCidadaoTelefone())
                .status(protocolo.getStatus())
                .statusLabel(protocolo.getStatus().getLabel())
                .prioridade(protocolo.getPrioridade())
                .prioridadeLabel(protocolo.getPrioridade().getLabel())
                .atendenteId(protocolo.getAtendente() != null ? protocolo.getAtendente().getId() : null)
                .atendenteNome(protocolo.getAtendente() != null ? protocolo.getAtendente().getNome() : null)
                .descricaoProblema(protocolo.getDescricaoProblema())
                .prazoLimite(protocolo.getPrazoLimite())
                .diasRestantes(protocolo.getDiasRestantes())
                .atrasado(protocolo.isAtrasado())
                .finalizadoEm(protocolo.getFinalizadoEm())
                .respostaFinal(protocolo.getRespostaFinal())
                .dados(dadosDTO)
                .historico(historicoDTO)
                .comentarios(comentariosDTO)
                .createdAt(protocolo.getCreatedAt())
                .updatedAt(protocolo.getUpdatedAt())
                .build();
    }
    
    private ProtocoloComentarioDTO toComentarioDTO(ProtocoloComentario comentario) {
        return ProtocoloComentarioDTO.builder()
                .id(comentario.getId())
                .usuarioId(comentario.getUsuario().getId())
                .usuarioNome(comentario.getUsuario().getNome())
                .comentario(comentario.getComentario())
                .interno(comentario.getInterno())
                .createdAt(comentario.getCreatedAt())
                .build();
    }
}



