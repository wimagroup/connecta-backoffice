package com.connecta.gestor.service;

import com.connecta.gestor.dto.*;
import com.connecta.gestor.exception.EmailAlreadyExistsException;
import com.connecta.gestor.exception.InvalidTokenException;
import com.connecta.gestor.exception.ResourceNotFoundException;
import com.connecta.gestor.model.Role;
import com.connecta.gestor.model.User;
import com.connecta.gestor.model.enums.RoleType;
import com.connecta.gestor.repository.RoleRepository;
import com.connecta.gestor.repository.UserRepository;
import com.connecta.gestor.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private Role testRole;
    
    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setNome(RoleType.ROLE_SUPER_ADMIN);
        testRole.setDescricao("Super Administrador");
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@email.com");
        testUser.setSenha("encodedPassword");
        testUser.setNome("Test User");
        testUser.setRole(testRole);
        testUser.setAtivo(true);
    }
    
    @Test
    @DisplayName("Deve fazer login com sucesso")
    void shouldLoginSuccessfully() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("test@email.com");
        request.setSenha("password123");
        
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(any())).thenReturn("jwt-token");
        
        LoginResponseDTO response = authService.login(request);
        
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("test@email.com");
        assertThat(response.getNome()).isEqualTo("Test User");
        assertThat(response.getRole()).isEqualTo("ROLE_SUPER_ADMIN");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@email.com");
        verify(jwtUtil).generateToken(testUser);
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao fazer login com credenciais inválidas")
    void shouldThrowExceptionWhenLoginWithInvalidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("test@email.com");
        request.setSenha("wrongPassword");
        
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Email ou senha inválidos");
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao fazer login com usuário inativo")
    void shouldThrowExceptionWhenLoginWithInactiveUser() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("test@email.com");
        request.setSenha("password123");
        
        testUser.setAtivo(false);
        
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
    
    @Test
    @DisplayName("Deve solicitar recuperação de senha com sucesso")
    void shouldRecoverPasswordSuccessfully() {
        RecoveryPasswordRequestDTO request = new RecoveryPasswordRequestDTO();
        request.setEmail("test@email.com");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);
        doNothing().when(emailService).sendRecoveryPasswordEmail(anyString(), anyString());
        
        assertThatCode(() -> authService.recoveryPassword(request))
                .doesNotThrowAnyException();
        
        verify(userRepository).findByEmail("test@email.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendRecoveryPasswordEmail(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao solicitar recuperação para email inexistente")
    void shouldThrowExceptionWhenRecoverPasswordWithNonExistentEmail() {
        RecoveryPasswordRequestDTO request = new RecoveryPasswordRequestDTO();
        request.setEmail("nonexistent@email.com");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> authService.recoveryPassword(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário não encontrado com o email informado");
    }
    
    @Test
    @DisplayName("Deve redefinir senha com token válido")
    void shouldResetPasswordWithValidToken() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("valid-token");
        request.setNovaSenha("newPassword123");
        
        testUser.setTokenRecuperacao("valid-token");
        testUser.setTokenRecuperacaoExpiracao(LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByTokenRecuperacao(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any())).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordChangedEmail(anyString(), anyString());
        
        assertThatCode(() -> authService.resetPassword(request))
                .doesNotThrowAnyException();
        
        verify(userRepository).findByTokenRecuperacao("valid-token");
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
        verify(emailService).sendPasswordChangedEmail(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao redefinir senha com token inválido")
    void shouldThrowExceptionWhenResetPasswordWithInvalidToken() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("invalid-token");
        request.setNovaSenha("newPassword123");
        
        when(userRepository.findByTokenRecuperacao(anyString())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token inválido");
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao redefinir senha com token expirado")
    void shouldThrowExceptionWhenResetPasswordWithExpiredToken() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("expired-token");
        request.setNovaSenha("newPassword123");
        
        testUser.setTokenRecuperacao("expired-token");
        testUser.setTokenRecuperacaoExpiracao(LocalDateTime.now().minusHours(1));
        
        when(userRepository.findByTokenRecuperacao(anyString())).thenReturn(Optional.of(testUser));
        
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token expirado");
    }
    
    @Test
    @DisplayName("Deve alterar senha com sucesso")
    void shouldChangePasswordSuccessfully() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setSenhaAtual("currentPassword");
        request.setNovaSenha("newPassword123");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("currentPassword"), eq("encodedPassword"))).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any())).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordChangedEmail(anyString(), anyString());
        
        assertThatCode(() -> authService.changePassword("test@email.com", request))
                .doesNotThrowAnyException();
        
        verify(userRepository).findByEmail("test@email.com");
        verify(passwordEncoder).matches(eq("currentPassword"), eq("encodedPassword"));
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
        verify(emailService).sendPasswordChangedEmail(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao alterar senha com senha atual incorreta")
    void shouldThrowExceptionWhenChangePasswordWithWrongCurrentPassword() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setSenhaAtual("wrongPassword");
        request.setNovaSenha("newPassword123");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        
        assertThatThrownBy(() -> authService.changePassword("test@email.com", request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Senha atual incorreta");
    }
    
    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setNome("New User");
        request.setEmail("newuser@email.com");
        request.setSenha("password123");
        request.setRoleId(1L);
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(testUser);
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString(), anyString());
        
        UserResponseDTO response = authService.createUser(request);
        
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@email.com");
        assertThat(response.getNome()).isEqualTo("Test User");
        
        verify(userRepository).existsByEmail("newuser@email.com");
        verify(roleRepository).findById(1L);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendWelcomeEmail(anyString(), anyString(), eq("password123"));
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email já existente")
    void shouldThrowExceptionWhenCreateUserWithExistingEmail() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setNome("New User");
        request.setEmail("existing@email.com");
        request.setSenha("password123");
        request.setRoleId(1L);
        
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        assertThatThrownBy(() -> authService.createUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Já existe um usuário cadastrado com este email");
    }
    
    @Test
    @DisplayName("Deve ativar/desativar usuário com sucesso")
    void shouldToggleUserStatusSuccessfully() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);
        
        String result = authService.toggleUserStatus(1L);
        
        assertThat(result).contains("desativado");
        assertThat(testUser.getAtivo()).isFalse();
        
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar ativar/desativar usuário inexistente")
    void shouldThrowExceptionWhenToggleNonExistentUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> authService.toggleUserStatus(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }
}

