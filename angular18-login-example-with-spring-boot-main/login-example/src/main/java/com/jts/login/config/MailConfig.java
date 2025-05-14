package com.jts.login.config;

import jakarta.mail.NoSuchProviderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.eclipse.angus.mail.smtp.SMTPTransport; // Updated import

import java.util.Properties;
import jakarta.mail.Provider;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() throws NoSuchProviderException {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setProtocol("smtp");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3"); // Ensure TLS compatibility

        // Manually register the SMTP provider
        mailSender.setSession(mailSender.getSession());
        mailSender.getSession().setProvider(new Provider(
                Provider.Type.TRANSPORT, "smtp", SMTPTransport.class.getName(), "Oracle", null));

        return mailSender;
    }
}