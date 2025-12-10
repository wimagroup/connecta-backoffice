package com.connecta.gestor.controller;

import com.connecta.gestor.dto.*;
import com.connecta.gestor.model.User;
import com.connecta.gestor.model.enums.StatusComunicado;
import com.connecta.gestor.repository.UserRepository;
import com.connecta.gestor.service.ComunicadoService;
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
@RequestMapping("/api/comunicados")
@CrossOrigin(origins = "*")
@Slf4j
public class ComunicadoController {
    
    @Autowired
    private ComunicadoService comunicadoService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<List<ComunicadoResumoDTO>> listarTodos() {
        return ResponseEntity.ok(comunicadoService.listarTodos());
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<List<ComunicadoResumoDTO>> listarPorStatus(@PathVariable StatusComunicado status) {
        return ResponseEntity.ok(comunicadoService.listarPorStatus(status));
    }
    
    @GetMapping("/meus")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<List<ComunicadoResumoDTO>> listarMeusComunicados(
            @AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.ok(comunicadoService.listarPorUsuario(usuarioLogado.getId(), userRepository));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<ComunicadoDetalheDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comunicadoService.buscarPorId(id));
    }
    
    @GetMapping("/{id}/destinatarios")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<List<ComunicadoDestinatarioDTO>> buscarDestinatarios(@PathVariable Long id) {
        return ResponseEntity.ok(comunicadoService.buscarDestinatarios(id));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<ComunicadoDetalheDTO> criar(
            @Valid @RequestBody CreateComunicadoRequestDTO request,
            @AuthenticationPrincipal User usuarioLogado) {
        ComunicadoDetalheDTO comunicado = comunicadoService.criar(request, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(comunicado);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<ComunicadoDetalheDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComunicadoRequestDTO request) {
        return ResponseEntity.ok(comunicadoService.atualizar(id, request));
    }
    
    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<ComunicadoDetalheDTO> enviar(@PathVariable Long id) {
        return ResponseEntity.ok(comunicadoService.enviarComunicado(id));
    }
    
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<ComunicadoDetalheDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(comunicadoService.cancelar(id));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        comunicadoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/estatisticas")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<EstatisticasComunicadoDTO> obterEstatisticas() {
        return ResponseEntity.ok(comunicadoService.obterEstatisticas());
    }
}


