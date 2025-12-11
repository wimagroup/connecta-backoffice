package com.connecta.gestor.service;

import com.connecta.gestor.dto.*;
import com.connecta.gestor.exception.EmailAlreadyExistsException;
import com.connecta.gestor.exception.InvalidTokenException;
import com.connecta.gestor.exception.ResourceNotFoundException;
import com.connecta.gestor.model.RefreshToken;
import com.connecta.gestor.model.Role;
import com.connecta.gestor.model.User;
import com.connecta.gestor.repository.RefreshTokenRepository;
import com.connecta.gestor.repository.RoleRepository;
import com.connecta.gestor.repository.UserRepository;
import com.connecta.gestor.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordRecoveryTokenService tokenService;
    
    @Autowired
    private com.connecta.gestor.validator.PasswordValidator passwordValidator;
    
    @Value("${app.password-recovery.token-expiration-hours:1}")
    private int tokenExpirationHours;
    
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            logger.info("Tentativa de login para o email: {}", request.getEmail());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getSenha()
                    )
            );
            
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            
            if (!user.getAtivo()) {
                throw new BadCredentialsException("Usuário inativo");
            }
            
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshTokenString = jwtUtil.generateRefreshToken();
            
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenString)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusNanos(jwtUtil.getRefreshTokenExpiration() * 1_000_000))
                    .revoked(false)
                    .build();
            
            refreshTokenRepository.save(refreshToken);
            
            logger.info("Login realizado com sucesso para: {}", request.getEmail());
            
            return LoginResponseDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nome(user.getNome())
                    .role(user.getRole().getNome().name())
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenString)
                    .tipo("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                    .build();
            
        } catch (BadCredentialsException e) {
            logger.error("Falha no login para: {}", request.getEmail());
            throw new BadCredentialsException("Email ou senha inválidos");
        }
    }
    
    @Transactional
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        logger.info("Tentativa de refresh token");
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token inválido"));
        
        if (refreshToken.getRevoked()) {
            throw new InvalidTokenException("Refresh token foi revogado");
        }
        
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException("Refresh token expirado");
        }
        
        User user = refreshToken.getUser();
        
        if (!user.getAtivo()) {
            throw new BadCredentialsException("Usuário inativo");
        }
        
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshTokenString = jwtUtil.generateRefreshToken();
        
        refreshToken.setToken(newRefreshTokenString);
        refreshToken.setExpiryDate(LocalDateTime.now().plusNanos(jwtUtil.getRefreshTokenExpiration() * 1_000_000));
        refreshTokenRepository.save(refreshToken);
        
        logger.info("Refresh token realizado com sucesso para: {}", user.getEmail());
        
        return LoginResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nome(user.getNome())
                .role(user.getRole().getNome().name())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenString)
                .tipo("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .build();
    }
    
    @Transactional
    public void logout(String refreshTokenString) {
        logger.info("Tentativa de logout");
        
        if (refreshTokenString == null || refreshTokenString.trim().isEmpty()) {
            logger.warn("Tentativa de logout com refresh token nulo ou vazio");
            throw new InvalidTokenException("Refresh token não fornecido");
        }
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> {
                    logger.warn("Tentativa de logout com refresh token inexistente: {}", 
                            refreshTokenString.substring(0, Math.min(10, refreshTokenString.length())) + "...");
                    return new InvalidTokenException("Refresh token inválido ou não encontrado");
                });
        
        if (refreshToken.getRevoked()) {
            logger.warn("Tentativa de logout com token já revogado. Usuário ID: {}, Token revogado em: {}", 
                    refreshToken.getUser().getId(), refreshToken.getRevokedAt());
            throw new InvalidTokenException("Refresh token já foi revogado");
        }
        
        if (refreshToken.isExpired()) {
            logger.warn("Tentativa de logout com token expirado. Usuário ID: {}, Expirado em: {}", 
                    refreshToken.getUser().getId(), refreshToken.getExpiryDate());
            throw new InvalidTokenException("Refresh token expirado");
        }
        
        User user = refreshToken.getUser();
        
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
        
        logger.info("Logout realizado com sucesso. Usuário: {} (ID: {}), Email: {}, Token revogado em: {}", 
                user.getNome(), user.getId(), user.getEmail(), refreshToken.getRevokedAt());
    }
    
    @Transactional
    public void recoveryPassword(RecoveryPasswordRequestDTO request) {
        logger.info("Solicitação de recuperação de senha recebida");
        
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElse(null);
            
            if (user != null && user.getAtivo()) {
                String token = tokenService.generateSecureToken();
                
                user.setTokenRecuperacao(token);
                user.setTokenRecuperacaoExpiracao(
                    LocalDateTime.now().plusHours(tokenExpirationHours)
                );
                
                userRepository.save(user);
                
                emailService.sendRecoveryPasswordEmail(user.getEmail(), user.getNome(), token);
                
                logger.info("Token de recuperação gerado e email enviado");
            } else {
                logger.warn("Tentativa de recuperação para email inexistente ou usuário inativo");
                Thread.sleep(2000);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Erro ao processar recuperação de senha", e);
        } catch (Exception e) {
            logger.error("Erro ao processar recuperação de senha", e);
        }
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        logger.info("Tentativa de reset de senha com token");
        
        if (!tokenService.validateTokenFormat(request.getToken())) {
            logger.warn("Formato de token inválido recebido");
            throw new InvalidTokenException("Token inválido ou malformado");
        }
        
        User user = userRepository.findByTokenRecuperacao(request.getToken())
                .orElseThrow(() -> {
                    logger.warn("Token não encontrado no banco de dados");
                    return new InvalidTokenException("Token inválido ou já utilizado");
                });
        
        if (user.getTokenRecuperacaoExpiracao() == null ||
            LocalDateTime.now().isAfter(user.getTokenRecuperacaoExpiracao())) {
            logger.warn("Token expirado para usuário: {}", user.getEmail());
            user.setTokenRecuperacao(null);
            user.setTokenRecuperacaoExpiracao(null);
            userRepository.save(user);
            throw new InvalidTokenException("Token expirado. Solicite uma nova recuperação de senha");
        }
        
        if (!user.getAtivo()) {
            logger.warn("Tentativa de reset de senha para usuário inativo: {}", user.getEmail());
            throw new BadCredentialsException("Usuário inativo");
        }
        
        com.connecta.gestor.validator.PasswordValidator.ValidationResult validation = 
            passwordValidator.validate(request.getNovaSenha());
        
        if (!validation.isValid()) {
            logger.warn("Senha fraca fornecida durante reset");
            throw new IllegalArgumentException(validation.getErrorMessage());
        }
        
        user.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        user.setTokenRecuperacao(null);
        user.setTokenRecuperacaoExpiracao(null);
        
        userRepository.save(user);
        
        refreshTokenRepository.findByUser(user)
            .forEach(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getNome());
        
        logger.info("Senha redefinida com sucesso. Todas as sessões anteriores foram invalidadas");
    }
    
    @Transactional
    public void changePassword(String email, ChangePasswordRequestDTO request) {
        logger.info("Tentativa de alteração de senha para: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        if (!passwordEncoder.matches(request.getSenhaAtual(), user.getSenha())) {
            throw new BadCredentialsException("Senha atual incorreta");
        }
        
        user.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        userRepository.save(user);
        
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getNome());
        
        logger.info("Senha alterada com sucesso para: {}", email);
    }
    
    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        logger.info("Criando novo usuário com email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Já existe um usuário cadastrado com este email");
        }
        
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada"));
        
        User user = new User();
        user.setNome(request.getNome());
        user.setEmail(request.getEmail());
        user.setSenha(passwordEncoder.encode(request.getSenha()));
        user.setRole(role);
        user.setAtivo(true);
        
        User savedUser = userRepository.save(user);
        
        emailService.sendWelcomeEmail(
                savedUser.getEmail(),
                savedUser.getNome(),
                request.getSenha()
        );
        
        logger.info("Usuário criado com sucesso: {}", savedUser.getEmail());
        
        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getNome(),
                savedUser.getEmail(),
                savedUser.getRole().getNome().name(),
                savedUser.getAtivo(),
                savedUser.getCreatedAt()
        );
    }
    
    @Transactional
    public String toggleUserStatus(Long userId) {
        logger.info("Alterando status do usuário ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        user.setAtivo(!user.getAtivo());
        userRepository.save(user);
        
        String status = user.getAtivo() ? "ativado" : "desativado";
        logger.info("Usuário {} foi {}", user.getEmail(), status);
        
        return String.format("Usuário %s com sucesso", status);
    }
}
