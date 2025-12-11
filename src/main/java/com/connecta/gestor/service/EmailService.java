package com.connecta.gestor.service;

import com.connecta.gestor.dto.brevo.BrevoEmailRequest;
import com.connecta.gestor.dto.brevo.BrevoEmailResponse;
import com.connecta.gestor.exception.EmailSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final Map<String, String> templateCache = new HashMap<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${brevo.api.key}")
    private String brevoApiKey;
    
    @Value("${email.from.address}")
    private String fromAddress;
    
    @Value("${email.from.name}")
    private String fromName;
    
    @Value("${email.replyTo.address}")
    private String replyToAddress;
    
    @Value("${email.replyTo.name}")
    private String replyToName;
    
    @Value("${app.url.frontend}")
    private String frontendUrl;
    
    private String loadTemplate(String templatePath) {
        try {
            if (templateCache.containsKey(templatePath)) {
                logger.debug("Template {} carregado do cache", templatePath);
                return templateCache.get(templatePath);
            }
            
            logger.debug("Carregando template {} do classpath", templatePath);
            ClassPathResource resource = new ClassPathResource(templatePath);
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            templateCache.put(templatePath, template);
            logger.debug("Template {} carregado e adicionado ao cache", templatePath);
            return template;
            
        } catch (Exception e) {
            logger.error("Erro ao carregar template {}: {}", templatePath, e.getMessage(), e);
            throw new EmailSendException("Erro ao carregar template de e-mail: " + templatePath, e);
        }
    }
    
    private String replaceVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
    
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            logger.debug("Preparando envio de e-mail via API Brevo para: {}", to);
            
            BrevoEmailRequest request = BrevoEmailRequest.builder()
                    .sender(BrevoEmailRequest.BrevoContact.builder()
                            .email(fromAddress)
                            .name(fromName)
                            .build())
                    .to(Collections.singletonList(BrevoEmailRequest.BrevoContact.builder()
                            .email(to)
                            .build()))
                    .replyTo(BrevoEmailRequest.BrevoContact.builder()
                            .email(replyToAddress)
                            .name(replyToName)
                            .build())
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            headers.set("accept", "application/json");
            
            HttpEntity<BrevoEmailRequest> httpEntity = new HttpEntity<>(request, headers);
            
            logger.debug("Enviando requisição para API Brevo...");
            ResponseEntity<BrevoEmailResponse> response = restTemplate.exchange(
                    BREVO_API_URL,
                    HttpMethod.POST,
                    httpEntity,
                    BrevoEmailResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                BrevoEmailResponse body = response.getBody();
                if (body != null && body.getMessageId() != null) {
                    logger.info("E-mail enviado com sucesso via API Brevo para: {} - MessageID: {}", 
                            to, body.getMessageId());
                } else {
                    logger.info("E-mail enviado com sucesso via API Brevo para: {}", to);
                }
            } else {
                logger.error("Resposta inesperada da API Brevo: {} - {}", 
                        response.getStatusCode(), response.getBody());
                throw new EmailSendException("Resposta inesperada da API Brevo: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("Erro HTTP 4xx ao enviar e-mail via API Brevo para {}: {} - {}", 
                    to, e.getStatusCode(), e.getResponseBodyAsString(), e);
            
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new EmailSendException("Falha de autenticação com API Brevo. Verifique a API key.", e);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new EmailSendException("Requisição inválida para API Brevo: " + e.getResponseBodyAsString(), e);
            } else {
                throw new EmailSendException("Erro ao enviar e-mail via API Brevo: " + e.getMessage(), e);
            }
            
        } catch (HttpServerErrorException e) {
            logger.error("Erro HTTP 5xx ao enviar e-mail via API Brevo para {}: {} - {}", 
                    to, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new EmailSendException("Erro no servidor da API Brevo: " + e.getMessage(), e);
            
        } catch (ResourceAccessException e) {
            logger.error("Erro de timeout/conexão ao enviar e-mail via API Brevo para {}: {}", 
                    to, e.getMessage(), e);
            throw new EmailSendException("Timeout ou erro de conexão com API Brevo: " + e.getMessage(), e);
            
        } catch (EmailSendException e) {
            throw e;
            
        } catch (Exception e) {
            logger.error("Erro inesperado ao enviar e-mail via API Brevo para {}: {}", 
                    to, e.getMessage(), e);
            throw new EmailSendException("Erro inesperado ao enviar e-mail: " + e.getMessage(), e);
        }
    }
    
    public void sendWelcomeEmail(String to, String nome, String senha) {
        String subject = "Bem-vindo ao Connecta Gestor!";
        
        String text = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2 style="color: #4CAF50;">Bem-vindo ao Connecta Gestor!</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Sua conta foi criada com sucesso!</p>
                    
                    <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p><strong>Suas credenciais de acesso:</strong></p>
                        <p>Email: <strong>%s</strong></p>
                        <p>Senha: <strong>%s</strong></p>
                    </div>
                    
                    <p style="color: #d32f2f;">
                        <strong>⚠️ Importante:</strong> Por segurança, recomendamos que você altere 
                        sua senha após o primeiro login.
                    </p>
                    
                    <p>Acesse o sistema e comece a utilizar todas as funcionalidades.</p>
                    
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
                    <p style="color: #777; font-size: 12px;">
                        Este é um email automático. Por favor, não responda.
                    </p>
                </body>
                </html>
                """, nome, to, senha);
        
        sendEmail(to, subject, text);
    }
    
    public void sendRecoveryPasswordEmail(String to, String nome, String token) {
        try {
            logger.debug("Preparando e-mail de recuperação de senha para: {}", to);
            
            String template = loadTemplate("templates/reset-password/forgot-password-email.html");
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            
            logger.info("URL de recuperação gerada: {}", resetUrl);
            logger.debug("Frontend URL configurada: {}", frontendUrl);
            logger.debug("Token gerado: {}", token.substring(0, Math.min(10, token.length())) + "...");
            
            Map<String, String> variables = new HashMap<>();
            variables.put("nome", nome);
            variables.put("resetLink", resetUrl);
            
            String htmlContent = replaceVariables(template, variables);
            String subject = "Recuperação de Senha - Connecta Gestor";
            
            sendEmail(to, subject, htmlContent);
            logger.info("E-mail de recuperação de senha enviado com sucesso para: {}", to);
            
        } catch (EmailSendException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao processar e-mail de recuperação para {}: {}", to, e.getMessage(), e);
            throw new EmailSendException("Erro ao enviar e-mail de recuperação de senha", e);
        }
    }
    
    public void sendPasswordChangedEmail(String to, String nome) {
        try {
            logger.debug("Preparando e-mail de confirmação de alteração de senha para: {}", to);
            
            String template = loadTemplate("templates/reset-password/reset-password-success.html");
            String loginUrl = frontendUrl + "/login";
            String dataAlteracao = LocalDateTime.now().format(DATE_FORMATTER);
            
            Map<String, String> variables = new HashMap<>();
            variables.put("nome", nome);
            variables.put("loginUrl", loginUrl);
            variables.put("dataAlteracao", dataAlteracao);
            
            String htmlContent = replaceVariables(template, variables);
            String subject = "Senha Alterada com Sucesso - Connecta Gestor";
            
            sendEmail(to, subject, htmlContent);
            logger.info("E-mail de confirmação de alteração de senha enviado com sucesso para: {}", to);
            
        } catch (EmailSendException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao processar e-mail de confirmação para {}: {}", to, e.getMessage(), e);
            throw new EmailSendException("Erro ao enviar e-mail de confirmação de alteração de senha", e);
        }
    }
}
