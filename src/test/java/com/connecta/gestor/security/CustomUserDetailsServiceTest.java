package com.connecta.gestor.security;

import com.connecta.gestor.model.Role;
import com.connecta.gestor.model.User;
import com.connecta.gestor.model.enums.RoleType;
import com.connecta.gestor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setNome(RoleType.ROLE_SUPER_ADMIN);
        role.setDescricao("Super Administrador");
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@email.com");
        testUser.setSenha("encodedPassword");
        testUser.setNome("Test User");
        testUser.setRole(role);
        testUser.setAtivo(true);
    }
    
    @Test
    @DisplayName("Deve carregar usuário por email com sucesso")
    void shouldLoadUserByUsernameSuccessfully() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@email.com");
        
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@email.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.isEnabled()).isTrue();
        
        verify(userRepository).findByEmail("test@email.com");
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent@email.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado com email:");
        
        verify(userRepository).findByEmail("nonexistent@email.com");
    }
}



