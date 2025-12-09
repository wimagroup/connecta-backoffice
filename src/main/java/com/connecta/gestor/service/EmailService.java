package com.connecta.gestor.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            
            mailSender.send(message);
            logger.info("Email enviado com sucesso para: {}", to);
            
        } catch (Exception e) {
            logger.error("Erro ao enviar email para {}: {}", to, e.getMessage());
            throw new RuntimeException("Erro ao enviar email: " + e.getMessage());
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
    
    public void sendRecoveryPasswordEmail(String to, String token) {
        String subject = "Recuperação de Senha - Connecta Gestor";
        String resetUrl = "http://localhost:4200/reset-password?token=" + token;
        
        String text = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2 style="color: #2196F3;">Recuperação de Senha</h2>
                    <p>Olá,</p>
                    <p>Recebemos uma solicitação para redefinir a senha da sua conta.</p>
                    
                    <div style="margin: 30px 0;">
                        <a href="%s" 
                           style="background-color: #2196F3; color: white; padding: 12px 25px; 
                                  text-decoration: none; border-radius: 5px; display: inline-block;">
                            Redefinir Senha
                        </a>
                    </div>
                    
                    <p>Ou copie e cole o link abaixo no seu navegador:</p>
                    <p style="background-color: #f5f5f5; padding: 10px; word-break: break-all;">
                        %s
                    </p>
                    
                    <p style="color: #d32f2f;">
                        <strong>⚠️ Este link expira em 1 hora.</strong>
                    </p>
                    
                    <p>Se você não solicitou a recuperação de senha, ignore este email.</p>
                    
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
                    <p style="color: #777; font-size: 12px;">
                        Este é um email automático. Por favor, não responda.
                    </p>
                </body>
                </html>
                """, resetUrl, resetUrl);
        
        sendEmail(to, subject, text);
    }
    
    public void sendPasswordChangedEmail(String to, String nome) {
        String subject = "Senha Alterada - Connecta Gestor";
        
        String text = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2 style="color: #4CAF50;">Senha Alterada com Sucesso</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Sua senha foi alterada com sucesso!</p>
                    
                    <p style="color: #d32f2f;">
                        <strong>⚠️ Se você não realizou esta alteração, entre em contato 
                        imediatamente com o suporte.</strong>
                    </p>
                    
                    <p>Data e hora da alteração: <strong>%s</strong></p>
                    
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
                    <p style="color: #777; font-size: 12px;">
                        Este é um email automático. Por favor, não responda.
                    </p>
                </body>
                </html>
                """, nome, java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        sendEmail(to, subject, text);
    }
}
