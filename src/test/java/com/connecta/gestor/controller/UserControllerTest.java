package com.connecta.gestor.controller;

import com.connecta.gestor.dto.CreateUserRequestDTO;
import com.connecta.gestor.dto.UserResponseDTO;
import com.connecta.gestor.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=com.connecta.gestor.config.DataInitializer"
})
@DisplayName("UserController Tests")
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    
    @Test
    @DisplayName("POST /api/users - Deve criar usuário com sucesso")
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void shouldCreateUserSuccessfully() throws Exception {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setNome("New User");
        request.setEmail("newuser@email.com");
        request.setSenha("password123");
        request.setRoleId(1L);
        
        UserResponseDTO response = new UserResponseDTO(
                1L,
                "New User",
                "newuser@email.com",
                "ROLE_GESTOR",
                true,
                LocalDateTime.now()
        );
        
        when(authService.createUser(any(CreateUserRequestDTO.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("New User"))
                .andExpect(jsonPath("$.email").value("newuser@email.com"))
                .andExpect(jsonPath("$.role").value("ROLE_GESTOR"))
                .andExpect(jsonPath("$.ativo").value(true));
        
        verify(authService).createUser(any(CreateUserRequestDTO.class));
    }
    
    @Test
    @DisplayName("POST /api/users - Deve retornar 400 para dados inválidos")
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void shouldReturn400ForInvalidData() throws Exception {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setNome("");
        request.setEmail("invalid-email");
        
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("PATCH /api/users/{id}/toggle-status - Deve ativar/desativar usuário")
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void shouldToggleUserStatus() throws Exception {
        when(authService.toggleUserStatus(anyLong())).thenReturn("Usuário desativado com sucesso");
        
        mockMvc.perform(patch("/api/users/1/toggle-status")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuário desativado com sucesso"));
        
        verify(authService).toggleUserStatus(1L);
    }
}
