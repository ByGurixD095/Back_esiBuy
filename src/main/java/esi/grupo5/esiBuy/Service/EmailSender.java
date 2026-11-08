package esi.grupo5.esiBuy.Service;

import jakarta.mail.MessagingException;

/** Contrato de transporte para enviar correo HTML. */
public interface EmailSender {
    void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException;
}
