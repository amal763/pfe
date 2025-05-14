package com.jts.login.test;

import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.Provider;
import org.eclipse.angus.mail.smtp.SMTPTransport; // Updated import
import java.util.Properties;

public class SmtpTest {
    public static void main(String[] args) throws NoSuchProviderException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props);
        // Manually register the SMTP provider
        session.setProvider(new Provider(
                Provider.Type.TRANSPORT, "smtp", SMTPTransport.class.getName(), "Oracle", null));

        try {
            Transport transport = session.getTransport("smtp");
            transport.connect("sofyennefzi811@gmail.com", "akjadueskyzjanmb");
            System.out.println("Connected successfully!");
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}