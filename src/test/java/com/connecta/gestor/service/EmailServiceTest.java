package com.connecta.gestor.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {
    
    @Mock
    private JavaMailSender mailSender;
    
    @Mock
    private MimeMessage mimeMessage;
    
    @InjectMocks
    private EmailService emailService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@connecta.com");
    }
    
    @Test
    @DisplayName("Deve enviar email com sucesso")
    void shouldSendEmailSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        
        assertThatCode(() -> emailService.sendEmail(
                "test@email.com",
                "Test Subject",
                "<h1>Test Content</h1>"
        )).doesNotThrowAnyException();
        
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao falhar no envio de email")
    void shouldThrowExceptionWhenSendEmailFails() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(MimeMessage.class));
        
        assertThatThrownBy(() -> emailService.sendEmail(
                "test@email.com",
                "Test Subject",
                "<h1>Test Content</h1>"
        )).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Erro ao enviar email");
    }
    
    @Test
    @DisplayName("Deve enviar email de boas-vindas com sucesso")
    void shouldSendWelcomeEmailSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        
        assertThatCode(() -> emailService.sendWelcomeEmail(
                "newuser@email.com",
                "John Doe",
                "tempPassword123"
        )).doesNotThrowAnyException();
        
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
    
    @Test
    @DisplayName("Deve enviar email de recuperação de senha com sucesso")
    void shouldSendRecoveryPasswordEmailSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        
        assertThatCode(() -> emailService.sendRecoveryPasswordEmail(
                "user@email.com",
                "recovery-token-123"
        )).doesNotThrowAnyException();
        
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
    
    @Test
    @DisplayName("Deve enviar email de confirmação de alteração de senha com sucesso")
    void shouldSendPasswordChangedEmailSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        
        assertThatCode(() -> emailService.sendPasswordChangedEmail(
                "user@email.com",
                "John Doe"
        )).doesNotThrowAnyException();
        
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
}


