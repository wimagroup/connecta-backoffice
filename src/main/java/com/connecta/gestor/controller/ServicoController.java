package com.connecta.gestor.controller;

import com.connecta.gestor.dto.CreateServicoRequestDTO;
import com.connecta.gestor.dto.ServicoDTO;
import com.connecta.gestor.dto.UpdateServicoRequestDTO;
import com.connecta.gestor.service.ServicoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@CrossOrigin(origins = "*")
@Slf4j
public class ServicoController {
    
    @Autowired
    private ServicoService servicoService;
    
    @GetMapping
    public ResponseEntity<List<ServicoDTO>> listarTodos() {
        return ResponseEntity.ok(servicoService.listarTodos());
    }
    
    @GetMapping("/ativos")
    public ResponseEntity<List<ServicoDTO>> listarAtivos() {
        return ResponseEntity.ok(servicoService.listarAtivos());
    }
    
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ServicoDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(servicoService.listarPorCategoria(categoriaId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ServicoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<ServicoDTO> criar(@Valid @RequestBody CreateServicoRequestDTO request) {
        ServicoDTO servico = servicoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(servico);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<ServicoDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServicoRequestDTO request) {
        return ResponseEntity.ok(servicoService.atualizar(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<ServicoDTO> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.toggleStatus(id));
    }
}

