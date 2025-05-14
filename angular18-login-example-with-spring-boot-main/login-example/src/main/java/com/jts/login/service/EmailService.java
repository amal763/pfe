package com.jts.login.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        // Trim and validate the fromEmail immediately
        this.fromEmail = fromEmail != null ? fromEmail.trim() : null;

        if (this.fromEmail == null || this.fromEmail.isEmpty()) {
            throw new IllegalArgumentException("From email address must be configured");
        }
    }

    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        try {
            // Validate recipient email
            String cleanToEmail = toEmail != null ? toEmail.trim() : "";
            if (cleanToEmail.isEmpty() || !cleanToEmail.contains("@")) {
                throw new IllegalArgumentException("Invalid recipient email: " + toEmail);
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(cleanToEmail);
            message.setSubject("Password Reset Request");
            message.setText("To reset your password, click the link below:\n" + resetLink +
                    "\n\nThis link will expire in 24 hours.");

            mailSender.send(message);
            logger.info("Password reset email sent to {}", cleanToEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage(), e);
        }
    }
}