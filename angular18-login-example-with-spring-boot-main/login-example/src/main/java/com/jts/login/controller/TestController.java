package com.jts.login.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public TestController(JavaMailSender mailSender,
                          @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @GetMapping("/email")
    public String testEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo("test@example.com");
            message.setSubject("Test Email");
            message.setText("This is a test email from the application");

            mailSender.send(message);
            return "Email sent successfully from: " + fromEmail;
        } catch (Exception e) {
            return "Email failed: " + e.getMessage();
        }
    }
}