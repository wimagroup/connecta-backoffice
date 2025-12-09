package com.connecta.gestor.service;

import com.connecta.gestor.dto.*;
import com.connecta.gestor.exception.EmailAlreadyExistsException;
import com.connecta.gestor.exception.InvalidTokenException;
import com.connecta.gestor.exception.ResourceNotFoundException;
import com.connecta.gestor.model.Role;
import com.connecta.gestor.model.User;
import com.connecta.gestor.repository.RoleRepository;
import com.connecta.gestor.repository.UserRepository;
import com.connecta.gestor.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private EmailService emailService;
    
    @Transactional(readOnly = true)
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
            
            String token = jwtUtil.generateToken(user);
            
            logger.info("Login realizado com sucesso para: {}", request.getEmail());
            
            return new LoginResponseDTO(
                    token,
                    user.getEmail(),
                    user.getNome(),
                    user.getRole().getNome().name()
            );
            
        } catch (BadCredentialsException e) {
            logger.error("Falha no login para: {}", request.getEmail());
            throw new BadCredentialsException("Email ou senha inválidos");
        }
    }
    
    @Transactional
    public void recoveryPassword(RecoveryPasswordRequestDTO request) {
        logger.info("Solicitação de recuperação de senha para: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com o email informado"));
        
        String token = UUID.randomUUID().toString();
        
        user.setTokenRecuperacao(token);
        user.setTokenRecuperacaoExpiracao(LocalDateTime.now().plusHours(1));
        
        userRepository.save(user);
        
        emailService.sendRecoveryPasswordEmail(user.getEmail(), token);
        
        logger.info("Email de recuperação enviado para: {}", request.getEmail());
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        logger.info("Tentativa de reset de senha com token");
        
        User user = userRepository.findByTokenRecuperacao(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Token inválido"));
        
        if (user.getTokenRecuperacaoExpiracao() == null ||
            LocalDateTime.now().isAfter(user.getTokenRecuperacaoExpiracao())) {
            throw new InvalidTokenException("Token expirado");
        }
        
        user.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        user.setTokenRecuperacao(null);
        user.setTokenRecuperacaoExpiracao(null);
        
        userRepository.save(user);
        
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getNome());
        
        logger.info("Senha redefinida com sucesso para o usuário: {}", user.getEmail());
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
