package com.connecta.gestor.controller;

import com.connecta.gestor.dto.*;
import com.connecta.gestor.model.User;
import com.connecta.gestor.model.enums.StatusProtocolo;
import com.connecta.gestor.service.ProtocoloService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/protocolos")
@CrossOrigin(origins = "*")
@Slf4j
public class ProtocoloController {
    
    @Autowired
    private ProtocoloService protocoloService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ATENDENTE')")
    public ResponseEntity<List<ProtocoloResumoDTO>> listarTodos() {
        return ResponseEntity.ok(protocoloService.listarTodos());
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ATENDENTE')")
    public ResponseEntity<List<ProtocoloResumoDTO>> listarPorStatus(@PathVariable StatusProtocolo status) {
        return ResponseEntity.ok(protocoloService.listarPorStatus(status));
    }
    
    @GetMapping("/atendente/{atendenteId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ATENDENTE')")
    public ResponseEntity<List<ProtocoloResumoDTO>> listarPorAtendente(@PathVariable Long atendenteId) {
        return ResponseEntity.ok(protocoloService.listarPorAtendente(atendenteId));
    }
    
    @GetMapping("/atrasados")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ATENDENTE')")
    public ResponseEntity<List<ProtocoloResumoDTO>> listarAtrasados() {
        return ResponseEntity.ok(protocoloService.listarAtrasados());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ATENDENTE')")
    public ResponseEntity<ProtocoloDetalheDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(protocoloService.buscarPorId(id));
    }
    
    @GetMapping("/numero/{numeroProtocolo}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ATENDENTE')")
    public ResponseEntity<ProtocoloDetalheDTO> buscarPorNumero(@PathVariable String numeroProtocolo) {
        return ResponseEntity.ok(protocoloService.buscarPorNumero(numeroProtocolo));
    }
    
    @PostMapping
    public ResponseEntity<ProtocoloDetalheDTO> criar(@Valid @RequestBody CreateProtocoloRequestDTO request) {
        ProtocoloDetalheDTO protocolo = protocoloService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(protocolo);
    }
    
    @PatchMapping("/{id}/atribuir")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<ProtocoloDetalheDTO> atribuir(
            @PathVariable Long id,
            @Valid @RequestBody AtribuirProtocoloRequestDTO request,
            @AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.ok(protocoloService.atribuir(id, request, usuarioLogado));
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ATENDENTE')")
    public ResponseEntity<ProtocoloDetalheDTO> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AlterarStatusRequestDTO request,
            @AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.ok(protocoloService.alterarStatus(id, request, usuarioLogado));
    }
    
    @PatchMapping("/{id}/prioridade")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<ProtocoloDetalheDTO> alterarPrioridade(
            @PathVariable Long id,
            @Valid @RequestBody AlterarPrioridadeRequestDTO request,
            @AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.ok(protocoloService.alterarPrioridade(id, request, usuarioLogado));
    }
    
    @PostMapping("/{id}/comentarios")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ATENDENTE')")
    public ResponseEntity<ProtocoloComentarioDTO> adicionarComentario(
            @PathVariable Long id,
            @Valid @RequestBody AdicionarComentarioRequestDTO request,
            @AuthenticationPrincipal User usuarioLogado) {
        ProtocoloComentarioDTO comentario = protocoloService.adicionarComentario(id, request, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
    }
    
    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ATENDENTE')")
    public ResponseEntity<ProtocoloDetalheDTO> finalizar(
            @PathVariable Long id,
            @Valid @RequestBody FinalizarProtocoloRequestDTO request,
            @AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.ok(protocoloService.finalizar(id, request, usuarioLogado));
    }
    
    @GetMapping("/estatisticas")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<EstatisticasProtocoloDTO> obterEstatisticas() {
        return ResponseEntity.ok(protocoloService.obterEstatisticas());
    }
}



