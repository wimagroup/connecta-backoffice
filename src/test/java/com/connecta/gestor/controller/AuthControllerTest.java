package com.connecta.gestor.controller;

import com.connecta.gestor.dto.*;
import com.connecta.gestor.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=com.connecta.gestor.config.DataInitializer"
})
@DisplayName("AuthController Tests")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    
    @Test
    @DisplayName("POST /api/auth/login - Deve fazer login com sucesso")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("test@email.com");
        request.setSenha("password123");
        
        LoginResponseDTO response = LoginResponseDTO.builder()
                .accessToken("jwt-access-token")
                .refreshToken("jwt-refresh-token")
                .tipo("Bearer")
                .expiresIn(900L)
                .email("test@email.com")
                .nome("Test User")
                .role("ROLE_SUPER_ADMIN")
                .build();
        
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("jwt-refresh-token"))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.nome").value("Test User"))
                .andExpect(jsonPath("$.role").value("ROLE_SUPER_ADMIN"))
                .andExpect(jsonPath("$.expiresIn").value(900));
        
        verify(authService).login(any(LoginRequestDTO.class));
    }
    
    @Test
    @DisplayName("POST /api/auth/login - Deve retornar 400 para dados inválidos")
    void shouldReturn400ForInvalidLoginData() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("invalid-email");
        request.setSenha("");
        
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/auth/recovery-password - Deve solicitar recuperação de senha")
    void shouldRecoverPassword() throws Exception {
        RecoveryPasswordRequestDTO request = new RecoveryPasswordRequestDTO();
        request.setEmail("test@email.com");
        
        doNothing().when(authService).recoveryPassword(any(RecoveryPasswordRequestDTO.class));
        
        mockMvc.perform(post("/api/auth/recovery-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("receberá instruções")));
        
        verify(authService).recoveryPassword(any(RecoveryPasswordRequestDTO.class));
    }
    
    @Test
    @DisplayName("POST /api/auth/reset-password - Deve redefinir senha")
    void shouldResetPassword() throws Exception {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("valid-token");
        request.setNovaSenha("newPassword123");
        
        doNothing().when(authService).resetPassword(any(ResetPasswordRequestDTO.class));
        
        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Senha redefinida com sucesso!"));
        
        verify(authService).resetPassword(any(ResetPasswordRequestDTO.class));
    }
    
    @Test
    @DisplayName("POST /api/auth/change-password - Deve alterar senha do usuário autenticado")
    @WithMockUser(username = "test@email.com")
    void shouldChangePassword() throws Exception {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setSenhaAtual("currentPassword");
        request.setNovaSenha("newPassword123");
        
        doNothing().when(authService).changePassword(anyString(), any(ChangePasswordRequestDTO.class));
        
        mockMvc.perform(post("/api/auth/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Senha alterada com sucesso!"));
        
        verify(authService).changePassword(anyString(), any(ChangePasswordRequestDTO.class));
    }
    
    @Test
    @DisplayName("GET /api/auth/me - Deve retornar dados do usuário autenticado")
    @WithMockUser(username = "test@email.com", roles = {"SUPER_ADMIN"})
    void shouldGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
