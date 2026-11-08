package esi.grupo5.esiBuy.Service;

import java.util.Properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/** Transporte SMTP Gmail, disponible para entornos que permitan salida SMTP. */
@Component
@ConditionalOnProperty(prefix = "app.email", name = "provider", havingValue = "gmail-smtp", matchIfMissing = true)
public class GmailSmtpEmailSender implements EmailSender {
    private static final String TIMEOUT_PROPERTY = "10000";

    private final String username;
    private final String appPassword;

    public GmailSmtpEmailSender(
            @Value("${mail.username}") String username,
            @Value("${mail.password}") String appPassword) {
        this.username = username;
        this.appPassword = appPassword;
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", TIMEOUT_PROPERTY);
        props.put("mail.smtp.timeout", TIMEOUT_PROPERTY);
        props.put("mail.smtp.writetimeout", TIMEOUT_PROPERTY);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");
        message.setContent(htmlContent, "text/html; charset=UTF-8");
        Transport.send(message);
    }
}
