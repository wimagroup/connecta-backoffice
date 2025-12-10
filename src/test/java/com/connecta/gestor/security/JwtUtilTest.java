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
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 2592000000L);
        
        Role role = new Role();
        role.setNome(RoleType.ROLE_SUPER_ADMIN);
        
        testUser = new User();
        testUser.setEmail("test@email.com");
        testUser.setNome("Test User");
        testUser.setRole(role);
        testUser.setAtivo(true);
    }
    
    @Test
    @DisplayName("Deve gerar access token JWT com sucesso")
    void shouldGenerateAccessTokenSuccessfully() {
        String token = jwtUtil.generateAccessToken(testUser);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }
    
    @Test
    @DisplayName("Deve gerar refresh token com sucesso")
    void shouldGenerateRefreshTokenSuccessfully() {
        String refreshToken = jwtUtil.generateRefreshToken();
        
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
    }
    
    @Test
    @DisplayName("Deve extrair username do access token")
    void shouldExtractUsernameFromToken() {
        String token = jwtUtil.generateAccessToken(testUser);
        
        String username = jwtUtil.getUsernameFromToken(token);
        
        assertThat(username).isEqualTo("test@email.com");
    }
    
    @Test
    @DisplayName("Deve extrair data de expiração do token")
    void shouldExtractExpirationDateFromToken() {
        String token = jwtUtil.generateAccessToken(testUser);
        
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(new Date());
    }
    
    @Test
    @DisplayName("Deve validar access token corretamente")
    void shouldValidateTokenCorrectly() {
        String token = jwtUtil.generateAccessToken(testUser);
        
        Boolean isValid = jwtUtil.validateToken(token, testUser);
        
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("Deve retornar false para token com username diferente")
    void shouldReturnFalseForTokenWithDifferentUsername() {
        String token = jwtUtil.generateAccessToken(testUser);
        
        User differentUser = new User();
        differentUser.setEmail("different@email.com");
        differentUser.setNome("Different User");
        differentUser.setRole(testUser.getRole());
        differentUser.setAtivo(true);
        
        Boolean isValid = jwtUtil.validateToken(token, differentUser);
        
        assertThat(isValid).isFalse();
    }
}

