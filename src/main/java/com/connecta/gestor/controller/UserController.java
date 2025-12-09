package com.connecta.gestor.controller;

import com.connecta.gestor.dto.CreateUserRequestDTO;
import com.connecta.gestor.dto.UserResponseDTO;
import com.connecta.gestor.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody CreateUserRequestDTO request) {
        
        UserResponseDTO response = authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id) {
        String message = authService.toggleUserStatus(id);
        return ResponseEntity.ok(message);
    }
}
