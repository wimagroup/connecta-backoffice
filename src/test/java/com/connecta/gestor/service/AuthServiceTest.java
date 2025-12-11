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
    private com.connecta.gestor.repository.RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private PasswordRecoveryTokenService tokenService;
    
    @Mock
    private com.connecta.gestor.validator.PasswordValidator passwordValidator;
    
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
        
        org.springframework.test.util.ReflectionTestUtils.setField(
            authService, "tokenExpirationHours", 1
        );
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
        when(jwtUtil.generateAccessToken(any())).thenReturn("jwt-access-token");
        when(jwtUtil.generateRefreshToken()).thenReturn("jwt-refresh-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(900000L);
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(2592000000L);
        when(refreshTokenRepository.save(any())).thenReturn(null);
        
        LoginResponseDTO response = authService.login(request);
        
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@email.com");
        assertThat(response.getNome()).isEqualTo("Test User");
        assertThat(response.getRole()).isEqualTo("ROLE_SUPER_ADMIN");
        assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("jwt-refresh-token");
        assertThat(response.getTipo()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900L);
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@email.com");
        verify(jwtUtil).generateAccessToken(testUser);
        verify(jwtUtil).generateRefreshToken();
        verify(refreshTokenRepository).save(any());
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
        when(tokenService.generateSecureToken()).thenReturn("secure-token-123");
        when(userRepository.save(any())).thenReturn(testUser);
        doNothing().when(emailService).sendRecoveryPasswordEmail(anyString(), anyString(), anyString());
        
        assertThatCode(() -> authService.recoveryPassword(request))
                .doesNotThrowAnyException();
        
        verify(userRepository).findByEmail("test@email.com");
        verify(tokenService).generateSecureToken();
        verify(userRepository).save(any(User.class));
        verify(emailService).sendRecoveryPasswordEmail(anyString(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("Não deve lançar exceção ao solicitar recuperação para email inexistente (segurança OWASP)")
    void shouldNotThrowExceptionWhenRecoverPasswordWithNonExistentEmail() {
        RecoveryPasswordRequestDTO request = new RecoveryPasswordRequestDTO();
        request.setEmail("nonexistent@email.com");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        assertThatCode(() -> authService.recoveryPassword(request))
                .doesNotThrowAnyException();
        
        verify(userRepository).findByEmail("nonexistent@email.com");
        verify(emailService, never()).sendRecoveryPasswordEmail(anyString(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("Deve redefinir senha com token válido")
    void shouldResetPasswordWithValidToken() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("valid-token");
        request.setNovaSenha("NewP@ssw0rd123");
        
        testUser.setTokenRecuperacao("valid-token");
        testUser.setTokenRecuperacaoExpiracao(LocalDateTime.now().plusHours(1));
        
        com.connecta.gestor.validator.PasswordValidator.ValidationResult validationResult = 
            new com.connecta.gestor.validator.PasswordValidator.ValidationResult(true, java.util.Collections.emptyList());
        
        when(tokenService.validateTokenFormat(anyString())).thenReturn(true);
        when(userRepository.findByTokenRecuperacao(anyString())).thenReturn(Optional.of(testUser));
        when(passwordValidator.validate(anyString())).thenReturn(validationResult);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any())).thenReturn(testUser);
        when(refreshTokenRepository.findByUser(any())).thenReturn(java.util.Collections.emptyList());
        doNothing().when(emailService).sendPasswordChangedEmail(anyString(), anyString());
        
        assertThatCode(() -> authService.resetPassword(request))
                .doesNotThrowAnyException();
        
        verify(tokenService).validateTokenFormat("valid-token");
        verify(userRepository).findByTokenRecuperacao("valid-token");
        verify(passwordValidator).validate("NewP@ssw0rd123");
        verify(passwordEncoder).encode("NewP@ssw0rd123");
        verify(userRepository).save(testUser);
        verify(refreshTokenRepository).findByUser(testUser);
        verify(emailService).sendPasswordChangedEmail(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao redefinir senha com token inválido")
    void shouldThrowExceptionWhenResetPasswordWithInvalidToken() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("invalid-token");
        request.setNovaSenha("NewP@ssw0rd123");
        
        when(tokenService.validateTokenFormat(anyString())).thenReturn(true);
        when(userRepository.findByTokenRecuperacao(anyString())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Token inválido ou já utilizado");
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao redefinir senha com token expirado")
    void shouldThrowExceptionWhenResetPasswordWithExpiredToken() {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("expired-token");
        request.setNovaSenha("NewP@ssw0rd123");
        
        testUser.setTokenRecuperacao("expired-token");
        testUser.setTokenRecuperacaoExpiracao(LocalDateTime.now().minusHours(1));
        
        when(tokenService.validateTokenFormat(anyString())).thenReturn(true);
        when(userRepository.findByTokenRecuperacao(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Token expirado");
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
    
    @Test
    @DisplayName("Deve realizar logout com sucesso")
    void shouldLogoutSuccessfully() {
        String refreshTokenString = "valid-refresh-token";
        com.connecta.gestor.model.RefreshToken refreshToken = com.connecta.gestor.model.RefreshToken.builder()
                .id(1L)
                .token(refreshTokenString)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(refreshTokenRepository.findByToken(refreshTokenString))
                .thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any())).thenReturn(refreshToken);
        
        assertThatCode(() -> authService.logout(refreshTokenString))
                .doesNotThrowAnyException();
        
        verify(refreshTokenRepository).findByToken(refreshTokenString);
        verify(refreshTokenRepository).save(any(com.connecta.gestor.model.RefreshToken.class));
        assertThat(refreshToken.getRevoked()).isTrue();
        assertThat(refreshToken.getRevokedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao fazer logout com refresh token nulo")
    void shouldThrowExceptionWhenLogoutWithNullToken() {
        assertThatThrownBy(() -> authService.logout(null))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token não fornecido");
        
        verify(refreshTokenRepository, never()).findByToken(anyString());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao fazer logout com refresh token vazio")
    void shouldThrowExceptionWhenLogoutWithEmptyToken() {
        assertThatThrownBy(() -> authService.logout(""))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token não fornecido");
        
        verify(refreshTokenRepository, never()).findByToken(anyString());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao fazer logout com refresh token inexistente")
    void shouldThrowExceptionWhenLogoutWithNonExistentToken() {
        String invalidToken = "non-existent-token";
        
        when(refreshTokenRepository.findByToken(invalidToken))
                .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> authService.logout(invalidToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token inválido ou não encontrado");
        
        verify(refreshTokenRepository).findByToken(invalidToken);
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao fazer logout com refresh token já revogado")
    void shouldThrowExceptionWhenLogoutWithRevokedToken() {
        String revokedTokenString = "revoked-token";
        com.connecta.gestor.model.RefreshToken revokedToken = com.connecta.gestor.model.RefreshToken.builder()
                .id(1L)
                .token(revokedTokenString)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(30))
                .revoked(true)
                .revokedAt(LocalDateTime.now().minusHours(1))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        when(refreshTokenRepository.findByToken(revokedTokenString))
                .thenReturn(Optional.of(revokedToken));
        
        assertThatThrownBy(() -> authService.logout(revokedTokenString))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token já foi revogado");
        
        verify(refreshTokenRepository).findByToken(revokedTokenString);
        verify(refreshTokenRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao fazer logout com refresh token expirado")
    void shouldThrowExceptionWhenLogoutWithExpiredToken() {
        String expiredTokenString = "expired-token";
        com.connecta.gestor.model.RefreshToken expiredToken = com.connecta.gestor.model.RefreshToken.builder()
                .id(1L)
                .token(expiredTokenString)
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .createdAt(LocalDateTime.now().minusDays(31))
                .build();
        
        when(refreshTokenRepository.findByToken(expiredTokenString))
                .thenReturn(Optional.of(expiredToken));
        
        assertThatThrownBy(() -> authService.logout(expiredTokenString))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token expirado");
        
        verify(refreshTokenRepository).findByToken(expiredTokenString);
        verify(refreshTokenRepository, never()).save(any());
    }
}

