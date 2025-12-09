package com.connecta.gestor.security;

import com.connecta.gestor.model.Role;
import com.connecta.gestor.model.User;
import com.connecta.gestor.model.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "TestSecretKeyForJWTTokenGenerationAndValidation123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        
        Role role = new Role();
        role.setNome(RoleType.ROLE_SUPER_ADMIN);
        
        testUser = new User();
        testUser.setEmail("test@email.com");
        testUser.setNome("Test User");
        testUser.setRole(role);
        testUser.setAtivo(true);
    }
    
    @Test
    @DisplayName("Deve gerar token JWT com sucesso")
    void shouldGenerateTokenSuccessfully() {
        String token = jwtUtil.generateToken(testUser);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }
    
    @Test
    @DisplayName("Deve extrair username do token")
    void shouldExtractUsernameFromToken() {
        String token = jwtUtil.generateToken(testUser);
        
        String username = jwtUtil.getUsernameFromToken(token);
        
        assertThat(username).isEqualTo("test@email.com");
    }
    
    @Test
    @DisplayName("Deve extrair data de expiração do token")
    void shouldExtractExpirationDateFromToken() {
        String token = jwtUtil.generateToken(testUser);
        
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(new Date());
    }
    
    @Test
    @DisplayName("Deve validar token corretamente")
    void shouldValidateTokenCorrectly() {
        String token = jwtUtil.generateToken(testUser);
        
        Boolean isValid = jwtUtil.validateToken(token, testUser);
        
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("Deve retornar false para token com username diferente")
    void shouldReturnFalseForTokenWithDifferentUsername() {
        String token = jwtUtil.generateToken(testUser);
        
        User differentUser = new User();
        differentUser.setEmail("different@email.com");
        differentUser.setNome("Different User");
        differentUser.setRole(testUser.getRole());
        differentUser.setAtivo(true);
        
        Boolean isValid = jwtUtil.validateToken(token, differentUser);
        
        assertThat(isValid).isFalse();
    }
}

