package com.connecta.gestor.service;

import com.connecta.gestor.dto.brevo.BrevoEmailRequest;
import com.connecta.gestor.dto.brevo.BrevoEmailResponse;
import com.connecta.gestor.exception.EmailSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests - API Brevo")
class EmailServiceTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    @InjectMocks
    private EmailService emailService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "brevoApiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "fromAddress", "noreply@test.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Test System");
        ReflectionTestUtils.setField(emailService, "replyToAddress", "support@test.com");
        ReflectionTestUtils.setField(emailService, "replyToName", "Test Support");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");
    }
    
    @Test
    @DisplayName("Deve enviar email com sucesso via API Brevo")
    void shouldSendEmailSuccessfully() {
        BrevoEmailResponse mockResponse = new BrevoEmailResponse();
        mockResponse.setMessageId("test-message-id-123");
        
        ResponseEntity<BrevoEmailResponse> responseEntity = 
            new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(BrevoEmailResponse.class)
        )).thenReturn(responseEntity);
        
        assertThatCode(() -> emailService.sendEmail(
                "test@email.com",
                "Test Subject",
                "<h1>Test Content</h1>"
        )).doesNotThrowAnyException();
        
        verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(BrevoEmailResponse.class)
        );
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao falhar no envio de email - Unauthorized")
    void shouldThrowExceptionWhenSendEmailFailsUnauthorized() {
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(BrevoEmailResponse.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        
        assertThatThrownBy(() -> emailService.sendEmail(
                "test@email.com",
                "Test Subject",
                "<h1>Test Content</h1>"
        )).isInstanceOf(EmailSendException.class)
          .hasMessageContaining("Falha de autenticação com API Brevo");
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao falhar no envio de email - Bad Request")
    void shouldThrowExceptionWhenSendEmailFailsBadRequest() {
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(BrevoEmailResponse.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid email".getBytes(), null));
        
        assertThatThrownBy(() -> emailService.sendEmail(
                "test@email.com",
                "Test Subject",
                "<h1>Test Content</h1>"
        )).isInstanceOf(EmailSendException.class)
          .hasMessageContaining("Requisição inválida");
    }
    
    @Test
    @DisplayName("Deve enviar email de boas-vindas com sucesso")
    void shouldSendWelcomeEmailSuccessfully() {
        BrevoEmailResponse mockResponse = new BrevoEmailResponse();
        mockResponse.setMessageId("test-message-id-456");
        
        ResponseEntity<BrevoEmailResponse> responseEntity = 
            new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(BrevoEmailResponse.class)
        )).thenReturn(responseEntity);
        
        assertThatCode(() -> emailService.sendWelcomeEmail(
                "newuser@email.com",
                "John Doe",
                "tempPassword123"
        )).doesNotThrowAnyException();
        
        verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(BrevoEmailResponse.class)
        );
    }
    
    @Test
    @DisplayName("Deve enviar email de confirmação de alteração de senha com sucesso")
    void shouldSendPasswordChangedEmailSuccessfully() {
        BrevoEmailResponse mockResponse = new BrevoEmailResponse();
        mockResponse.setMessageId("test-message-id-789");
        
        ResponseEntity<BrevoEmailResponse> responseEntity = 
            new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(BrevoEmailResponse.class)
        )).thenReturn(responseEntity);
        
        assertThatCode(() -> emailService.sendPasswordChangedEmail(
                "user@email.com",
                "John Doe"
        )).doesNotThrowAnyException();
        
        verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(BrevoEmailResponse.class)
        );
    }
}
